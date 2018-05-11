package com.jianjin.camera;

import com.jianjin.camera.widget.ISavePicCallback;

/**
 * Created by Administrator on 2018/5/9.
 * 操作相机的接口
 */
public interface ICameraOperation {

    /**
     * 切换前后摄像头
     */
    void switchCamera();

    /**
     * 拍照
     */
    boolean takePicture(ISavePicCallback savePicCallback);

    /**
     * 闪光灯开启，关闭
     */
    void switchFlashMode();

    /**
     * 相机最大缩放级别
     */

    int getMaxZoom();

    /**
     * 设置当前缩放级别
     */
    void setZoom(int zoom);

    /**
     * 获取当前缩放级别
     */
    int getZoom();

    /**
     * 释放相机
     */
    void releaseCamera();
}
