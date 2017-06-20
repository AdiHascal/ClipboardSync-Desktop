package com.adihascal.clipboardsync.handler;

import java.awt.datatransfer.DataFlavor;
import java.util.HashMap;

public class ClipHandlerRegistry
{
	private static final HashMap<String, IClipHandler> handlers = new HashMap<>();
	
	static
	{
		handlers.put(DataFlavor.stringFlavor.getMimeType(), new TextHandler());
		handlers.put(DataFlavor.allHtmlFlavor.getMimeType(), new TextHandler());
		handlers.put(DataFlavor.javaSerializedObjectMimeType, new IntentHandler());
	}
	
	public static IClipHandler getHandlerFor(String type)
	{
		return handlers.get(type);
	}
	
	public static boolean isMimeTypeSupported(String type)
	{
		return handlers.containsKey(type);
	}
}