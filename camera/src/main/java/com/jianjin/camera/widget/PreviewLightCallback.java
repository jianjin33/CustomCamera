package com.jianjin.camera.widget;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.view.View;

import com.jianjin.camera.CameraDirection;
import com.jianjin.camera.FlashLightStatus;
import com.jianjin.camera.utils.Logger;

/**
 * Created by Administrator on 2018/5/14.
 */

public class PreviewLightCallback implements Camera.PreviewCallback {

    private static final String TAG = PreviewLightCallback.class.getSimpleName();
    private long lastRecordTime = System.currentTimeMillis();
    // 上次记录的索引
    private int darkIndex = 0;
    // 一个历史记录的数组，255是代表亮度最大值
    private long[] darkList = new long[]{255, 255, 255, 255};
    // 扫描间隔
    private int waitScanTime = 300;
    // 亮度低的阀值
    private int darkValue = 60;

    private CameraManager cameraManager;
    private OnCameraLightCallback onCameraLightCallback;

    public PreviewLightCallback(CameraManager cameraManager, OnCameraLightCallback onCameraLightCallback) {
        this.cameraManager = cameraManager;
        this.onCameraLightCallback = onCameraLightCallback;
    }

    /**
     * Called as preview frames are displayed.  This callback is invoked
     * on the event thread {@link #open(int)} was called from.
     * <p>
     * <p>If using the {@link ImageFormat#YV12} format,
     * refer to the equations in {@link Camera.Parameters#setPreviewFormat}
     * for the arrangement of the pixel data in the preview callback
     * buffers.
     *
     * @param data   the contents of the preview frame in the format defined
     *               by {@link ImageFormat}, which can be queried
     *               with {@link Camera.Parameters#getPreviewFormat()}.
     *               If {@link Camera.Parameters#setPreviewFormat(int)}
     *               is never called, the default will be the YCbCr_420_SP
     *               (NV21) format.
     * @param camera the Camera service object.
     */
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastRecordTime < waitScanTime) {
            return;
        }
        lastRecordTime = currentTime;

        int width = camera.getParameters().getPreviewSize().width;
        int height = camera.getParameters().getPreviewSize().height;
        //像素点的总亮度
        long pixelLightCount = 0L;
        //像素点的总数
        long pixeCount = width * height;
        //采集步长，因为没有必要每个像素点都采集，可以跨一段采集一个，减少计算负担，必须大于等于1。
        int step = 10;
        //data.length - allCount * 1.5f的目的是判断图像格式是不是YUV420格式，只有是这种格式才相等
        //因为int整形与float浮点直接比较会出问题，所以这么比
        if (Math.abs(data.length - pixeCount * 1.5f) < 0.00001f) {
            for (int i = 0; i < pixeCount; i += step) {
                //如果直接加是不行的，因为data[i]记录的是色值并不是数值，byte的范围是+127到—128，
                // 而亮度FFFFFF是11111111是-127，所以这里需要先转为无符号unsigned long参考Byte.toUnsignedLong()
                pixelLightCount += ((long) data[i]) & 0xffL;
            }
            //平均亮度
            long cameraLight = pixelLightCount / (pixeCount / step);
            //更新历史记录
            int lightSize = darkList.length;
            darkList[darkIndex = darkIndex % lightSize] = cameraLight;
            darkIndex++;
            boolean isDarkEnv = true;
            //判断在时间范围waitScanTime * lightSize内是不是亮度过暗
            for (int i = 0; i < lightSize; i++) {
                if (darkList[i] > darkValue) {
                    isDarkEnv = false;
                }
            }
            Logger.debug(TAG, "摄像头环境亮度为 ： " + cameraLight);

            if (onCameraLightCallback != null){
                onCameraLightCallback.cameraLight(isDarkEnv);
            }

        }
    }


    public interface OnCameraLightCallback {
        void cameraLight(boolean isDark);
    }
}
