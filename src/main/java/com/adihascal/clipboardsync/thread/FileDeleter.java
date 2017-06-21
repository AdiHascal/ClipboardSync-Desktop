package com.adihascal.clipboardsync.thread;

import java.io.File;

public class FileDeleter implements Runnable
{
	private File[] files;
	
	public FileDeleter(File f)
	{
		this.files = f.listFiles();
	}
	
	@Override
	public void run()
	{
		for(File f : files)
		{
			f.delete();
		}
	}
}
