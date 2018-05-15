package com.jianjin.camera.widget;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.jianjin.camera.CameraDirection;
import com.jianjin.camera.FlashLightStatus;
import com.jianjin.camera.utils.Logger;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.jianjin.camera.utils.Consts.TYPE_PICTURE;
import static com.jianjin.camera.utils.Consts.TYPE_PREVIEW;

/**
 * Created by Administrator on 2018/5/9.
 * 相机管理类
 */
public class CameraManager {

    private static final String TAG = CameraManager.class.getSimpleName();
    private static CameraManager mInstance;
    public static List<FlashLightStatus> mFlashLightNotSupport = new ArrayList<>();
    private String[] flashHint;
    private String[] cameraDireHint;
    private static final int ALLOW_PIC_LEN = 2000;       //最大允许的照片尺寸的长度   宽或者高

    private Context mContext;
    private CameraDirection mCameraDirection;
    private FlashLightStatus mFlashLightStatus;
    private TextView mTvFlashLight;
    private TextView mTvCameraDirection;
    private PreviewLightCallback previewLightCallback;


    public static CameraManager getInstance(Context context) {
        if (mInstance == null) {
            synchronized (CameraManager.class) {
                if (mInstance == null) {
                    mInstance = new CameraManager(context);
                }
            }
        }
        return mInstance;
    }

    private CameraManager(Context context) {
        this.mContext = context;

        // 默认 自动
        mFlashLightStatus = FlashLightStatus.valueOf(FlashLightStatus.LIGHT_OFF.ordinal());
        // 默认后置摄像头
        mCameraDirection = CameraDirection.valueOf(CameraDirection.CAMERA_BACK.ordinal());
    }


    /**
     * 绑定闪光灯、摄像头设置控件
     *
     * @param tvFlashLight
     * @param tvCameraDirection
     */
    public void bindOptionMenuView(TextView tvFlashLight, TextView tvCameraDirection,
                                   @Nullable String[] flashHint, @Nullable String[] cameraDireHint) {
        mTvFlashLight = tvFlashLight;
        mTvCameraDirection = tvCameraDirection;
        this.flashHint = flashHint;
        this.cameraDireHint = cameraDireHint;

        // 刷新视图
        setFlashLightStatus(getFlashLightStatus());
        setCameraDirection(getCameraDirection());
    }

    public void unbindView() {
        mTvFlashLight = null;
        mTvCameraDirection = null;
    }

    void setCameraDirection(CameraDirection mCameraDirection) {
        this.mCameraDirection = mCameraDirection;
        if (mTvCameraDirection != null) {
            if (cameraDireHint != null) {
                mTvCameraDirection.setText(cameraDireHint[mCameraDirection.ordinal()]);
            }
            // 前置摄像头为选中状态
            mTvCameraDirection.setSelected(mCameraDirection == CameraDirection.CAMERA_FRONT);
            // 设置前后摄像头的图标显示
            // 记录相机方向 sp
            // 前置摄像头不能开启闪光灯
            if (mTvFlashLight != null) {
                mTvFlashLight.setVisibility(mCameraDirection == CameraDirection.CAMERA_BACK ? View.VISIBLE : View.GONE);
            }
        }
    }

    CameraDirection getCameraDirection() {
        return mCameraDirection;
    }

    void setFlashLightStatus(FlashLightStatus mFlashLightStatus) {
        this.mFlashLightStatus = mFlashLightStatus;
        if (mTvFlashLight != null) {
            if (flashHint != null) {
                mTvFlashLight.setText(flashHint[mFlashLightStatus.ordinal()]);
            }
            mTvFlashLight.setSelected(mFlashLightStatus == FlashLightStatus.LIGHT_ON);
            // 设置各种状态图片
            // 保存当前状态sp
        }
    }

    FlashLightStatus getFlashLightStatus() {
        return mFlashLightStatus;
    }

