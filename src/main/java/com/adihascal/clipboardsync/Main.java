package com.adihascal.clipboardsync;

import com.adihascal.clipboardsync.network.SyncClient;
import com.adihascal.clipboardsync.network.SyncServer;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lc.kra.system.keyboard.GlobalKeyboardHook;
import lc.kra.system.keyboard.event.GlobalKeyAdapter;
import lc.kra.system.keyboard.event.GlobalKeyEvent;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.EnumMap;
import java.util.Map;

import static lc.kra.system.keyboard.event.GlobalKeyEvent.*;

public class Main implements ClipboardOwner
{
	public static final Main INSTANCE = new Main();
	private static final String localFolderName = System.getProperty("user.home") + "/AppData/Local/ClipboardSync";
	public static final File localFolder = new File(localFolderName);
	public static boolean isBusy = false;
	private static int port;
	private static SyncServer server = new SyncServer();
	private static Transferable prev;
	private static GlobalKeyboardHook hook = new GlobalKeyboardHook(true);
	
	public static void main(String[] args) throws IOException
	{
		port = 63708;
		try
		{
			if(!localFolder.exists())
			{
				if(!localFolder.mkdir())
				{
					throw new Exception("failed to create temporary folder");
				}
			}
			File imageFile = new File(System.getProperty("user.home") + "/Desktop/image.png");
			Map<EncodeHintType, Object> hintMap = new EnumMap<>(EncodeHintType.class);
			hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");
			
			hintMap.put(EncodeHintType.MARGIN, 1);
			hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
			
			QRCodeWriter qrCodeWriter = new QRCodeWriter();
			BitMatrix bitMatrix = qrCodeWriter.encode(InetAddress.getLocalHost().getHostName() + "," + InetAddress.getLocalHost().getHostAddress(), BarcodeFormat.QR_CODE, 250, 250, hintMap);
			BufferedImage image = new BufferedImage(bitMatrix.getWidth(), bitMatrix.getHeight(), BufferedImage.TYPE_INT_RGB);
			image.createGraphics();
			
			Graphics2D graphics = (Graphics2D) image.getGraphics();
			graphics.setColor(Color.WHITE);
			graphics.fillRect(0, 0, bitMatrix.getWidth(), bitMatrix.getHeight());
			graphics.setColor(Color.BLACK);
			
			for(int i = 0; i < bitMatrix.getWidth(); i++)
			{
				for(int j = 0; j < bitMatrix.getWidth(); j++)
				{
					if(bitMatrix.get(i, j))
					{
						graphics.fillRect(i, j, 1, 1);
					}
				}
			}
			ImageIO.write(image, "png", imageFile);
		} catch(Exception e)
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
	
	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents)
	{
		if(!isBusy)
		{
			try
			{
				isBusy = true;
				Thread.sleep(20);
				Transferable content = clipboard.getContents(this);
				prev = content;
				new SyncClient("send", content).start();
				clipboard.setContents(content, this);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	@Override
	protected void finalize() throws Throwable
	{
		new SyncClient("disconnect", null).start();
		hook.shutdownHook();
		server.interrupt();
		super.finalize();
	}
}
