package com.jianjin.customcamera;

import android.app.Application;

import com.jianjin.camera.CustomCameraAgent;

/**
 * Created by Administrator on 2018/5/11.
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CustomCameraAgent.init(this);
        CustomCameraAgent.openLog();
    }
}
