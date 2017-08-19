package com.adihascal.clipboardsync.tasks;

import com.adihascal.clipboardsync.Main;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.adihascal.clipboardsync.network.SocketHolder.out;

@SuppressWarnings({"ConstantConditions", "ResultOfMethodCallIgnored", "SpellCheckingInspection"})
public class SendTask implements ITask
{
	private static final File dataDir = new File(Main.localFolder, "chunks");
	private static final int chunkSize = 15728640;
	private List<File> files;
	private List<File> binFiles;
	private LinkedList<Object> objectsToSend = new LinkedList<>();
	private DataOutputStream currentStream;
	private int currentChunk = 0;
	private int nChunks;
	
	public SendTask(List<File> uList)
	{
		if(!dataDir.exists())
		{
			dataDir.mkdir();
		}
		this.files = uList;
	}
	
	private void addToList(File f)
	{
		if(!f.isDirectory())
		{
			objectsToSend.add("file");
			objectsToSend.add(f.getName());
			objectsToSend.add(f.length());
			objectsToSend.add(f);
		}
		else
		{
			objectsToSend.add("dir");
			File[] subs = f.listFiles();
			assert subs != null;
			objectsToSend.add(f.getName());
			objectsToSend.add(subs.length);
			for(File sub : subs)
			{
				addToList(sub);
			}
		}
	}
	
	private long getObjectLength(Object o)
	{
		if(o instanceof String)
		{
			return getUTFLength((String) o);
		}
		else if(o instanceof Long)
		{
			return 8;
		}
		else if(o instanceof Integer)
		{
			return 4;
		}
		else if(o instanceof File)
		{
			return ((File) o).length();
		}
		else
		{
			return 0;
		}
	}
	
	private int getUTFLength(String str)
	{
		int len = 0;
		
		for(char c : str.toCharArray())
		{
			if((c >= 0x0001) && (c <= 0x007F))
			{
				len++;
			}
			else if(c > 0x07FF)
			{
				len += 3;
			}
			else
			{
				len += 2;
			}
		}
		return len;
	}
	
	private void writeObject(Object o) throws IOException
	{
		int bytesWritten;
		long bytesRemaining;
		byte[] tempBuffer;
		
		if(!(o instanceof File))
		{
			tempBuffer = convertToBytes(o);
			bytesRemaining = tempBuffer.length;
			bytesWritten = writeBytes(tempBuffer, 0, tempBuffer.length);
			bytesRemaining -= bytesWritten;
			
			if(bytesRemaining > 0)
			{
				nextStream();
				writeBytes(tempBuffer, bytesWritten, (int) Math.min(tempBuffer.length - bytesWritten, bytesRemaining));
			}
		}
		else
		{
			bytesRemaining = ((File) o).length();
			FileInputStream in = new FileInputStream((File) o);
			
			tempBuffer = new byte[chunkSize / 1024];
			int bytesRead;
			
			while((bytesRead = in.read(tempBuffer, 0, tempBuffer.length)) != -1)
			{
				bytesWritten = writeBytes(tempBuffer, 0, bytesRead);
				bytesRemaining -= bytesWritten;
				
				if(getFreeSpace() == 0 && bytesRemaining > 0)
				{
					nextStream();
					writeBytes(tempBuffer, bytesWritten, (int) Math
							.min(tempBuffer.length - bytesWritten, bytesRemaining));
				}
			}
		}
	}
	
	private byte[] convertToBytes(Object o)
	{
		switch(o.getClass().getSimpleName())
		{
			case "String":
				return convertStringToUTFBytes((String) o);
			case "Integer":
				return convertIntToBytes((Integer) o);
			case "Long":
				return convertLongToBytes(((Long) o));
		}
		return new byte[0];
	}
	
	private int writeBytes(byte[] b, int off, int len) throws IOException
	{
		int ret = Math.min(getFreeSpace(), len);
		currentStream.write(b, off, ret);
		return ret;
	}
	
