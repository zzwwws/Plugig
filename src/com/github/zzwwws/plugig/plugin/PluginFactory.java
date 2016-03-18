package com.github.zzwwws.plugig.plugin;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

public class PluginFactory {
	private static final Map<String, PluginClazz> clazzes = new HashMap<String, PluginClazz>();
	
	public static final IPlugin create(Context context, String name) {
		PluginClazz clazz = clazzes.get(name);
		if (clazz == null) {
			clazz = new PluginClazz(context, name);
			clazzes.put(name, clazz);
		}
		
		return clazz.newInstanceEx(context);
	}
}
