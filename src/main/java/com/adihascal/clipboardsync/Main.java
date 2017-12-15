package com.adihascal.clipboardsync;

import com.adihascal.clipboardsync.handler.ClipHandlerRegistry;
import com.adihascal.clipboardsync.handler.GuiHandler;
import com.adihascal.clipboardsync.handler.TextHandler;
import com.adihascal.clipboardsync.network.SyncClient;
import com.adihascal.clipboardsync.network.SyncServer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import lc.kra.system.keyboard.GlobalKeyboardHook;
import lc.kra.system.keyboard.event.GlobalKeyAdapter;
import lc.kra.system.keyboard.event.GlobalKeyEvent;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.IOException;

import static lc.kra.system.keyboard.event.GlobalKeyEvent.*;

public class Main extends Application implements ClipboardOwner
{
	public static final Main INSTANCE = new Main();
	public static final File localFolder = new File(System.getProperty("user.home") + "/AppData/Local/ClipboardSync");
	public static final File packedTemp = new File(localFolder, "packed");
	public static volatile boolean isBusy = false;
	private static int port;
	private static SyncServer server = new SyncServer();
	private static Transferable prev;
	private static GlobalKeyboardHook hook = new GlobalKeyboardHook(true);
	private static GuiHandler guiHandler;
	
	public static void main(String[] args)
	{
		port = 63708;
		try
		{
			if(!packedTemp.exists())
			{
				if(!packedTemp.mkdirs())
				{
					throw new Exception("failed to create temporary folder");
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		System.out.println("\n\nQR code is located in desktop\\image.png");
		
		Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable t = clip.getContents(INSTANCE);
		clip.setContents(t, INSTANCE);
		server.start();
		hook.addKeyListener(new GlobalKeyAdapter()
		{
			@Override
			public void keyPressed(GlobalKeyEvent e)
			{
				if(e.toString().equals("86 [down,menu,control]"))
				{
					Main.pastePrevAndSwap();
				}
			}
		});
		Runtime.getRuntime().addShutdownHook(new Thread(() ->
		{
			new SyncClient("disconnect", null).start();
			hook.shutdownHook();
			server.interrupt();
		}));
		launch(args);
	}
	
	private static void pastePrevAndSwap()
	{
		if(prev != null)
		{
			Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
			Transferable toPut = prev;
			prev = clip.getContents(INSTANCE);
			clip.setContents(toPut, INSTANCE);
			
			Robot robot = null;
			try
			{
				robot = new Robot();
			}
			catch(AWTException e)
			{
				e.printStackTrace();
			}
			
			if(robot != null)
			{
				robot.setAutoDelay(20);
				robot.keyRelease(VK_CONTROL);
				robot.keyRelease(VK_MENU);
				robot.keyRelease(VK_V);
				robot.keyPress(VK_CONTROL);
				robot.keyPress(VK_V);
				robot.keyRelease(VK_CONTROL);
				robot.keyRelease(VK_V);
			}
		}
	}
	
	public static int getPort()
	{
		return port;
	}
	
	public static SyncServer getServer()
	{
		return server;
	}
	
	public static GuiHandler getGuiHandler()
	{
		return guiHandler;
	}
	
	public static void restart()
	{
		server = new SyncServer();
		server.start();
	}
	
	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents)
	{
		try
		{
			Thread.sleep(20);
			Transferable content = clipboard.getContents(this);
			clipboard.setContents(content, Main.INSTANCE);
			if(!isBusy)
			{
				if(ClipHandlerRegistry
						.getHandlerFor(ClipHandlerRegistry.getSuitableFlavor(content.getTransferDataFlavors())
								.getMimeType()) instanceof TextHandler)
				{
					prev = content;
				}
				new SyncClient("announce", content).start();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public void start(Stage primaryStage)
	{
		try
		{
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout.fxml"));
			Parent root = loader.load();
			guiHandler = loader.getController();
			guiHandler.setup(primaryStage, root);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public void stop() throws Exception
	{
		super.stop();
		System.exit(0);
	}
}
