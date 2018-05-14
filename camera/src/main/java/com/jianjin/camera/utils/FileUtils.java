package com.jianjin.camera.utils;

import android.os.Environment;

import com.jianjin.camera.CustomCameraAgent;

import java.io.File;

/**
 * Created by Administrator on 2018/5/10.
 */
public class FileUtils {
    public static String getPhotoPathForLockWallPaper() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator
                + CustomCameraAgent.picFileName;

        File file = new File(path);
        if (!file.getParentFile().exists()) {
            mkdir(file.getParentFile());
        }
        if (!file.exists()) {
            mkdir(file);
        }
        return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
                + CustomCameraAgent.picFileName;
    }

    public static boolean mkdir(File file) {
        while (!file.getParentFile().exists()) {
            mkdir(file.getParentFile());
        }
        return file.mkdir();
    }

}