	private byte[] convertLongToBytes(long val)
	{
		byte[] writeBuffer = new byte[8];
		writeBuffer[0] = (byte) (val >>> 56);
		writeBuffer[1] = (byte) (val >>> 48);
		writeBuffer[2] = (byte) (val >>> 40);
		writeBuffer[3] = (byte) (val >>> 32);
		writeBuffer[4] = (byte) (val >>> 24);
		writeBuffer[5] = (byte) (val >>> 16);
		writeBuffer[6] = (byte) (val >>> 8);
		writeBuffer[7] = (byte) val;
		
		return writeBuffer;
	}
	
	private byte[] convertIntToBytes(int val)
	{
		byte[] buf = new byte[4];
		buf[0] = (byte) ((val >>> 24) & 0xFF);
		buf[1] = (byte) ((val >>> 16) & 0xFF);
		buf[2] = (byte) ((val >>> 8) & 0xFF);
		buf[3] = (byte) (val & 0xFF);
		
		return buf;
	}
	
	private int getFreeSpace()
	{
		return chunkSize - currentStream.size();
	}
	
	private byte[] convertStringToUTFBytes(String str)
	{
		int strlen = str.length();
		int utflen = getUTFLength(str);
		int c, count = 0;
		
		byte[] bytearr = new byte[utflen + 2];
		bytearr[count++] = (byte) ((utflen >>> 8) & 0xFF);
		bytearr[count++] = (byte) (utflen & 0xFF);
		
		int i;
		for(i = 0; i < strlen; i++)
		{
			c = str.charAt(i);
			if(!((c >= 0x0001) && (c <= 0x007F)))
			{
				break;
			}
			bytearr[count++] = (byte) c;
		}
		
		for(; i < strlen; i++)
		{
			c = str.charAt(i);
			if((c >= 0x0001) && (c <= 0x007F))
			{
				bytearr[count++] = (byte) c;
				
			}
			else if(c > 0x07FF)
			{
				bytearr[count++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
				bytearr[count++] = (byte) (0x80 | ((c >> 6) & 0x3F));
				bytearr[count++] = (byte) (0x80 | (c & 0x3F));
			}
			else
			{
				bytearr[count++] = (byte) (0xC0 | ((c >> 6) & 0x1F));
				bytearr[count++] = (byte) (0x80 | (c & 0x3F));
			}
		}
		return bytearr;
	}
	
	private void nextStream()
	{
		try
		{
			currentStream.flush();
			currentStream.close();
			currentChunk++;
			if(currentChunk < nChunks)
			{
				currentStream = newStream(currentChunk);
			}
			byte[] buffer = new byte[chunkSize / 1024];
			int bytesRead;
			FileInputStream input = new FileInputStream(binFiles.get(currentChunk - 1));
			while((bytesRead = input.read(buffer)) != -1)
			{
				out().write(buffer, 0, bytesRead);
			}
			input.close();
			onChunkSent();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private DataOutputStream newStream(int i) throws FileNotFoundException
	{
		return new DataOutputStream((new FileOutputStream(binFiles.get(i))));
	}
	
	private void onChunkSent()
	{
		if(currentChunk > 1)
		{
			File f = binFiles.get(currentChunk - 2);
			if(f.exists())
			{
				f.delete();
			}
		}
	}
	
	public void execute()
	{
		try
		{
			for(File bin : dataDir.listFiles())
			{
				if(bin.getName().endsWith(".bin"))
				{
					bin.delete();
				}
			}
			
			for(File f : files)
			{
				addToList(f);
			}
			
			long length = 0L;
			for(Object o : objectsToSend)
			{
				length += getObjectLength(o);
			}
			
			nChunks = (int) Math.ceil((double) length / chunkSize);
			
			binFiles = new ArrayList<>(files.size());
			for(int i = 0; i < nChunks; i++)
			{
				File f = new File(dataDir, Integer.toString(i) + ".bin");
				f.createNewFile();
				binFiles.add(i, f);
			}
			
			out().writeUTF("application/x-java-serialized-object");
			out().writeLong(length);
			out().writeInt(files.size());
			
			currentStream = newStream(0);
			for(Object o : objectsToSend)
			{
				writeObject(o);
			}
			nextStream();
			finish();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}
