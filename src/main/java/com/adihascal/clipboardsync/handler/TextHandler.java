package com.adihascal.clipboardsync.handler;

import com.adihascal.clipboardsync.Main;

import java.awt.datatransfer.*;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class TextHandler implements IClipHandler
{
	@Override
	public void sendClip(Socket s, Transferable clip) throws IOException, UnsupportedFlavorException
	{
		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
		out.writeUTF("text/plain");
		out.writeUTF((String) clip.getTransferData(DataFlavor.stringFlavor));
		out.flush();
	}
	
	@Override
	public void receiveClip(DataInputStream s, Clipboard manager) throws IOException
	{
		manager.setContents(new StringSelection(s.readUTF()), Main.INSTANCE);
	}
}
