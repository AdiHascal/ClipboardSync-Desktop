package com.adihascal.clipboardsync.handler;

import com.adihascal.clipboardsync.Main;
import com.adihascal.clipboardsync.thread.FileDeleter;
import com.adihascal.clipboardsync.transferable.FileListTransferable;
import sun.misc.IOUtils;

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
		List<File> files = (List<File>) clip.getTransferData(DataFlavor.javaFileListFlavor);
		
		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(s.getOutputStream(), 104857600));
		out.writeUTF("application/x-java-serialized-object");
		out.write(files.size());
		
		for(File f : files)
		{
			FileInputStream in = new FileInputStream(f);
			out.writeUTF(f.getName());
			out.writeLong(f.length());
			byte[] data = IOUtils.readFully(in, -1, true);
			out.write(data);
			out.flush();
			in.close();
		}
	}
	
	@Override
	public void receiveClip(DataInputStream s, Clipboard manager) throws IOException
	{
		new Thread(new FileDeleter(Main.localFolder)).start();
		
		int nFiles = s.read();
		File[] toTransfer = new File[nFiles];
		byte[] buf;
		
		for(int i = 0; i < nFiles; i++)
		{
			File f = new File(Main.localFolder, s.readUTF());
			if(f.createNewFile())
			{
				FileOutputStream out = new FileOutputStream(f);
				buf = new byte[Math.toIntExact(s.readLong())];
				s.read(buf);
				out.write(buf);
				out.close();
			}
			toTransfer[i] = f;
		}
		
		manager.setContents(new FileListTransferable(toTransfer), Main.INSTANCE);
	}
}
