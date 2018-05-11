package com.jianjin.camera.utils;

import android.text.TextUtils;
import android.util.Log;

import com.jianjin.camera.BuildConfig;

import static com.jianjin.camera.CustomCameraAgent.isShowLog;

/**
 * Created by Administrator on 2018/5/11.
 */
public class Logger {

    public static void debug(String tag, String message) {
        if (isShowLog) {
            Log.d(TextUtils.isEmpty(tag) ? BuildConfig.APPLICATION_ID : tag, message);
        }
    }

    public static void info(String tag, String message) {
        if (isShowLog) {
            Log.i(TextUtils.isEmpty(tag) ? BuildConfig.APPLICATION_ID : tag, message);
        }
    }

    public static void warning(String tag, String message) {
        if (isShowLog) {
            Log.w(TextUtils.isEmpty(tag) ? BuildConfig.APPLICATION_ID : tag, message);
        }
    }

    public static void error(String tag, String message) {
        if (isShowLog) {
            Log.e(TextUtils.isEmpty(tag) ? BuildConfig.APPLICATION_ID : tag, message);
        }
    }

}
