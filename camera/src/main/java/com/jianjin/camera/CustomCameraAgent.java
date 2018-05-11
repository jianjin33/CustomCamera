package com.jianjin.camera;

import android.app.Application;
import android.content.Context;

import com.jianjin.camera.utils.UIUtils;

/**
 * Created by Administrator on 2018/5/11.
 * 初始化，保存Application context
 */

public class CustomCameraAgent {

    public static Context mContext;
    public static boolean isShowLog = false;
    public static String picFileName = "photos";

    public static void init(Application application) {
        mContext = application;
        UIUtils.init();
    }

    /**
     * 是否开启Log日志
     */
    public static void openLog() {
        isShowLog = true;
    }

    /**
     * 设置相机存储照片的文件夹名称
     */
    public static void setPicFileName(String picFileName) {
        picFileName = picFileName;
    }

    public static void setCameraWidthAndHeight(int screenWidth, int screenHeight) {
        UIUtils.setWidthAndHeight(screenWidth,screenHeight);
    }
}
