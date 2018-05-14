package com.jianjin.camera.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SeekBar;

import com.jianjin.camera.CameraDirection;
import com.jianjin.camera.IActivityLifeCycle;
import com.jianjin.camera.ICameraOperation;
import com.jianjin.camera.R;
import com.jianjin.camera.SavePicHandler;
import com.jianjin.camera.SensorController;
import com.jianjin.camera.UIHandler;
import com.jianjin.camera.utils.Logger;
import com.jianjin.camera.utils.UIUtils;

import static com.jianjin.camera.utils.Consts.MODE_INIT;
import static com.jianjin.camera.utils.Consts.MODE_ZOOM;

/**
 * Created by Administrator on 2018/5/9.
 */
public class CameraContainer extends FrameLayout implements IActivityLifeCycle, ICameraOperation {

    public static final int RESET_MASK_DELAY = 1000; //一段时间后遮罩层一定要隐藏
    private static final String TAG = "CameraContainer";
    private Context mContext;
    private CameraPreview mCameraView;
    private FocusImageView mFocusImageView;
    private SensorController mSensorController;
    private SoundPool mSoundPool;
    private Activity mActivity;
    private ISavePicCallback savePicCallback;
    private SeekBar mZoomSeekBar;
    private HandlerThread handlerThread;
//    private boolean handlerThreadQuit;
//    private SavePicHandler savePicHandler;

    public CameraContainer(@NonNull Context context) {
        this(context, null);
    }

    public CameraContainer(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraContainer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    private void init() {
        inflate(mContext, R.layout.custom_camera_container, this);
        mCameraView = (CameraPreview) findViewById(R.id.camera_preview);
        mFocusImageView = (FocusImageView) findViewById(R.id.iv_focus);
        mZoomSeekBar = (SeekBar) findViewById(R.id.seek_zoom);

        mSensorController = SensorController.getInstance();

        mSensorController.setCameraFocusListener(new SensorController.CameraFocusListener() {
            @Override
            public void onFocus() {
                Point point = new Point(UIUtils.screenWidth / 2, UIUtils.screenHeight / 2);

                onCameraFocus(point);
            }
        });

        mCameraView.setOnCameraPrepareListener(new OnCameraPrepareListener() {
            @Override
            public void onPrepare(CameraDirection cameraDirection) {
                postDelayed(new Runnable() {
                    @Override
                    public void run() {

                    }
                }, RESET_MASK_DELAY);
                //在这里相机已经准备好 可以获取maxZoom
                mZoomSeekBar.setMax(mCameraView.getMaxZoom());

                if (cameraDirection == CameraDirection.CAMERA_BACK) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            Point point = new Point(UIUtils.screenWidth / 2, UIUtils.screenHeight / 2);
                            onCameraFocus(point);
                        }
                    }, 800);
                }
            }
        });
        mCameraView.setSwitchCameraCallBack(new SwitchCameraCallback() {
            @Override
            public void switchCamera(boolean isSwitchFromFront) {
                if (isSwitchFromFront) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Point point = new Point(UIUtils.screenWidth / 2, UIUtils.screenHeight / 2);
                            onCameraFocus(point);
                        }
                    }, 300);
                }
            }
        });

        mCameraView.setPictureCallback(mPictureCallback);
        mZoomSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
    }

    private Handler handler = new UIHandler(CameraContainer.this);
    // 拍照回调
    private Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {


        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            boolean isBackCamera = true;
            if (mCameraView != null) {
                isBackCamera = mCameraView.getCameraId() == CameraDirection.CAMERA_BACK;
            }

//            new SavePicTask(data, isBackCamera).start();

            Logger.debug(TAG, "创建线程");
            handlerThread = new HandlerThread("save_picture");
            handlerThread.start();

            SavePicHandler savePicHandler = new SavePicHandler(handlerThread.getLooper(), handler, data, isBackCamera);
            // 开始存储图片
            savePicHandler.sendEmptyMessage(0);
        }
    };


    private final SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            mCameraView.setZoom(progress);

            mHandler.removeCallbacksAndMessages(mZoomSeekBar);
            //ZOOM模式下 在结束两秒后隐藏seekbar 设置token为mZoomSeekBar用以在连续点击时移除前一个定时任务
            mHandler.postAtTime(new Runnable() {

                @Override
                public void run() {
                    mZoomSeekBar.setVisibility(View.GONE);
                }
            }, mZoomSeekBar, SystemClock.uptimeMillis() + 2000);
        }


        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }


        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };


    @Override
    public void onStart() {
        mSensorController.onStart();

        if (mCameraView != null) {
            mCameraView.onStart();
        }

        mSoundPool = getSoundPool();
    }

    @Override
    public void onStop() {
        mSensorController.onStop();

        if (mCameraView != null) {
            mCameraView.onStop();
        }

        mSoundPool.release();
        mSoundPool = null;
        if (handlerThread != null) {
             handlerThread.quit();
        }
    }

    /**
     * 切换前后摄像头
     */
    @Override
    public void switchCamera() {
        mCameraView.switchCamera();
    }

    /**
     * 拍照
     */
    @Override
    public boolean takePicture(ISavePicCallback savePicCallback) {
        this.savePicCallback = savePicCallback;
//        setMaskOn();
        boolean flag = mCameraView.takePicture(savePicCallback);
        if (!flag) {
            mSensorController.unlockFocus();
        }
//        setMaskOff();
        return flag;
    }

    /**
     * 闪光灯开启，关闭
     */
    @Override
    public void switchFlashMode() {
        mCameraView.switchFlashMode();
    }

    /**
     * 相机最大缩放级别
     */
    @Override
    public int getMaxZoom() {
        return 0;
    }

    /**
     * 设置当前缩放级别
     *
     * @param zoom
     */
    @Override
    public void setZoom(int zoom) {

    }

    /**
     * 获取当前缩放级别
     */
    @Override
    public int getZoom() {
        return 0;
    }

    /**
     * 释放相机
     */
    @Override
    public void releaseCamera() {
        if (mCameraView != null) {
            mCameraView.releaseCamera();
        }
    }

    public Activity getActivity() {
        return mActivity;
    }

    public ISavePicCallback getSavePicCallback() {
        return savePicCallback;
    }

    public void bindActivity(Activity activity) {
        this.mActivity = activity;
        if (mCameraView != null) {
            mCameraView.bindActivity(activity);
        }
    }

    private SoundPool getSoundPool() {
        if (mSoundPool == null) {
            mSoundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
/*            mFocusSoundId = mSoundPool.load(mContext,R.raw.camera_focus,1);
            mFocusSoundPrepared = false;
            mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                @Override
                public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                    mFocusSoundPrepared = true;
                }
            });*/
        }
        return mSoundPool;
    }


    private int mode = MODE_INIT;// 初始状态

    private float startDis;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
