package com.adihascal.clipboardsync.handler;

import com.adihascal.clipboardsync.Main;
import com.adihascal.clipboardsync.transferable.FileListTransferable;
import com.adihascal.clipboardsync.util.Utilities;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class IntentHandler implements IClipHandler
{
	
	@Override
	public void sendClip(DataOutputStream out, Transferable clip) throws IOException, UnsupportedFlavorException
	{
		List<File> files = (List) clip.getTransferData(DataFlavor.javaFileListFlavor);
		out.writeUTF("application/x-java-serialized-object");
		out.writeLong(getPayloadSize(files.toArray(new File[0])));
		out.writeInt(files.size());
		
		for(File f : files)
		{
			sendFile(out, f);
		}
		
		out.flush();
		Main.isBusy = false;
	}
	
	@Override
	public void receiveClip(DataInputStream s, Clipboard manager) throws IOException
	{
		final File[] existing = Main.localFolder.listFiles();
		
		if(existing != null)
		{
			new Thread(() ->
			{
				for(File f : existing)
				{
					try
					{
						deleteFile(f);
					}
					catch(IOException e)
					{
						e.printStackTrace();
					}
				}
			}).start();
		}
		
		int nFiles = s.readInt();
		ArrayList<File> toTransfer = new ArrayList<>();
		for(int i = 0; i < nFiles; i++)
		{
			receiveFile(s, toTransfer, null);
		}
		
		manager.setContents(new FileListTransferable(toTransfer), Main.INSTANCE);
	}
	
	private void sendFile(DataOutputStream out, File f) throws IOException
	{
		if(!f.isDirectory())
		{
			FileInputStream in = new FileInputStream(f);
			out.writeUTF("file");
			out.writeUTF(f.getName());
			out.writeLong(f.length());
			Utilities.copyStream(in, out);
			in.close();
		}
		else
		{
			out.writeUTF("dir");
			File[] subs = f.listFiles();
			assert subs != null;
			out.writeUTF(f.getName());
			out.writeInt(subs.length);
			for(File sub : subs)
			{
				sendFile(out, sub);
			}
		}
	}
	
	private void receiveFile(DataInputStream in, List<File> toTransfer, String parent) throws IOException
	{
		File f;
		String path = Main.localFolder.getPath();
		String thing = in.readUTF();
		if(thing.equals("file"))
		{
			if(parent != null)
			{
				path = parent;
			}
			path += "/" + in.readUTF();
			f = new File(path);
			f.createNewFile();
			FileOutputStream out = new FileOutputStream(f);
			Utilities.copyStream(in, out, (int) in.readLong());
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
			path += "/" + in.readUTF();
			f = new File(path);
			f.mkdir();
			int nFiles = in.readInt();
			for(int i = 0; i < nFiles; i++)
			{
				receiveFile(in, toTransfer, f.getPath());
			}
			if(parent == null)
			{
				toTransfer.add(f);
			}
		}
	}
	
	private long getPayloadSize(File... files)
	{
		long size = 0L;
		
		for(File f : files)
		{
			if(!f.isDirectory())
			{
				size += f.length();
			}
			else
			{
				for(File sub : f.listFiles())
				{
					size += getPayloadSize(sub);
				}
			}
		}
		
		return size;
	}
	
	private void deleteFile(File f) throws IOException
	{
		if(!f.isDirectory())
		{
			Files.delete(f.toPath());
		}
		else
		{
			Files.list(f.toPath()).forEach(sub ->
			{
				try
				{
					deleteFile(sub.toFile());
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
			});
		}
	}
}
