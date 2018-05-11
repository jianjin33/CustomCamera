package com.jianjin.camera.widget;

/**
 * Created by Administrator on 2018/5/11.
 */

public interface ISavePicCallback {
    void saveComplete(String picPath);
    void saveFailure(String msg);
}
