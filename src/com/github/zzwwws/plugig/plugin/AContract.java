package com.github.zzwwws.plugig.plugin;

import android.content.Context;
import android.content.Intent;

/**
 * Created by zzwwws on 2016/3/18.
 */
public class AContract {

    public static void startA(IPlugin plugin, Context context){
        if (plugin == null) {
            return;
        }

        Intent intent = new Intent();
        plugin.launch(context, intent);
    }
}
