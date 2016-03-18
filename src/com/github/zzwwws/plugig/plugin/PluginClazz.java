package com.github.zzwwws.plugig.plugin;

import android.content.Context;

import com.github.zzwwws.plugig.compatible.ClazzEx;


public class PluginClazz extends ClazzEx {
	private final String pName;

	public PluginClazz(Context context, String pName) {
		super(className(context, pName));
	
		this.pName = pName;
	}

	@Override
	public ClassLoader getClassLoader(Context context) {
		return context.getClassLoader();
	}
	
	private static String className(Context context, String pName) {
		return context.getPackageName() + "." + "plugin" + "." +  pName + "." +pName+ "Plugin";
	}
}
