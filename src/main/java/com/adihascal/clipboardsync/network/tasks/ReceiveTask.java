package com.adihascal.clipboardsync.network.tasks;

import com.adihascal.clipboardsync.Main;
import com.adihascal.clipboardsync.transferable.FileListTransferable;

import java.awt.*;
import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import static com.adihascal.clipboardsync.network.SocketHolder.in;

public class ReceiveTask
{
	private void copyStream(InputStream input, OutputStream output, final long length) throws IOException
	{
		byte[] buffer = new byte[15360];
		int bytesRead;
		int totalBytesRead = 0;
		while(totalBytesRead < length)
		{
			bytesRead = input.read(buffer, 0, (int) Math.min(length - totalBytesRead, 15360));
			totalBytesRead += bytesRead;
			output.write(buffer, 0, bytesRead);
		}
		output.close();
	}
	
	private void receiveFile(List<File> toTransfer, String parent) throws IOException
	{
		File f;
		String path = Main.localFolder.getPath();
		String thing = in().readUTF();
		if(thing.equals("file"))
		{
			if(parent != null)
			{
				path = parent;
			}
			path += "/" + in().readUTF();
			f = new File(path);
			Files.createFile(f.toPath());
			copyStream(in(), new FileOutputStream(f), in().readLong());
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
			path += "/" + in().readUTF();
			f = new File(path);
			Files.createDirectory(f.toPath());
			int nFiles = in().readInt();
			for(int i = 0; i < nFiles; i++)
			{
				receiveFile(toTransfer, f.getPath());
			}
			if(parent == null)
			{
				toTransfer.add(f);
			}
		}
	}
	
	private void run()
	{
		try
		{
			final File[] existing = Main.localFolder.listFiles();
			
			if(existing != null)
			{
				for(File f : existing)
				{
					try
					{
						if(!(f.isDirectory() && f.getName().equals("chunks")))
						{
							deleteFile(f.toPath());
						}
					}
					catch(IOException | InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			}
			
			int nFiles = in().readInt();
			ArrayList<File> toTransfer = new ArrayList<>();
			for(int i = 0; i < nFiles; i++)
			{
				receiveFile(toTransfer, null);
			}
			
			Toolkit.getDefaultToolkit().getSystemClipboard()
					.setContents(new FileListTransferable(toTransfer), Main.INSTANCE);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void deleteFile(Path f) throws IOException, InterruptedException
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
	
	public void exec()
	{
		this.run();
	}
}
