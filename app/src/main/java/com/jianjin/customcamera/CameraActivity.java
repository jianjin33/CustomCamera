package com.jianjin.customcamera;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.jianjin.camera.CameraManager;
import com.jianjin.camera.utils.UriUtils;
import com.jianjin.camera.widget.CameraContainer;
import com.jianjin.camera.widget.ISavePicCallback;

public class CameraActivity extends Activity implements ISavePicCallback {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    static final int CAMERA_REQUEST_CODE = 100;

    private CameraManager mCameraManager;
    private CameraContainer cameraContainer;
    private int REQUEST_PICTURE = 2;
    private TextView flash;
    private TextView picture;
    private TextView mSwitch;
    private Button mTakePictureBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置无标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 设置全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera);

        // Example of a call to a native method
        /*TextView tv = (TextView) findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());*/
        mCameraManager = CameraManager.getInstance(this);
        initView();
        mCameraManager.bindOptionMenuView(flash, mSwitch);
        setListener();
        checkPermission();
    }

    private void initView() {

        flash = (TextView) findViewById(R.id.button_flash);
        cameraContainer = (CameraContainer) findViewById(R.id.camera);
        picture = (TextView) findViewById(R.id.button_picture);
        mSwitch = (TextView) findViewById(R.id.button_switch);
        mTakePictureBtn = (Button) findViewById(R.id.button_capture);
    }

    private void setListener() {

        mTakePictureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraContainer.takePicture(CameraActivity.this);
            }
        });

        mSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraContainer.switchCamera();
            }
        });
        flash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraContainer.switchFlashMode();
            }
        });
        picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_PICTURE);
            }
        });
    }

    private void checkPermission() {
        final String permission = Manifest.permission.CAMERA;  //相机权限
        final String permission1 = Manifest.permission.WRITE_EXTERNAL_STORAGE; //写入数据权限
        final String permission2 = Manifest.permission.READ_EXTERNAL_STORAGE; //写入数据权限
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, permission1) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, permission2) != PackageManager.PERMISSION_GRANTED) {  //先判断是否被赋予权限，没有则申请权限
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {  //给出权限申请说明
                ActivityCompat.requestPermissions(this,
                        new String[]{permission, permission1, permission2},
                        CAMERA_REQUEST_CODE);
            } else { //直接申请权限
                ActivityCompat.requestPermissions(this,
                        new String[]{permission, permission1, permission2},
                        CAMERA_REQUEST_CODE); //申请权限，可同时申请多个权限，并根据用户是否赋予权限进行判断
            }
        } else {  //赋予过权限，则直接调用相机拍照
            cameraContainer.bindActivity(CameraActivity.this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED
                && grantResults[2] == PackageManager.PERMISSION_GRANTED) {  //如果用户赋予权限，则调用相机
            cameraContainer.bindActivity(CameraActivity.this);
        } else { //未赋予权限，则做出对应提示
        }
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        if (res == RESULT_OK) {
            try {
                /**
                 * 该uri是上一个Activity返回的
                 */
                Uri uri = data.getData();

//                File file = new FileStorage().createIconFile(); //工具类
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                    // 针对Android7.0，需要通过FileProvider封装过的路径，提供给外部调用
//                    // 通过FileProvider创建一个content类型的Uri，进行封装
//                    Uri imageUri = FileProvider.getUriForFile(activity, "com.pecoo.ifcoo.fileprovider", file);
//                    output = file;
                } else {
                    // 7.0以下，如果直接拿到相机返回的intent值，拿到的则是拍照的原图大小，很容易发生OOM，所以我们同样将返回的地址，保存到指定路径，返回到Activity时，去指定路径获取，压缩图片
//                    output = ImageUtils.createFile(FileUtils.getInst().getPhotoPathForLockWallPaper(), true);
//                    Uri imageUri = Uri.fromFile(output);
                }
                if (uri != null) {
                    Intent intent = new Intent(CameraActivity.this, PicActivity.class);
                    intent.putExtra("imgUri", UriUtils.getPath(CameraActivity.this, uri));
                    startActivity(intent);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    @Override
    public void saveComplete(String picPath) {
        Intent intent = new Intent(this, PicActivity.class);
        intent.putExtra("imgUri", picPath);
        startActivity(intent);
    }

    @Override
    public void saveFailure(String msg) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCameraManager.unbinding();
    }
}
