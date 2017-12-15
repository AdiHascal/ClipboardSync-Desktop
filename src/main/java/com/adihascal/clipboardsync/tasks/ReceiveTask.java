package com.adihascal.clipboardsync.tasks;

import com.adihascal.clipboardsync.Main;
import com.adihascal.clipboardsync.handler.GuiHandler.ProgramState;
import com.adihascal.clipboardsync.handler.TaskHandler;
import com.adihascal.clipboardsync.network.SocketHolder;
import com.adihascal.clipboardsync.transferable.FileListTransferable;
import com.adihascal.clipboardsync.util.DynamicSequenceInputStream;
import com.adihascal.clipboardsync.util.IStreamSupplier;

import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import static com.adihascal.clipboardsync.network.SocketHolder.in;
import static com.adihascal.clipboardsync.network.SocketHolder.out;

public class ReceiveTask implements ITask, IStreamSupplier<InputStream>
{
	private static final int chunkSize = 15728640;
	private volatile int currentChunk = 0;
	private long size;
	private int nChunks;
	
	private void getChunk(RandomAccessFile raf, long length)
	{
		int totalBytesRead = 0;
		try
		{
			byte[] buffer = new byte[chunkSize / 1024];
			int bytesRead;
			totalBytesRead = (int) raf.getFilePointer();
			while(totalBytesRead < length)
			{
				bytesRead = in().read(buffer, 0, Math.min(chunkSize - totalBytesRead, buffer.length));
				raf.write(buffer, 0, bytesRead);
				totalBytesRead += bytesRead;
			}
		}
		catch(IOException e)
		{
			try
			{
				System.out.println("network error. waiting.");
				TaskHandler.INSTANCE.pause();
				System.out.println("seeking to position " + totalBytesRead);
				out().writeLong(totalBytesRead);
				raf.seek(totalBytesRead);
				System.out.println("receiving chunk " + currentChunk + " recursively");
				getChunk(raf, length);
			}
			catch(InterruptedException | IOException e1)
			{
				e1.printStackTrace();
			}
		}
	}
	
	private void deleteExisting() throws IOException
	{
		Files.list(Main.localFolder.toPath())
				.filter(path -> !(Files.isDirectory(path) && (path.getFileName().toString().equals("chunks") ||
						path.getFileName().toString().equals("packed")))).forEach(this :: deleteFile);
		Files.list(Main.packedTemp.toPath()).forEach(this :: deleteFile);
	}
	
	private void deleteFile(Path f)
	{
		try
		{
			Files.walkFileTree(f, new SimpleFileVisitor<Path>()
			{
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
				{
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}
				
				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
				{
					Files.delete(dir);
					return FileVisitResult.CONTINUE;
				}
			});
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void run()
	{
		try
		{
			Main.getGuiHandler().setStatus(ProgramState.RECEIVING_FILES);
			deleteExisting();
			size = in().readLong(); //for the number of files
			nChunks = (int) Math.ceil((double) size / chunkSize);
			SocketHolder.getSocket().setSoTimeout(5000);
			new Thread(new Unpacker(), "ClipboardSync Unpacker").start();
			for(int i = 0; i < nChunks; i++)
			{
				String path = Main.packedTemp.getPath() + "\\" + Integer.toString(i) + ".bin";
				Files.createFile(Paths.get(path));
				RandomAccessFile raf = new RandomAccessFile(path, "rw");
				System.out.println("receiving chunk " + i);
				getChunk(raf, length(i));
				raf.close();
				System.out.println("incrementing currentChunk to " + (currentChunk + 1));
				//noinspection NonAtomicOperationOnVolatileField
				currentChunk++;
			}
			finish();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public InputStream next(int index)
	{
		try
		{
			System.out.println("loading chunk " + index);
			return new FileInputStream(new File(Main.packedTemp, Integer.toString(index) + ".bin"));
		}
		catch(FileNotFoundException e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public boolean canProvide(int index)
	{
		return index < currentChunk;
	}
	
	@Override
	public void afterClose(int index)
	{
		try
		{
			Files.delete(Paths.get(Main.packedTemp.getPath(), Integer.toString(index) + ".bin"));
			System.out.println("deleted chunk " + index);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public long length(int index)
	{
		if(index == nChunks - 1 && size % chunkSize != 0)
		{
			return size % chunkSize;
		}
		else
		{
			return chunkSize;
		}
	}
	
	private class Unpacker implements Runnable
	{
		private DynamicSequenceInputStream src;
		
		private void read(List<File> toTransfer, String parent) throws IOException
		{
			File f;
			String path = Main.localFolder.getPath();
			String thing = src.readUTF();
			if(thing.equals("file"))
			{
				if(parent != null)
				{
					path = parent;
				}
				path += "/" + src.readUTF();
				f = new File(path);
				Files.createFile(f.toPath());
				byte[] buffer = new byte[15360];
				int bytesRead;
				int totalBytesRead = 0;
				long length = src.readLong();
				FileOutputStream output = new FileOutputStream(f);
				while(totalBytesRead < length)
				{
					bytesRead = src.read(buffer, 0, (int) Math.min(length - totalBytesRead, 15360));
					totalBytesRead += bytesRead;
					output.write(buffer, 0, bytesRead);
				}
				output.close();
				if(parent == null)
				{
					toTransfer.add(f);
				}
			}
			else
			{
				if(parent != null)
				{
					path = parent;
				}
				path += "/" + src.readUTF();
				f = new File(path);
				Files.createDirectory(f.toPath());
				int nFiles = src.readInt();
				for(int i = 0; i < nFiles; i++)
				{
					read(toTransfer, f.getPath());
				}
				if(parent == null)
				{
					toTransfer.add(f);
				}
			}
		}
		
		@Override
		public void run()
		{
			try
			{
				this.src = new DynamicSequenceInputStream(ReceiveTask.this);
				int nFiles = src.readInt();
				ArrayList<File> toTransfer = new ArrayList<>(nFiles);
				for(int i = 0; i < nFiles; i++)
				{
					read(toTransfer, null);
				}
				src.close();
				Toolkit.getDefaultToolkit().getSystemClipboard()
						.setContents(new FileListTransferable(toTransfer), Main.INSTANCE);
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
	}
}
