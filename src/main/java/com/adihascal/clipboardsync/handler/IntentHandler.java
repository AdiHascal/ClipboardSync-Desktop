package com.adihascal.clipboardsync.handler;

import com.adihascal.clipboardsync.tasks.ReceiveTask;
import com.adihascal.clipboardsync.tasks.SendTask;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.util.List;

public class IntentHandler implements IClipHandler
{
	@Override
	public void sendClip(Transferable clip) throws Exception
	{
		List<File> files = (List<File>) clip.getTransferData(DataFlavor.javaFileListFlavor);
		TaskHandler.INSTANCE.setAndRun(new SendTask(files));
	}
	
	@Override
	public void receiveClip(Clipboard manager) throws Exception
	{
		TaskHandler.INSTANCE.setAndRun(new ReceiveTask());
	}
}
