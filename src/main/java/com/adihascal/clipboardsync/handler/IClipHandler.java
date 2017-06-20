package com.adihascal.clipboardsync.handler;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public interface IClipHandler
{
	void sendClip(Socket s, Transferable clip) throws IOException, UnsupportedFlavorException;
	
	void receiveClip(DataInputStream s, Clipboard manager) throws IOException;
}