/** 通过与运算保留最后八位 MotionEvent.ACTION_MASK = 255 */
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            // 手指压下屏幕
            case MotionEvent.ACTION_DOWN:
                mode = MODE_INIT;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                //如果mZoomSeekBar为null 表示该设备不支持缩放 直接跳过设置mode Move指令也无法执行
                if (mZoomSeekBar == null) return true;
                //移除token对象为mZoomSeekBar的延时任务
                mHandler.removeCallbacksAndMessages(mZoomSeekBar);
//                mZoomSeekBar.setVisibility(View.VISIBLE);
                mZoomSeekBar.setVisibility(View.GONE);

                mode = MODE_ZOOM;
                /** 计算两个手指间的距离 */
                startDis = spacing(event);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == MODE_ZOOM) {
                    //只有同时触屏两个点的时候才执行
                    if (event.getPointerCount() < 2) return true;
                    float endDis = spacing(event);// 结束距离
                    //每变化10f zoom变1
                    int scale = (int) ((endDis - startDis) / 10f);
                    if (scale >= 1 || scale <= -1) {
                        int zoom = mCameraView.getZoom() + scale;
                        //zoom不能超出范围
                        if (zoom > mCameraView.getMaxZoom()) zoom = mCameraView.getMaxZoom();
                        if (zoom < 0) zoom = 0;
                        mCameraView.setZoom(zoom);
                        mZoomSeekBar.setProgress(zoom);
                        //将最后一次的距离设为当前距离
                        startDis = endDis;
                    }
                }
                break;
            // 手指离开屏幕
            case MotionEvent.ACTION_UP:
                if (mode != MODE_ZOOM) {
                    //设置聚焦
                    Point point = new Point((int) event.getX(), (int) event.getY());
                    onCameraFocus(point);
                } else {
                    //ZOOM模式下 在结束两秒后隐藏seekbar 设置token为mZoomSeekBar用以在连续点击时移除前一个定时任务
                    mHandler.postAtTime(new Runnable() {

                        @Override
                        public void run() {
                            mZoomSeekBar.setVisibility(View.GONE);
                        }
                    }, mZoomSeekBar, SystemClock.uptimeMillis() + 2000);
                }
                break;
        }
        return true;
    }


    /**
     * 两点的距离
     */
    private float spacing(MotionEvent event) {
        if (event == null) {
            return 0;
        }
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

        }
    };

    /**
     * 相机对焦  默认不需要延时
     *
     * @param point
     */
    private void onCameraFocus(final Point point) {
        onCameraFocus(point, false);
    }


    /**
     * 相机对焦
     *
     * @param point
     * @param needDelay 是否需要延时
     */
    private void onCameraFocus(final Point point, boolean needDelay) {
        long delayDuration = needDelay ? 300 : 0;

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!mSensorController.isFocusLocked()) {
                    if (mCameraView.onFocus(point, autoFocusCallback)) {
                        mSensorController.lockFocus();
                        mFocusImageView.startFocus(point);

                        //播放对焦音效
//                        if(mFocusSoundPrepared) {
//                            mSoundPool.play(mFocusSoundId, 1.0f, 0.5f, 1, 0, 1.0f);
//                        }
                    }
                }
            }
        }, delayDuration);
    }

    private final Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {

        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            //聚焦之后根据结果修改图片
            if (success) {
                mFocusImageView.onFocusSuccess();
            } else {
                //聚焦失败显示的图片，由于未找到合适的资源，这里仍显示同一张图片
                mFocusImageView.onFocusFailed();
            }
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //一秒之后才能再次对焦
                    mSensorController.unlockFocus();
                }
            }, 1000);
        }
    };
}
