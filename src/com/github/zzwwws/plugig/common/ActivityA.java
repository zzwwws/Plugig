package com.github.zzwwws.plugig.common;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.github.zzwwws.plugig.R;

/**
 * Created by zzwwws on 2016/3/15.
 */
public class ActivityA extends AppCompatActivity {

    public static void start(Context context){
        Intent intent = new Intent(context, ActivityA.class);
        context.startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a);
    }
}
