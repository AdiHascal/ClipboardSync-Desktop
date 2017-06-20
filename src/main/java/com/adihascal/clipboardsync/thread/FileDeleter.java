package com.adihascal.clipboardsync.thread;

import java.io.File;

public class FileDeleter implements Runnable
{
	File dir;
	
	public FileDeleter(File f)
	{
		this.dir = f;
	}
	
	@Override
	public void run()
	{
		File[] files = dir.listFiles();
		
		if(files != null)
		{
			for(File f : files)
			{
				f.delete();
			}
		}
	}
}
