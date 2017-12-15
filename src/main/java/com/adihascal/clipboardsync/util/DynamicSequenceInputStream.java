package com.adihascal.clipboardsync.util;

import java.io.*;

public class DynamicSequenceInputStream extends InputStream implements DataInput
{
	private final IStreamSupplier<InputStream> supplier;
	private InputStream in;
	private int read = 0;
	private int streamIndex = 0;
	
	public DynamicSequenceInputStream(IStreamSupplier<InputStream> supp)
	{
		this.supplier = supp;
		next(false);
	}
	
	public int read() throws IOException
	{
		int i = in.read();
		if(i == -1)
		{
			System.out.println(read + " bytes read from stream " + (streamIndex - 1));
			next(false);
			return read();
		}
		read++;
		return i;
	}
	
	@Override
	public void readFully(byte[] b) throws IOException
	{
		readFully(b, 0, b.length);
	}
	
	public int read(byte[] b, int off, int len) throws IOException
	{
		if(off < 0 || len < 0 || len > b.length - off)
		{
			throw new IndexOutOfBoundsException();
		}
		else if(len == 0)
		{
			return 0;
		}
		
		int c = read();
		if(c == -1)
		{
			return -1;
		}
		b[off] = (byte) c;
		
		int i = 1;
		try
		{
			for(; i < len; i++)
			{
				c = read();
				if(c == -1)
				{
					break;
				}
				b[off + i] = (byte) c;
			}
		}
		catch(IOException ignored)
		{
		}
		return i;
	}
	
	@Override
	public void close()
	{
		if(in != null)
		{
			System.out.println(read + " bytes read from stream " + (streamIndex - 1));
			next(true);
		}
	}
	
	private void next(boolean close)
	{
		try
		{
			if(in != null)
			{
				in.close();
				supplier.afterClose(streamIndex - 1);
			}
			
			if(!close)
			{
				while(true)
				{
					if(supplier.canProvide(streamIndex))
					{
						read = 0;
						InputStream next = supplier.next(streamIndex++);
						in = new BufferedInputStream(next, 61440);
						break;
					}
				}
			}
			else
			{
				System.out.println("closing");
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public void readFully(byte[] b, int off, int len) throws IOException
	{
		int n = 0;
		do
		{
			int count = this.read(b, off + n, len - n);
			if(count < 0)
			{
				throw new EOFException();
			}
			n += count;
		} while(n < len);
	}
	
	@Override
	public int skipBytes(int n) throws IOException
	{
		int total = 0;
		int cur;
		
		while((total < n) && ((cur = (int) in.skip(n - total)) > 0))
		{
			total += cur;
		}
		
		return total;
	}
	
	@Override
	public boolean readBoolean() throws IOException
	{
		int ch = this.read();
		if(ch < 0)
		{
			throw new EOFException();
		}
		return (ch != 0);
	}
	
	@Override
	public byte readByte() throws IOException
	{
		int ch = this.read();
		if(ch < 0)
		{
			throw new EOFException();
		}
		return (byte) (ch);
	}
	
	@Override
	public int readUnsignedByte() throws IOException
	{
		int ch = this.read();
		if(ch < 0)
		{
			throw new EOFException();
		}
		return ch;
	}
	
	public short readShort() throws IOException
	{
		int ch1 = this.read();
		int ch2 = this.read();
		if((ch1 | ch2) < 0)
		{
			throw new EOFException();
		}
		return (short) ((ch1 << 8) + (ch2));
	}
	
	public int readUnsignedShort() throws IOException
	{
		int ch1 = this.read();
		int ch2 = this.read();
		if((ch1 | ch2) < 0)
		{
			throw new EOFException();
		}
		return (ch1 << 8) + (ch2);
	}
	
	public char readChar() throws IOException
	{
		int ch1 = this.read();
		int ch2 = this.read();
		if((ch1 | ch2) < 0)
		{
			throw new EOFException();
		}
		return (char) ((ch1 << 8) + (ch2));
	}
	
	public int readInt() throws IOException
	{
		int ch1 = this.read();
		int ch2 = this.read();
		int ch3 = this.read();
		int ch4 = this.read();
		if((ch1 | ch2 | ch3 | ch4) < 0)
		{
			throw new EOFException();
		}
		return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4));
	}
	
	public long readLong() throws IOException
	{
		return ((long) (readInt()) << 32) + (readInt() & 0xFFFFFFFFL);
	}
	
	public float readFloat() throws IOException
	{
		return Float.intBitsToFloat(readInt());
	}
	
	public double readDouble() throws IOException
	{
		return Double.longBitsToDouble(readLong());
	}
	
	public String readLine()
	{
		return null;
	}
	
	@Override
	public String readUTF() throws IOException
	{
		return DataInputStream.readUTF(this);
	}
}
