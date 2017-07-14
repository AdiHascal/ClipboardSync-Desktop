package com.adihascal.clipboardsync.util;

import java.io.*;

public class Utilities
{
	public static final int BUFFER_SIZE = 1048576;
	
	public static void copyStreamToFile(InputStream input, File f) throws IOException
	{
		if(!f.exists())
		{
			f.createNewFile();
		}
		copyStream(input, new FileOutputStream(f));
	}
	
	public static void copyStream(InputStream input, OutputStream output) throws IOException
	{
		byte[] buffer = new byte[BUFFER_SIZE];
		int bytesRead;
		while((bytesRead = input.read(buffer)) != -1)
		{
			output.write(buffer, 0, bytesRead);
		}
		output.close();
	}
	
	public static void copyStream(InputStream input, OutputStream output, int length) throws IOException
	{
		byte[] buffer = new byte[BUFFER_SIZE];
		int bytesRead;
		int totalBytesRead = 0;
		while(totalBytesRead < length && (bytesRead = input
				.read(buffer, 0, Math.min(length - totalBytesRead, BUFFER_SIZE))) != -1)
		{
			totalBytesRead += bytesRead;
			output.write(buffer, 0, bytesRead);
		}
		output.close();
	}
}
