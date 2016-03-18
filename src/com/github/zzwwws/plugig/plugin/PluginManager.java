package com.github.zzwwws.plugig.plugin;

import android.content.Context;

/**
 * Created by zzwwws on 2016/3/18.
 */
public class PluginManager {

    public static IPlugin access(Context context, String id){
        IPlugin plugin = PluginFactory.create(context,id);
        return plugin;
    }
}
