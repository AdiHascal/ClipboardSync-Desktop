package com.adihascal.clipboardsync.handler;

import com.adihascal.clipboardsync.Main;
import com.adihascal.clipboardsync.transferable.FileListTransferable;
import com.adihascal.clipboardsync.util.Utilities;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.*;
import java.net.Socket;
import java.util.List;

public class IntentHandler implements IClipHandler
{
	
	@Override
	public void sendClip(Socket s, Transferable clip) throws IOException, UnsupportedFlavorException
	{
		List<File> files = (List) clip.getTransferData(DataFlavor.javaFileListFlavor);
		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
		out.writeUTF("application/x-java-serialized-object");
		out.writeInt(files.size());
		
		for(File f : files)
		{
			FileInputStream in = new FileInputStream(f);
			out.writeUTF(f.getName());
			out.writeLong(f.length());
			Utilities.copyStream(in, out);
			in.close();
		}
		
		out.flush();
		Main.isBusy = false;
	}
	
	@Override
	public void receiveClip(DataInputStream s, Clipboard manager) throws IOException
	{
		for(File f : Main.localFolder.listFiles())
		{
			f.delete();
		}
		
		int nFiles = s.readInt();
		File[] toTransfer = new File[nFiles];
		long len;
		
		for(int i = 0; i < nFiles; i++)
		{
			File f = new File(Main.localFolder, s.readUTF());
			if(f.createNewFile())
			{
				FileOutputStream out = new FileOutputStream(f);
				len = s.readLong();
				Utilities.copyStream(s, out, (int) len);
				out.close();
			}
			toTransfer[i] = f;
		}
		
		manager.setContents(new FileListTransferable(toTransfer), Main.INSTANCE);
	}
}