    /**
     * 初始化相机
     *
     * @param facing 前后摄像头
     * @return
     */
    Camera openCamera(int facing) {
        Camera camera = null;
        if (checkCameraHardware(mContext)) {
            camera = Camera.open(getCameraId(facing));
            setPreviewLight(camera);
        }
        mFlashLightNotSupport.clear();
        if (camera != null) {
            List<String> supportFlashModes = camera.getParameters().getSupportedFlashModes();
            // 后置摄像头判断闪光灯是否支持，前置摄像头不需要开启闪光灯
            if (facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                // 某些supportFlashModes  null  不支持
                if (supportFlashModes != null) {
                   /* if (!supportFlashModes.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
                        mFlashLightNotSupport.add(FlashLightStatus.LIGHT_AUTO);
                    }*/
                    if (!supportFlashModes.contains(Camera.Parameters.FLASH_MODE_ON)) {
                        mFlashLightNotSupport.add(FlashLightStatus.LIGHT_ON);
                    }
                }
            }
        }
        Logger.info(TAG, "相机初始化open");
        return camera;
    }

    private int getCameraId(int facing) {
        int numberOfCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo info = new Camera.CameraInfo();
        for (int id = 0; id < numberOfCameras; id++) {
            Camera.getCameraInfo(id, info);
            if (info.facing == facing) {
                return id;
            }
        }
        return -1;
    }

    /**
     * 判断相机是否支持
     *
     * @param context
     * @return
     */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    void setPreviewLight(Camera camera) {
        if (mTvFlashLight != null && previewLightCallback == null) {
            // 亮度过暗显示开灯按钮
            PreviewLightCallback.OnCameraLightCallback onCameraLightCallback = new PreviewLightCallback.OnCameraLightCallback() {
                @Override
                public void cameraLight(boolean isDarkEnv) {
                    // 亮度过暗显示开灯按钮
                    if (getCameraDirection() == CameraDirection.CAMERA_BACK) {
                        if (isDarkEnv || getFlashLightStatus() == FlashLightStatus.LIGHT_ON) {
                            if (mTvFlashLight.getVisibility() != View.VISIBLE) {
                                mTvFlashLight.setVisibility(View.VISIBLE);
                            }
                        } else {
                            if (mTvFlashLight.getVisibility() != View.GONE) {
                                mTvFlashLight.setVisibility(View.GONE);
                            }
                        }
                    }
                }
            };
            previewLightCallback = new PreviewLightCallback(this, onCameraLightCallback);
        }

        camera.setPreviewCallback(previewLightCallback);
    }


