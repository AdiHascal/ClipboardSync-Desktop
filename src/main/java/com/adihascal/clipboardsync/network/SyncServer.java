package com.adihascal.clipboardsync.network;

import com.adihascal.clipboardsync.Main;
import com.adihascal.clipboardsync.handler.ClipHandlerRegistry;
import com.adihascal.clipboardsync.handler.GuiHandler.ProgramState;
import com.adihascal.clipboardsync.handler.IClipHandler;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.IOException;

import static com.adihascal.clipboardsync.network.SocketHolder.in;
import static com.adihascal.clipboardsync.network.SocketHolder.out;

public class SyncServer extends Thread
{
	private Transferable temp;
	private boolean paused = false;
	
	public SyncServer()
	{
		super("ClipBoardSync Server");
	}
	
	@Override
	public synchronized void run()
	{
		try
		{
			SocketHolder.init();
			
			while(true)
			{
				System.out.println("waiting for input");
				while(Main.isBusy && !paused)
				{
					wait(1000);
				}
				String command = in().readUTF();
				switch(command)
				{
					case "receive":
						Main.isBusy = true;
						IClipHandler handler =
								ClipHandlerRegistry.getHandlerFor(in().readUTF());
						handler.receiveClip(Toolkit.getDefaultToolkit().getSystemClipboard());
						System.out.println("receiving data");
						break;
					case "connect":
						SyncClient.phoneAddress = in().readUTF();
						Main.getGuiHandler().setPhoneName(in().readUTF()).setStatus(ProgramState.IDLE);
						System.out.println(SyncClient.phoneAddress + " connected");
						break;
					case "disconnect":
						if(SyncClient.phoneAddress != null)
						{
							System.out.println(SyncClient.phoneAddress + " disconnected");
						}
						SyncClient.phoneAddress = null;
						Main.getGuiHandler().resetPhoneName().setStatus(ProgramState.DISCONNECTED);
						SocketHolder.invalidate();
						Main.restart();
						return;
					case "accept":
						DataFlavor flavor = ClipHandlerRegistry.getSuitableFlavor(this.temp.getTransferDataFlavors());
						if(flavor != null)
						{
							Main.isBusy = true;
							out().writeUTF("receive");
							ClipHandlerRegistry.getHandlerFor(flavor.getMimeType())
									.sendClip(this.temp);
							System.out.println("sending data");
							this.temp = null;
						}
						break;
					case "refuse":
						this.temp = null;
						Main.getGuiHandler().setStatus(ProgramState.IDLE);
						System.out.println("remote refused local data");
						break;
					case "pause":
						System.out.println("paused");
						Main.getGuiHandler().setStatus(ProgramState.STANDBY);
						paused = true;
						Main.isBusy = true;
						break;
					case "resume":
						System.out.println("resumed");
						Main.getGuiHandler().setStatus(ProgramState.IDLE);
						paused = false;
						Main.isBusy = false;
						break;
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("IO Error. Restarting server.");
			Main.restart();
		}
	}
	
	@Override
	public void interrupt()
	{
		try
		{
			SocketHolder.invalidate();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		super.interrupt();
	}
	
	void setTransferable(Transferable temp)
	{
		this.temp = temp;
	}
}
