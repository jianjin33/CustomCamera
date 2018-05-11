package com.jianjin.camera;

/**
 * Created by Administrator on 2018/5/9.
 */
public enum FlashLightStatus {
    LIGHT_ON, LIGHT_OFF, LIGHT_AUTO;

    public FlashLightStatus next() {
        int index = ordinal();
        int len = FlashLightStatus.values().length;
        FlashLightStatus status = FlashLightStatus.values()[(index + 1) % len];
        if(CameraManager.mFlashLightNotSupport.contains(status.name())){
            return next();
        }else {
            return status;
        }
    }

    public static FlashLightStatus valueOf(int index) {
        return FlashLightStatus.values()[index];
    }
}