    void releaseCamera(Camera camera) {
        if (camera != null) {
            try {
                camera.setPreviewCallback(null);
                camera.setPreviewCallbackWithBuffer(null);
                camera.stopPreview();
                camera.release();
                camera = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 设置相机拍照的尺寸
     *
     * @param camera
     */
    private void setUpPicSize(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();

        try {
            Camera.Size adapterSize = findBestResolution(camera, 1.0d, TYPE_PICTURE);
            parameters.setPictureSize(adapterSize.width, adapterSize.height);
            camera.setParameters(parameters);

            Logger.info(TAG, "setUpPicSize:" + adapterSize.width + "*" + adapterSize.height);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置相机预览的尺寸
     *
     * @param camera
     */
    private void setUpPreviewSize(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();

        try {
            Camera.Size adapterSize = findBestResolution(camera, 1.0d, TYPE_PREVIEW);
            parameters.setPreviewSize(adapterSize.width, adapterSize.height);
            camera.setParameters(parameters);

            Logger.info(TAG, "setUpPreviewSize:" + adapterSize.width + "*" + adapterSize.height);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param camera
     * @param bl
     */
    void setFitPicSize(Camera camera, float bl) {
        Camera.Parameters parameters = camera.getParameters();

        try {
            Camera.Size adapterSize = findFitPicResolution(camera, bl);
            parameters.setPictureSize(adapterSize.width, adapterSize.height);
            camera.setParameters(parameters);

            Logger.info(TAG, "setFitPicSize:" + adapterSize.width + "*" + adapterSize.height);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置合适的预览尺寸
     *
     * @param camera
     */
    void setFitPreSize(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();

        try {
            Camera.Size adapterSize = findFitPreResolution(camera);
            parameters.setPreviewSize(adapterSize.width, adapterSize.height);
            camera.setParameters(parameters);

            Logger.info(TAG, "setFitPreSize:" + adapterSize.width + "*" + adapterSize.height);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 找到合适的尺寸
     *
     * @param cameraInst
     * @param maxDistortion 最大允许的宽高比
     * @return
     * @type 尺寸类型 0：preview  1：picture
     */
    private Camera.Size findBestResolution(Camera cameraInst, double maxDistortion, int type) throws Exception {
        Camera.Parameters cameraParameters = cameraInst.getParameters();
        List<Camera.Size> supportedPicResolutions = type == TYPE_PREVIEW ? cameraParameters.getSupportedPreviewSizes() : cameraParameters.getSupportedPictureSizes(); // 至少会返回一个值

        StringBuilder picResolutionSb = new StringBuilder();
        for (Camera.Size supportedPicResolution : supportedPicResolutions) {
            picResolutionSb.append(supportedPicResolution.width).append('x')
                    .append(supportedPicResolution.height).append(" ");
        }
        Logger.debug(TAG, "Supported picture resolutions: " + picResolutionSb);

        Camera.Size defaultPictureResolution = cameraParameters.getPictureSize();
        Logger.debug(TAG, "default picture resolution " + defaultPictureResolution.width + "x"
                + defaultPictureResolution.height);

        // 排序
        List<Camera.Size> sortedSupportedPicResolutions = new ArrayList<Camera.Size>(
                supportedPicResolutions);
        Collections.sort(sortedSupportedPicResolutions, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size a, Camera.Size b) {
                int aRatio = a.width / a.height;
                int bRatio = b.width / a.height;

                if (Math.abs(aRatio - 1) <= Math.abs(bRatio - 1)) {
                    return -1;
                } else {
                    return 1;
                }
            }
        });

        //返回最合适的
        return sortedSupportedPicResolutions.get(0);
    }

    /**
     * 返回合适的照片尺寸参数
     *
     * @param camera
     * @param bl
     * @return
     */
    private Camera.Size findFitPicResolution(Camera camera, float bl) throws Exception {
        Camera.Parameters cameraParameters = camera.getParameters();
        List<Camera.Size> supportedPicResolutions = cameraParameters.getSupportedPictureSizes();

        Camera.Size resultSize = null;
        for (Camera.Size size : supportedPicResolutions) {
            if ((float) size.width / size.height == bl && size.width <= ALLOW_PIC_LEN && size.height <= ALLOW_PIC_LEN) {
                if (resultSize == null) {
                    resultSize = size;
                } else if (size.width > resultSize.width) {
                    resultSize = size;
                }
            }
        }
        if (resultSize == null) {
            return supportedPicResolutions.get(0);
        }
        return resultSize;
    }

    /**
     * 返回合适的预览尺寸参数
     *
     * @param camera
     * @return
     */
    private Camera.Size findFitPreResolution(Camera camera) throws Exception {
        Camera.Parameters cameraParameters = camera.getParameters();
        List<Camera.Size> supportedPicResolutions = cameraParameters.getSupportedPreviewSizes();

        Camera.Size resultSize = null;
        for (Camera.Size size : supportedPicResolutions) {
            if (size.width <= ALLOW_PIC_LEN) {
                if (resultSize == null) {
                    resultSize = size;
                } else if (size.width > resultSize.width) {
                    resultSize = size;
                }
            }
        }
        if (resultSize == null) {
            return supportedPicResolutions.get(0);
        }
        return resultSize;
    }

    /**
     * 控制图像的正确显示方向
     */
    private void setDisplay(Camera camera) {
        int degrees = 90;
        if (Build.VERSION.SDK_INT >= 14) {
            camera.setDisplayOrientation(degrees);
        } else if (Build.VERSION.SDK_INT >= 8) {
            setDisplayOrientation(camera, degrees);
        }
    }

    /**
     * 实现的图像的正确显示
     *
     * @param camera
     * @param i
     */
    private void setDisplayOrientation(Camera camera, int i) {
        Method downPolymorphic;
        try {
            downPolymorphic = camera.getClass().getMethod("setDisplayOrientation",
                    new Class[]{int.class});
            if (downPolymorphic != null) {
                downPolymorphic.invoke(camera, new Object[]{i});
            }
        } catch (Exception e) {
            Logger.error(TAG, "图像出错");
        }
    }
}
