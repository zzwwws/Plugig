package com.github.zzwwws.plugig.plugin.A;

import android.content.Context;
import android.content.Intent;

import com.github.zzwwws.plugig.plugin.IPlugin;

/**
 * Created by zzwwws on 2016/3/18.
 */
public class APlugin implements IPlugin {

    @Override
    public void launch(Context context, Intent intent) {
        APluginActivity.start(context);
    }
}
