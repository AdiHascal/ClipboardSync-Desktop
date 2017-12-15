package com.adihascal.clipboardsync.handler;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.EnumMap;
import java.util.Map;

@SuppressWarnings("UnusedReturnValue")
public class GuiHandler
{
	private Parent root;
	private boolean init = false;
	
	public void setup(Stage primaryStage, Parent parent)
	{
		if(!init)
		{
			this.root = parent;
			setImage();
			primaryStage.setScene(new Scene(root));
			primaryStage.setTitle("ClipboardSync");
			primaryStage.setResizable(false);
			primaryStage.show();
			init = true;
		}
		else
		{
			try
			{
				throw new Exception("you can't setup the gui twice");
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private void setImage()
	{
		try
		{
			ByteArrayOutputStream imageOut = new ByteArrayOutputStream();
			ImageIO.write(getQrCode(), "png", imageOut);
			ImageView qrImage = (ImageView) this.root.lookup("#qrCode");
			qrImage.setImage(new Image(new ByteArrayInputStream(imageOut.toByteArray())));
		}
		catch(IOException | WriterException e)
		{
			e.printStackTrace();
		}
	}
	
	private BufferedImage getQrCode() throws UnknownHostException, WriterException
	{
		Map<EncodeHintType, Object> hintMap = new EnumMap<>(EncodeHintType.class);
		hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");
		hintMap.put(EncodeHintType.MARGIN, 1);
		hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
		
		BitMatrix matrix = new QRCodeWriter()
				.encode(InetAddress.getLocalHost().getHostName() + "," + InetAddress.getLocalHost()
						.getHostAddress(), BarcodeFormat.QR_CODE, 250, 250, hintMap);
		BufferedImage image = new BufferedImage(matrix.getWidth(), matrix.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics graphics = image.getGraphics();
		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, matrix.getWidth(), matrix.getHeight());
		graphics.setColor(Color.BLACK);
		
		for(int i = 0; i < matrix.getWidth(); i++)
		{
			for(int j = 0; j < matrix.getWidth(); j++)
			{
				if(matrix.get(i, j))
				{
					graphics.fillRect(i, j, 1, 1);
				}
			}
		}
		return image;
	}
	
	public GuiHandler resetPhoneName()
	{
		setPhoneName("Nothing");
		return this;
	}
	
	public GuiHandler setPhoneName(String name)
	{
		((Text) root.lookup("#phoneName")).setText(name);
		return this;
	}
	
	public GuiHandler setStatus(ProgramState state)
	{
		((Text) this.root.lookup("#status")).setText(state.getDisplayName());
		return this;
	}
	
	public enum ProgramState
	{
		DISCONNECTED("Disconnected"),
		IDLE("Idle"),
		SENDING_FILES("Sending files"),
		RECEIVING_FILES("Receiving files"),
		STANDBY("Standby"),
		WAITING_FOR_RESPONSE("Waiting for a response");
		private final String displayName;
		
		ProgramState(String displayName)
		{
			this.displayName = displayName;
		}
		
		public String getDisplayName()
		{
			return displayName;
		}
	}
}
