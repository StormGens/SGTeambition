package com.stormgens.sgteambition;

import android.app.Application;

/**
 * Created by zlq on 15-10-10.
 */
public class MyApp extends Application{
    private static MyApp _instance;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    private String code;

    @Override
    public void onCreate() {
        _instance=this;
        super.onCreate();
    }

    public static MyApp getInstance(){
        return _instance;
    }
}
