package com.adihascal.clipboardsync.util;

import java.io.DataOutputStream;
import java.io.OutputStream;

public class ExposedDataOutputStream extends DataOutputStream
{
	public ExposedDataOutputStream(OutputStream out)
	{
		super(out);
	}
	
	public OutputStream getOut()
	{
		return super.out;
	}
}
