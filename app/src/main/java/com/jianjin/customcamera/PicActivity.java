package com.jianjin.customcamera;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

public class PicActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 设置无标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 设置全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_pic);

        ImageView imageView = (ImageView) findViewById(R.id.image_view);
        String imgPath = getIntent().getStringExtra("imgUri");
        Bitmap bitmap = BitmapFactory.decodeFile(imgPath);

        imageView.setImageBitmap(bitmap);
    }
}
