package com.jianjin.camera.utils;

import android.util.DisplayMetrics;

import com.jianjin.camera.CustomCameraAgent;

/**
 * Created by Administrator on 2018/5/9.
 */
public class UIUtils {
    public static int screenWidth;
    public static int screenHeight;

    public UIUtils() {
        DisplayMetrics mDisplayMetrics = CustomCameraAgent.mContext.getResources()
                .getDisplayMetrics();
        screenWidth = mDisplayMetrics.widthPixels;
        screenHeight = mDisplayMetrics.heightPixels;
    }

    public static void setWidthAndHeight(int screenWidth, int screenHeight) {
        UIUtils.screenWidth = screenWidth;
        UIUtils.screenHeight = screenHeight;
    }
}
