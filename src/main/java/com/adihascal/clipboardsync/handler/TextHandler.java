package com.adihascal.clipboardsync.handler;

import com.adihascal.clipboardsync.Main;
import com.adihascal.clipboardsync.handler.GuiHandler.ProgramState;

import java.awt.datatransfer.*;
import java.io.IOException;

import static com.adihascal.clipboardsync.network.SocketHolder.in;
import static com.adihascal.clipboardsync.network.SocketHolder.out;

public class TextHandler implements IClipHandler
{
	@Override
	public void sendClip(Transferable clip) throws IOException, UnsupportedFlavorException
	{
		out().writeUTF("text/plain");
		out().writeUTF((String) clip.getTransferData(DataFlavor.stringFlavor));
		Main.isBusy = false;
		Main.getGuiHandler().setStatus(ProgramState.IDLE);
	}
	
	@Override
	public void receiveClip(Clipboard manager) throws IOException
	{
		manager.setContents(new StringSelection(in().readUTF()), Main.INSTANCE);
		Main.isBusy = false;
		Main.getGuiHandler().setStatus(ProgramState.IDLE);
	}
}
