package com.github.zzwwws.plugig.compatible;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

public class ContextClassLoader extends ClassLoader {
    private static final String TAG = "ContextClassLoader";
    private static final String JAR = ".jar";
    private static ContextClassLoader ccl;
    private Context context;

    private ClassLoader cl;

    private ContextClassLoader(Context context) {
        super(context.getClassLoader().getParent());

        this.context = context;
        this.cl = context.getClassLoader();
    }

    public static final ClassLoader init(Context context) {
        ccl = new ContextClassLoader(context);

        try {
            Loader.replaceCtxCL(context, ccl);
        } catch (LoaderException ex) {
            ex.printStackTrace();
        }
        return ccl;
    }

    @Override
    protected Class<?> findClass(String className) throws ClassNotFoundException {
        try {
            return cl.loadClass(className);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();

            String jarName = jarOfClass(className);
            if (!TextUtils.isEmpty(jarName)) {
                load(jarName + JAR);

                return cl.loadClass(className);
            }

            throw ex;
        }
    }

    private void load(String name) {
        Log.d(TAG, "load: " + name);

        try {
            cl = Loader.load(context, cl, name);
        } catch (LoaderException ex) {
            ex.printStackTrace();
        }
    }

    private String jarOfClass(String className) {
        if (className.startsWith("com.github.zzwwws.plugig.plugin")) {
            String[] parts = className.split("\\.");
            if (parts.length > 6) {
                String plugin = parts[5];
                if (!TextUtils.isEmpty(plugin)) {
                    return plugin;
                }
            }
        }
        return null;
    }
}