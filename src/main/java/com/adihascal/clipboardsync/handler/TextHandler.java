package com.adihascal.clipboardsync.handler;

import com.adihascal.clipboardsync.Main;

import java.awt.datatransfer.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TextHandler implements IClipHandler
{
	@Override
	public void sendClip(DataOutputStream out, Transferable clip) throws IOException, UnsupportedFlavorException
	{
		out.writeUTF("text/plain");
		out.writeUTF((String) clip.getTransferData(DataFlavor.stringFlavor));
		out.flush();
		Main.isBusy = false;
	}
	
	@Override
	public void receiveClip(DataInputStream s, Clipboard manager) throws IOException
	{
		manager.setContents(new StringSelection(s.readUTF()), Main.INSTANCE);
	}
}
