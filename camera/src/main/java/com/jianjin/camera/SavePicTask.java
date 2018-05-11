package com.jianjin.camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.os.Build;
import android.os.Environment;
import android.text.format.DateFormat;

import com.jianjin.camera.utils.Logger;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * Created by Administrator on 2018/5/10.
 */
public class SavePicTask extends Thread {

    private static final String TAG = SavePicTask.class.getSimpleName();
    private byte[] data;
    private boolean isBackCamera;
    private boolean sampleSizeSuggested;
    private boolean ioExceptionRetried;     // 寻找合适的bitmap发生io异常  允许一次重试


    public SavePicTask(byte[] data, boolean isBackCamera) {
        sampleSizeSuggested = false;
        ioExceptionRetried = false;
        this.data = data;
        this.isBackCamera = isBackCamera;
    }

    @Override
    public void run() {
        super.run();

        saveToSDCard(data);
    }

    private boolean saveToSDCard(byte[] data) {
        File pictureDir = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (pictureDir == null) {
            return false;
        }

        String mImagePath = pictureDir + File.separator
                + new DateFormat().format("yyyyMMddHHmmss",
                new Date()).toString()
                + ".jpg";
        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);

        if (isBackCamera) {
            bmp = rotateBitmapByDegree(bmp, 90);
        } else {
            bmp = rotateBitmapByDegree(bmp, 270);
        }
        save(bmp, mImagePath);
        setPictureDegreeZero(mImagePath);

        /*Bitmap bitmap = findFitBitmap(data, getCropRect(data), 1);

        if (bitmap == null) {
            return false;
        }
        return saveBitmap(bitmap, mImagePath, 100, Bitmap.CompressFormat.JPEG);*/
        return false;
    }

    /**
     * 将图片按照某个角度进行旋转
     *
     * @param bm     需要旋转的图片
     * @param degree 旋转角度
     * @return 旋转后的图片
     */
    public static Bitmap rotateBitmapByDegree(Bitmap bm, int degree) {
        Bitmap returnBm = null;

        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
                    bm.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
        }
        if (returnBm == null) {
            returnBm = bm;
        }
        if (bm != returnBm) {
            bm.recycle();
        }
        return returnBm;
    }

    private void save(Bitmap bitmap, String filePath) {
        File file = new File(filePath);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs(); // 创建文件夹
        }
        try {
            BufferedOutputStream bos = new BufferedOutputStream(
                    new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos); // 向缓冲区之中压缩图片
            bos.flush();
            bos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 将图片的旋转角度置为0  ，此方法可以解决某些机型拍照后图像，出现了旋转情况
     *
     * @param path
     * @return void
     */
    private void setPictureDegreeZero(String path) {
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            // 修正图片的旋转角度，设置其不旋转。这里也可以设置其旋转的角度，可以传值过去，
            // 例如旋转90度，传值ExifInterface.ORIENTATION_ROTATE_90，需要将这个值转换为String类型的
            if (isBackCamera) {
                exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, "ExifInterface.ORIENTATION_ROTATE_90");
            }else {
                exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, "ExifInterface.ORIENTATION_ROTATE_270");
            }
            exifInterface.saveAttributes();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /**
     * 获取以中心点为中心的正方形区域
     *
     * @param data
     * @return
     */
    private Rect getCropRect(byte[] data) {
        //获得图片大小
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, options);

        int width = options.outWidth;
        int height = options.outHeight;
        int centerX = width / 2;
        int centerY = height / 2;
        Logger.debug(TAG, "width" + width + "height" + height);

//        return new Rect(0, 0, height, width);

        return new Rect(centerX - height / 2, centerY - width / 2, centerX + height / 2, centerY + width / 2);
    }


    /**
     * 寻找合适的bitmap  剪切rect  并且做旋转  镜像处理
     *
     * @param data
     * @param sampleSize
     * @return
     */
    private Bitmap findFitBitmap(byte[] data, Rect rect, int sampleSize) {
        InputStream is = null;
        System.gc();
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
            options.inSampleSize = sampleSize;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inPurgeable = true;
            options.inInputShareable = true;

            is = new ByteArrayInputStream(data);

            Bitmap bitmap = decode(is, rect, options);

            bitmap = rotateBitmap(bitmap, isBackCamera);
            return bitmap;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            System.gc();

                /* 是否对sampleSize做出过建议，没有就做一次建议，按照建议的尺寸做出缩放，做过就直接缩小图片**/
            if (sampleSizeSuggested) {
                return findFitBitmap(data, rect, sampleSize * 2);
            } else {
                // FIXME
                return findFitBitmap(data, rect, suggestSampleSize(data, 720));
            }
        } catch (Exception e) {
            e.printStackTrace();
            //try again
            if (!ioExceptionRetried) {
                ioExceptionRetried = true;
                return findFitBitmap(data, rect, sampleSize);
            }
            return null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 旋转bitmap
     * 对于前置摄像头和后置摄像头采用不同的旋转角度  前置摄像头还需要做镜像水平翻转
     *
     * @param bitmap
     * @param isBackCamera
     * @return
     */
    public Bitmap rotateBitmap(Bitmap bitmap, boolean isBackCamera) {
        System.gc();
        int degrees = isBackCamera ? 90 : -90;
        if (null == bitmap) {
            return bitmap;
        }
        Matrix matrix = new Matrix();
        matrix.setRotate(degrees, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
        if (!isBackCamera) {
            matrix.postScale(-1, 1, bitmap.getWidth() / 2, bitmap.getHeight() / 2);   //镜像水平翻转
        }
//            Bitmap bmp = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,!isBackCamera);
        //不需要透明度 使用RGB_565
        Bitmap bmp = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.RGB_565);
        Paint paint = new Paint();
        Canvas canvas = new Canvas(bmp);
        canvas.drawBitmap(bitmap, matrix, paint);


        if (null != bitmap) {
            bitmap.recycle();
        }

        return bmp;
    }


    public static boolean saveBitmap(Bitmap b, String absolutePath, int quality, Bitmap.CompressFormat format) {
        String fileName = absolutePath;
        File f = new File(fileName);
        try {
            f.createNewFile();
            FileOutputStream fOut = new FileOutputStream(f);
            b.compress(format, quality, fOut);
            fOut.flush();
            fOut.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * 按指定区域解码
     *
     * @param is
     * @param options
     */
    public static Bitmap decode(InputStream is, Rect rect, BitmapFactory.Options options) throws Exception {
        Bitmap bitmap = null;
        if (Build.VERSION.SDK_INT > 9) {
            BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(is, false);
            bitmap = decoder.decodeRegion(rect, options);
        } else {
            Bitmap temp = BitmapFactory.decodeStream(is, null, options);
            bitmap = Bitmap.createBitmap(temp, rect.left, rect.top, rect.width(), rect.height());
            if (temp != null && !temp.isRecycled()) {
                temp.recycle();
            }
        }

        return bitmap;
    }

    /**
     * 给出合适的sampleSize的建议
     *
     * @param data
     * @param target
     * @return
     */
    private int suggestSampleSize(byte[] data, int target) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inPurgeable = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, options);

        int w = options.outWidth;
        int h = options.outHeight;
        int candidateW = w / target;
        int candidateH = h / target;
        int candidate = Math.max(candidateW, candidateH);
        if (candidate == 0)
            return 1;
        if (candidate > 1) {
            if ((w > target) && (w / candidate) < target)
                candidate -= 1;
        }
        if (candidate > 1) {
            if ((h > target) && (h / candidate) < target)
                candidate -= 1;
        }
        return candidate;
    }

}
