package com.github.zzwwws.plugig.compatible;

import android.util.Log;

import java.io.File;
import java.io.IOException;


class LoaderWatchDog {
    private static final String TAG = "LoaderWatchDog";
    private static final String SP_NAME = "loader_watch_dog";

    public static void onLoadStart(String lib) {
        if (!failedLastTime(lib)) {
            save(lib);
        }
    }

    public static void onLoadFinish(String lib) {
        remove(lib);
    }

    private static boolean failedLastTime(String lib) {
        if (new File(tmpFile(lib)).exists()) {
            Log.e(TAG, "odex failed last time. clear odex cache");
            clearOdexCache(lib);
            return true;
        }

        return false;
    }

    private static void save(String lib) {
        try {
            new File(tmpFile(lib)).createNewFile();
        } catch (IOException e) {
        }
    }

    private static void remove(String lib) {
        new File(tmpFile(lib)).delete();
    }

    private static void clearOdexCache(String lib) {
        new File("/data/data/com.github.zzwwws.plugig/cache" + "/" + lib).delete();
    }

    private static String tmpFile(String lib) {
        return "/data/data/com.github.zzwwws.plugig" + "/.tmp." + lib;
    }
}
