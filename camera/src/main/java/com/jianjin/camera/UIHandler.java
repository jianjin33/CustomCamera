package com.jianjin.camera;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;

import com.jianjin.camera.utils.Logger;
import com.jianjin.camera.widget.CameraContainer;
import com.jianjin.camera.widget.ISavePicCallback;

import java.lang.ref.SoftReference;

import static com.jianjin.camera.utils.Consts.SAVE_IMG_PATH;
import static com.jianjin.camera.utils.Consts.SAVE_PICTURE_FAILURE;
import static com.jianjin.camera.utils.Consts.SAVE_PICTURE_SUCCESS;

/**
 * Created by Administrator on 2018/5/14.
 * 存储图片后，通知UI线程
 */

public class UIHandler extends Handler {
    private String TAG = "UIHandler";

    private SoftReference<CameraContainer> softReference;

    public UIHandler(CameraContainer cameraContainer) {
        this.softReference = new SoftReference<>(cameraContainer);
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        CameraContainer cameraContainer = softReference.get();
        if (cameraContainer == null) return;

        Activity activity = cameraContainer.getActivity();
        if (activity == null) return;

        Bundle bundle = msg.getData();
        if (bundle == null) return;

        ISavePicCallback savePicCallback = cameraContainer.getSavePicCallback();

        switch (msg.what) {
            case SAVE_PICTURE_SUCCESS:
                Logger.debug(TAG, "存储成功，打开第二个界面展示照片");

                    /*String parentPath = bundle.getString(SAVE_IMG_PARENT_PATH);
                    if (parentPath.isEmpty()) return;*/

                String imgPath = bundle.getString(SAVE_IMG_PATH);
                if (imgPath.isEmpty()) return;

                // 数据库插入新图片的信息
                insertMediaStore(activity, imgPath);

                // 发送广播刷新相册（此方法在部分手机上不适配，推荐使用上面的方式）
               /* Intent refreshIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri uri = Uri.fromFile(new File(parentPath));
                refreshIntent.setData(uri);
                activity.sendBroadcast(refreshIntent);*/

                if (savePicCallback != null) {
                    savePicCallback.saveComplete(imgPath);
                }
                break;
            case SAVE_PICTURE_FAILURE:
                if (savePicCallback != null) {
                    savePicCallback.saveFailure("失败");
                }
                break;
            default:
                break;
        }
    }

    /**
     * 数据库插入新图片的信息
     * @param activity
     * @param imgPath
     */
    private void insertMediaStore(Activity activity, String imgPath) {
        ContentValues values = new ContentValues();
        ContentResolver resolver = activity.getContentResolver();
        values.put(MediaStore.Images.ImageColumns.DATA, imgPath);
        values.put(MediaStore.Images.ImageColumns.TITLE,
                imgPath.substring(imgPath.lastIndexOf("/") + 1));
        values.put(MediaStore.Images.ImageColumns.DATE_TAKEN,
                System.currentTimeMillis());
        values.put(MediaStore.Images.ImageColumns.MIME_TYPE,
                "image/jpeg");
        resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values);
    }
}