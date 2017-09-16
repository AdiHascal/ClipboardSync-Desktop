package com.adihascal.clipboardsync.tasks;

import com.adihascal.clipboardsync.Main;
import com.adihascal.clipboardsync.util.DynamicSequenceOutputStream;
import com.adihascal.clipboardsync.util.IStreamSupplier;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.adihascal.clipboardsync.network.SocketHolder.in;
import static com.adihascal.clipboardsync.network.SocketHolder.out;

@SuppressWarnings({"ConstantConditions", "ResultOfMethodCallIgnored", "SpellCheckingInspection"})
public class SendTask implements ITask, IStreamSupplier<OutputStream>
{
	private static final File dataDir = new File(Main.localFolder, "chunks");
	private static final int chunkSize = 15728640;
	private List<File> files;
	private List<File> binFiles;
	private LinkedList<Object> objectsToSend = new LinkedList<>();
	private DynamicSequenceOutputStream stream;
	private int currentChunk = 0;
	
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
	
	public synchronized void execute()
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
			
			int nChunks = (int) Math.ceil((double) length / chunkSize);
			
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
			
			stream = new DynamicSequenceOutputStream(this);
			for(Object o : objectsToSend)
			{
				writeObject(o);
			}
			stream.close();
			finish();
		}
		catch(IOException e)
		{
			e.printStackTrace();
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
	
	private void writeObject(Object o) throws IOException
	{
		if(!(o instanceof File))
		{
			stream.write(convertToBytes(o));
		}
		else
		{
			FileInputStream in = new FileInputStream((File) o);
			byte[] tempBuffer = new byte[chunkSize / 1024];
			while(in.read(tempBuffer, 0, tempBuffer.length) != -1)
			{
				stream.write(tempBuffer);
			}
		}
	}
	
	@Override
	public OutputStream next(int prevIndex)
	{
		try
		{
			return new FileOutputStream(binFiles.get(++prevIndex));
		}
		catch(FileNotFoundException e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public boolean canProvide(int index)
	{
		return index < currentChunk;
	}
	
	@Override
	public void afterClose(int index)
	{
		afterClose(index, false);
	}
	
	private synchronized void afterClose(int index, boolean recursive)
	{
		try
		{
			byte[] buffer = new byte[chunkSize / 1024];
			int bytesRead;
			RandomAccessFile input = new RandomAccessFile(binFiles.get(index), "rw");
			if(!recursive)
			{
				currentChunk++;
			}
			else
			{
				input.seek(in().readLong());
			}
			
			while((bytesRead = input.read(buffer)) != -1)
			{
				out().write(buffer, 0, bytesRead);
			}
			input.close();
			Files.delete(binFiles.get(index).toPath());
		}
		catch(IOException e)
		{
			try
			{
				wait(30000);
				afterClose(index, true);
				e.printStackTrace();
			}
			catch(InterruptedException e1)
			{
				e1.printStackTrace();
			}
		}
	}
}
