package com.adihascal.clipboardsync.handler;

import java.awt.datatransfer.DataFlavor;
import java.util.HashMap;

public class ClipHandlerRegistry
{
	private static final HashMap<String, IClipHandler> handlers = new HashMap<>();
	private static TextHandler textHandler = new TextHandler();
	private static IntentHandler intentHandler = new IntentHandler();
	
	static
	{
		handlers.put("text/plain", textHandler);
		handlers.put("application/x-java-serialized-object", intentHandler);
		handlers.put("fucking retards", intentHandler);
		handlers.put(DataFlavor.stringFlavor.getMimeType(), textHandler);
		handlers.put(DataFlavor.allHtmlFlavor.getMimeType(), textHandler);
		handlers.put(DataFlavor.fragmentHtmlFlavor.getMimeType(), textHandler);
		handlers.put(DataFlavor.selectionHtmlFlavor.getMimeType(), textHandler);
		handlers.put(DataFlavor.javaFileListFlavor.getMimeType(), intentHandler);
	}
	
	public static IClipHandler getHandlerFor(String type)
	{
		return handlers.get(type);
	}
	
	public static DataFlavor getSuitableFlavor(DataFlavor[] types)
	{
		for(DataFlavor df : types)
		{
			if(handlers.containsKey(df.getMimeType()))
			{
				return df;
			}
		}
		return null;
	}
}