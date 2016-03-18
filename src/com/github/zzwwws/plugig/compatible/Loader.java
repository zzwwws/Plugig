package com.github.zzwwws.plugig.compatible;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.github.zzwwws.plugig.util.MD5;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Properties;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

final class Loader {
    private static final String TAG = "Loader";

    private static final String PRELOAD = "preload";

    private static final String SUFFIX_VERIFY = ".vfy";

    private static final String PROPERTY_MD5 = "MD5";

    private static final String PROPERTY_FOO = "FOO";

    private static final String APP_CACHE_DIR = "/data/data/com.github.zzwwws.plugig/cache";
    private static final String APP_LIB_DIR = "/data/data/com.github.zzwwws.plugig/lib";
    private static final String APP_DEX_DIR = "/data/data/com.github.zzwwws.plugig/dex";


    public static final ClassLoader load(Context context, ClassLoader parent, String name) throws LoaderException {
        // MON
        LoaderWatchDog.onLoadStart(name);

        ClassLoader cl = new DexClassLoader(install(context, name), APP_CACHE_DIR, APP_LIB_DIR, parent);

        LoaderWatchDog.onLoadFinish(name);

        return cl;
    }

    public static final void load(Context context, String name) throws LoaderException {
        // MON
        LoaderWatchDog.onLoadStart(name);

        replaceCtxCL(context, new DexClassLoader(install(context, name), APP_CACHE_DIR, APP_LIB_DIR, context.getClassLoader()));

        LoaderWatchDog.onLoadFinish(name);
    }

    public static final void load2(Context context, String name) throws LoaderException {
        ClassLoader apk = context.getClassLoader();
        ClassLoader fx = apk.getParent();
        ClassLoader cl = new DexClassLoader(install(context, name), APP_CACHE_DIR, APP_LIB_DIR, fx);
        replaceParentCL(apk, cl);
    }

    public static final void load3(Context context, String name) throws LoaderException {
        Properties vfy = new Properties();

        String path = install(context, name, vfy);
        String foo = vfy.getProperty(PROPERTY_FOO);

        Injector.inject(context, path, foo);
    }

    private static final void replaceParentCL(ClassLoader cl, ClassLoader parent) throws LoaderException {
        try {
            (new FieldAccessor(cl, "parent", null)).set(parent);
        } catch (Exception e) {
            String msg = "unable to replace PARENT CL " + e;
            Log.e(TAG, msg);
            throw new LoaderException(msg, e);
        }
    }

    /*package*/
    static final void replaceCtxCL(Context context, ClassLoader cl) throws LoaderException {
        context = context.getApplicationContext();

        try {
            // mBase
            Object mBase = (new FieldAccessor(context, "mBase", null)).get();

            // mPackageInfo
            Object mPackageInfo = (new FieldAccessor(mBase, "mPackageInfo", null)).get();

            // mClassLoader
            (new FieldAccessor(mPackageInfo, "mClassLoader", null)).set(cl);
        } catch (Exception e) {
            String msg = "unable to replace CTX CL " + e;
            Log.e(TAG, msg);
            throw new LoaderException(msg, e);
        }
    }

    private static final String install(Context context, String name) throws LoaderException {
        return install(context, name, new Properties());
    }

    private static final String install(Context context, String name, Properties vfy) throws LoaderException {
        getVerify(context, name, vfy);

        String md5 = vfy.getProperty(PROPERTY_MD5);
        if (TextUtils.isEmpty(md5)) {
            String msg = "unable to verify " + name;
            Log.e(TAG, msg);
            throw new LoaderException(msg);
        }

        return install(context, PRELOAD + "/" + name, APP_DEX_DIR + "/" + name, md5);
    }

