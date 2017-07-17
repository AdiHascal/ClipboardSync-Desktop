package com.adihascal.clipboardsync.util;

import java.io.*;

public class Utilities
{
	public static final int BUFFER_SIZE = 1048576;
	
	/**
	 * overload of {@link #copyStream(InputStream, OutputStream)} that removes the need to create a {@link FileOutputStream}
	 *
	 * @param input the origin stream
	 * @param f     the destination file
	 * @throws IOException if something explodes
	 */
	public static void copyStream(InputStream input, File f) throws IOException
	{
		if(!f.exists())
		{
			f.createNewFile();
		}
		copyStream(input, new FileOutputStream(f));
	}
	
	/**
	 * copies all the data present in <code>input</code> to <code>output</code>
	 * @param input the origin stream
	 * @param output the destination stream
	 * @throws IOException if something explodes
	 */
	public static void copyStream(InputStream input, OutputStream output) throws IOException
	{
		byte[] buffer = new byte[BUFFER_SIZE];
		int bytesRead;
		while((bytesRead = input.read(buffer)) != -1)
		{
			output.write(buffer, 0, bytesRead);
		}
	}
	
	/**
	 * copies <code>length</code> bytes from <code>input</code> to <code>output</code>
	 * @param input the origin stream
	 * @param output the destination stream
	 * @param length number of bytes to copy
	 * @throws IOException if something explodes
	 */
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
