package com.jianjin.camera;

/**
 * Created by Administrator on 2018/5/9.
 */

public enum CameraDirection {
    CAMERA_BACK, CAMERA_FRONT;

    //不断循环的枚举
    public CameraDirection next() {
        int index = ordinal();
        int len = CameraDirection.values().length;
        return CameraDirection.values()[(index + 1) % len];
    }

    public static CameraDirection valueOf(int index) {
        return CameraDirection.values()[index];
    }

}
