## CustomCamera
android自定义相机
功能描述：
1. 主要可自定义相机的各类按钮布局
2. 相机拍照缩放功能
3. 相机的宽高设置
3. 前后摄像头拍照设置

## 使用方法
#### 一、添加依赖
Step 1. Add it in your root build.gradle at the end of repositories:
```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
Step 2. Add the dependency
```
dependencies {
        implementation 'com.github.jianjin33:CustomCamera:v1.0.3'
}
```
#### 二、清单文件中配置权限
```
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.MOUNT_UNMOUNT_FILESYSTEMS" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.CAMERA" />

<uses-feature android:name="android.hardware.camera.autofocus" />
<uses-feature android:name="android.hardware.camera" />
```

#### 三、初始化
可以在自定义Application或开启相机界面之前的适当位置，添加一下初始化代码：
```
CustomCameraAgent.init(this);
```

还可以配置一些属性：

`CustomCameraAgent.openLog();`  打开日志

`CustomCameraAgent.setCameraWidthAndHeight();`  设置相机预览界面的宽高和照片的宽高

`CustomCameraAgent.setPicFileName();`   设置自定义相机照片存储路径，默认在photos文件夹下

#### 四、Activity
1. 布局文件中使用CameraContainer控件；
2. onCreate方法中初始化CameraManager，并绑定开关灯View和切换前后摄像头view，第三第四个参数为灯状态和前后摄像头时的文字提示，可为null。
```
mCameraManager = CameraManager.getInstance(this);
mCameraManager.bindOptionMenuView(mCameraIvLight, mCameraTvSwitch, flashHint, null);
mCameraContainer.bindActivity(this);
```
3. 当相机不可见时，及时释放相机的资源及注销一些传感器的监听，调用CameraContainer.onStop(),恢复界面时调用CameraContainer.onStart();
```
@Override
protected void onResume() {
    super.onResume();
    if (mCameraContainer != null) {
        mCameraContainer.onStart();
    }
}

@Override
protected void onPause() {
    super.onPause();
    if (mCameraContainer != null) {
        mCameraContainer.onStop();
    }
}
```
4. 界面销毁时释放相机资源
```
@Override
protected void onDestroy() {
   super.onDestroy();
   mCameraManager.unbindView();
   if (mCameraContainer != null) {
       mCameraContainer.releaseCamera();
   }
}
```
5. 拍照、切换摄像头和开关闪光灯等功能：

`mCameraContainer.switchFlashMode();`   开启关闭闪光灯。这里注意只有当光线较暗的情况下才会显示开灯按钮；

`mCameraContainer.switchCamera();`  切换前后相机

`mCameraContainer.takePicture(ISavePicCallback);` 拍照，传入存储图片完成后的回调接口ISavePicCallback