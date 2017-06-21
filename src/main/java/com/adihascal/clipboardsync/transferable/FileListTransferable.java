package com.adihascal.clipboardsync.transferable;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class FileListTransferable implements Transferable
{
	private File[] data;
	
	public FileListTransferable(File[] files)
	{
		this.data = files;
	}
	
	@Override
	public DataFlavor[] getTransferDataFlavors()
	{
		return new DataFlavor[]{DataFlavor.javaFileListFlavor};
	}
	
	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor)
	{
		return flavor == DataFlavor.javaFileListFlavor;
	}
	
	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException
	{
		if(!isDataFlavorSupported(flavor))
		{
			throw new UnsupportedFlavorException(flavor);
		} else
		{
			return Arrays.asList(this.data);
		}
	}
}
