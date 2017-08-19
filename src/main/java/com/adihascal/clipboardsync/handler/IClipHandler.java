package com.adihascal.clipboardsync.handler;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;

public interface IClipHandler
{
	void sendClip(Transferable clip) throws Exception;
	
	void receiveClip(Clipboard manager) throws Exception;
}
