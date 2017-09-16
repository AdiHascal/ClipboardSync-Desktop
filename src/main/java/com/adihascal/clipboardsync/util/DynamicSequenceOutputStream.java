package com.adihascal.clipboardsync.util;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class DynamicSequenceOutputStream extends OutputStream
{
	private final IStreamSupplier<OutputStream> supplier;
	private int streamIndex = -1;
	private int count, pos;
	private OutputStream out;
	
	public DynamicSequenceOutputStream(IStreamSupplier<OutputStream> supp)
	{
		this.supplier = supp;
	}
	
	@Override
	public void write(int b) throws IOException
	{
		if(pos < count)
		{
			pos++;
			out.write(b);
		}
		else
		{
			next(false);
			out.write(b);
		}
	}
	
	private void next(boolean close)
	{
		try
		{
			if(out != null)
			{
				out.close();
				supplier.afterClose(streamIndex);
			}
			
			if(!close)
			{
				FileOutputStream fIn = (FileOutputStream) supplier.next(streamIndex++);
				pos = 0;
				count = 15728640;
				out = new BufferedOutputStream(fIn, 15360);
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public void close() throws IOException
	{
		if(out != null)
		{
			next(true);
		}
	}
}
