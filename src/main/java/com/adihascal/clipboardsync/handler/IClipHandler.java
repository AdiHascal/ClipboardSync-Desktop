package com.adihascal.clipboardsync.handler;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public interface IClipHandler
{
	void sendClip(Transferable clip) throws IOException, UnsupportedFlavorException;
	
	void receiveClip(Clipboard manager) throws IOException;
}
