package com.github.zzwwws.plugig.compatible;

import android.content.Context;

public abstract class ClazzEx extends Clazz {
	public ClazzEx(String name) {
		super(name);
	}
	
	public final <T> T newInstanceEx(Context context) {
		if (!loaded()) {
			try {
				load(getClassLoader(context));
			} catch (ClazzException e) {
				e.printStackTrace();
			}
		}

		try {
			return newInstance();
		} catch (ClazzException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public abstract ClassLoader getClassLoader(Context context);
}
