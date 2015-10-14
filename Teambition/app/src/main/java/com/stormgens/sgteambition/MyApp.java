package com.stormgens.sgteambition;

import android.app.Application;

/**
 * Created by zlq on 15-10-10.
 */
public class MyApp extends Application{
    private static MyApp _instance;

    @Override
    public void onCreate() {
        _instance=this;
        super.onCreate();
    }

    public static MyApp getInstance(){
        return _instance;
    }
}