    private static final Properties getVerify(Context context, String name, Properties vfy) {
        try {
            InputStream is = context.getAssets().open(PRELOAD + "/" + name + SUFFIX_VERIFY);
            vfy.load(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return vfy;
    }

    private static final String install(Context context, String src, String dest, String md5) throws LoaderException {
        // ensure APP DEX DIR
        {
            File file = new File(APP_DEX_DIR);
            if ((!file.exists() || !file.isDirectory()) && !file.mkdirs()) {
                String msg = "unable to create app dex dir";
                Log.e(TAG, msg);
                throw new LoaderException(msg);
            }
        }

        // TRACE
        Log.d(TAG, "install " + src + " to " + dest);

        // CHECK
        if (new File(dest).exists()) {
            String vfy = MD5.getStreamMD5(dest);
            if (!TextUtils.isEmpty(md5) && !TextUtils.isEmpty(vfy) && vfy.compareToIgnoreCase(md5) == 0) {
                return dest;
            } else {
                // TRACE
                Log.w(TAG, "verify fail " + md5 + " != " + vfy);

                new File(dest).delete();
            }
        }

        // EXTRACT
        extract(context, src, dest);

        return dest;
    }

    private static final void extract(Context context, String src, String dest) throws LoaderException {
        InputStream is = null;
        OutputStream os = null;
        try {
            try {
                is = new BufferedInputStream(context.getAssets().open(src));
            } catch (IOException e) {
                String msg = "unable to open src " + e;
                Log.e(TAG, msg);
                throw new LoaderException(msg, e);
            }

            try {
                os = new BufferedOutputStream(new FileOutputStream(dest));
            } catch (FileNotFoundException e) {
                String msg = "unable to create dest " + e;
                Log.e(TAG, msg);
                throw new LoaderException(msg, e);
            }

            try {
                byte[] buffer = new byte[8192];
                int read = 0;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            } catch (IOException e) {
                String msg = "unable to write dest " + e;
                Log.e(TAG, msg);
                throw new LoaderException(msg, e);
            }
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static final class Injector {
        private static final String TAG = "Loader";

        public static void inject(Context context, String dexPath, String foo) throws LoaderException {
            try {
                Class.forName("dalvik.system.LexClassLoader");

                injectInAliyunOs(context, dexPath, foo);
            } catch (ClassNotFoundException ex) {
                boolean flag = false;
                try {
                    Class.forName("dalvik.system.BaseDexClassLoader");
                    flag = true;
                } catch (ClassNotFoundException e) {

                }
                if (flag) {
                    injectAboveEqualApiLevel14(context, dexPath);
                } else {
                    injectBelowApiLevel14(context, dexPath, foo);
                }
            }
        }

        private static Object getDexElements(Object obj) throws NoSuchFieldException, IllegalAccessException {
            return getField(obj, obj.getClass(), "dexElements");
        }

        private static Object getPathList(Object obj) throws NoSuchFieldException, IllegalAccessException {
            try {
                return getField(obj, Class.forName("dalvik.system.BaseDexClassLoader"), "pathList");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            return null;
        }

        private static void injectAboveEqualApiLevel14(Context context, String dexPath) throws LoaderException {
            ClassLoader parent = context.getClassLoader();
            ClassLoader cl = new DexClassLoader(dexPath, APP_CACHE_DIR, dexPath, parent);

            try {
                Object dexElements = combineArray(getDexElements(getPathList(parent)), getDexElements(getPathList(cl)));

                Object obj = getPathList(parent);
                setField(obj, obj.getClass(), "dexElements", dexElements);
            } catch (Throwable tr) {
                String msg = "unable to inject API >= 14 " + tr;
                Log.e(TAG, msg);
                throw new LoaderException(msg, tr);
            }
        }

        private static void injectBelowApiLevel14(Context context, String dexPath, String foo) throws LoaderException {
            ClassLoader parent = context.getClassLoader();
            ClassLoader cl = new DexClassLoader(dexPath, APP_CACHE_DIR, dexPath, parent);
            try {
                cl.loadClass(foo);

                setField(parent, PathClassLoader.class, "mPaths", appendArray(getField(parent, PathClassLoader.class, "mPaths"), getField(cl, DexClassLoader.class, "mRawDexPath")));
                setField(parent, PathClassLoader.class, "mFiles", combineArray(getField(parent, PathClassLoader.class, "mFiles"), getField(cl, DexClassLoader.class, "mFiles")));
                setField(parent, PathClassLoader.class, "mZips", combineArray(getField(parent, PathClassLoader.class, "mZips"), getField(cl, DexClassLoader.class, "mZips")));
                setField(parent, PathClassLoader.class, "mDexs", combineArray(getField(parent, PathClassLoader.class, "mDexs"), getField(cl, DexClassLoader.class, "mDexs")));
            } catch (Throwable tr) {
                String msg = "unable to inject API < 14 " + tr;
                Log.e(TAG, msg);
                throw new LoaderException(msg, tr);
            }
        }

        private static void injectInAliyunOs(Context context, String dexPath, String foo) throws LoaderException {
            String lex = (new File(dexPath)).getName().replaceAll("\\.[a-zA-Z0-9]+", ".lex");
            String lexPath = APP_DEX_DIR + File.separator + lex;

            ClassLoader parent = context.getClassLoader();

            new DexClassLoader(dexPath, APP_CACHE_DIR, dexPath, parent);

            Object[] params = new Object[4];
            params[0] = lexPath;
            params[1] = APP_CACHE_DIR;
            params[2] = dexPath;
            params[3] = parent;

            try {
                Class<?> clazz = Class.forName("dalvik.system.LexClassLoader");
                Constructor<?> ctor = clazz.getConstructor(new Class[]{String.class, String.class, String.class, ClassLoader.class});
                Object cl = ctor.newInstance(params);
                clazz.getMethod("loadClass", new Class[]{String.class}).invoke(cl, new Object[]{foo});

                setField(parent, PathClassLoader.class, "mPaths", appendArray(getField(parent, PathClassLoader.class, "mPaths"), getField(cl, clazz, "mRawDexPath")));
                setField(parent, PathClassLoader.class, "mFiles", combineArray(getField(parent, PathClassLoader.class, "mFiles"), getField(cl, clazz, "mFiles")));
                setField(parent, PathClassLoader.class, "mZips", combineArray(getField(parent, PathClassLoader.class, "mZips"), getField(cl, clazz, "mZips")));
                setField(parent, PathClassLoader.class, "mLexs", combineArray(getField(parent, PathClassLoader.class, "mLexs"), getField(cl, clazz, "mDexs")));
            } catch (Throwable tr) {
                String msg = "unable to inject on aliyun " + tr;
                Log.e(TAG, msg);
                throw new LoaderException(msg, tr);
            }
        }

        private static void setField(Object obj, Class<?> clazz, String fieldName,
                                     Object value) throws NoSuchFieldException, IllegalAccessException {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        }

        private static Object getField(Object obj, Class<?> clazz, String fieldName)
                throws NoSuchFieldException, IllegalAccessException {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        }

        private static Object appendArray(Object a1, Object o2) {
            int l1 = Array.getLength(a1);
            int l = l1 + 1;

            Class<?> clazz = a1.getClass().getComponentType();
            Object a = Array.newInstance(clazz, l);

            for (int i = 0; i < l; i++) {
                if (i < l1) {
                    Array.set(a, i, Array.get(a1, i));
                } else {
                    Array.set(a, i, o2);
                }
                i++;
            }

            return a;
        }

        private static Object combineArray(Object a1, Object a2) {
            int l1 = Array.getLength(a1);
            int l2 = Array.getLength(a2);
            int l = l1 + l2;

            Class<?> clazz = a1.getClass().getComponentType();
            Object a = Array.newInstance(clazz, l);

            for (int i = 0; i < l; i++) {
                if (i < l1) {
                    Array.set(a, i, Array.get(a1, i));
                } else {
                    Array.set(a, i, Array.get(a2, i - l1));
                }
            }

            return a;
        }
    }
}
