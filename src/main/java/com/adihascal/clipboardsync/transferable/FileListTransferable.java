package com.adihascal.clipboardsync.transferable;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class FileListTransferable implements Transferable
{
	private List<File> data;
	
	public FileListTransferable(List<File> files)
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
		return flavor.equals(DataFlavor.javaFileListFlavor);
	}
	
	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException
	{
		return data;
	}
}
