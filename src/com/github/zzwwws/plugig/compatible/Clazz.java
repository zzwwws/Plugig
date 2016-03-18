package com.github.zzwwws.plugig.compatible;

import android.util.Log;

public class Clazz {
	private static final String TAG = "CLAZZ";
	
	public static final class ClazzException extends Exception {
		private static final long serialVersionUID = -6296885561482884182L;

		public ClazzException() {
	    }

	    public ClazzException(String detailMessage) {
	        super(detailMessage);
	    }

	    public ClazzException(String detailMessage, Throwable throwable) {
	        super(detailMessage, throwable);
	    }

	    public ClazzException(Throwable throwable) {
	        super(throwable);
	    }
	}
	
	private final String name;

	protected Class<?> clazz;

	public Clazz(String name) {
		this.name = name;
	}
	
	public final boolean loaded() {
		return clazz != null;
	}
	
	public final void load(ClassLoader cl) throws ClazzException {
		if (cl != null) {
			try {
				clazz = cl.loadClass(name);
			} catch (ClassNotFoundException ex) {
				ex.printStackTrace();
				
				throwsException("load", ex);
			} catch (Throwable tr) {
				tr.printStackTrace();
				
				throwsException("unexpected", tr);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public final <T> T newInstance() throws ClazzException {
		if (clazz != null) {
			try {
				return (T) clazz.newInstance();
			} catch (InstantiationException ex) {
				ex.printStackTrace();
				
				throwsException("instantiate", ex);
			} catch (IllegalAccessException ex) {
				ex.printStackTrace();
				
				throwsException("access default constructor", ex);
			} catch (ClassCastException ex) {
				ex.printStackTrace();
				
				throwsException("cast class", ex);
			} catch (Throwable tr) {
				tr.printStackTrace();
				
				throwsException("unexpected", tr);
			}
		}
		
		return null;
	}
	
	protected final void throwsException(String what, Throwable cause) throws ClazzException {
		String msg = what + 
				" class " + name + 
				" cause " + cause;
	
		Log.e(TAG, msg);
		
		throw new ClazzException(msg, cause);
	}
}