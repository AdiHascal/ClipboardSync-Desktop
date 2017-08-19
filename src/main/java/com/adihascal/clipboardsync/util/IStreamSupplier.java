package com.adihascal.clipboardsync.util;

import java.io.InputStream;

public interface IStreamSupplier
{
	InputStream next(int prevIndex);
	
	boolean canProvide(int index);
}
