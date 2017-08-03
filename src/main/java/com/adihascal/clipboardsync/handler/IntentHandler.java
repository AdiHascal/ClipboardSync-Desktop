package com.adihascal.clipboardsync.handler;

import com.adihascal.clipboardsync.network.tasks.ReceiveTask;
import com.adihascal.clipboardsync.network.tasks.SendTask;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class IntentHandler implements IClipHandler
{
	
	@Override
	public void sendClip(Transferable clip) throws IOException, UnsupportedFlavorException
	{
		List<File> files = (List<File>) clip.getTransferData(DataFlavor.javaFileListFlavor);
		new SendTask(files).exec();
	}
	
	@Override
	public void receiveClip(Clipboard manager) throws IOException
	{
		new ReceiveTask().exec();
	}
}
