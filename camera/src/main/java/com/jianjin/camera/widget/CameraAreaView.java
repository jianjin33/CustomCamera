package com.jianjin.camera.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.jianjin.camera.R;

/**
 * Created by Administrator on 2018/5/10.
 */
public class CameraAreaView extends View {

    private Paint mPaint;
    private int height;
    private int width;
    private int cornerWidth;
    private int cornerLength;

    public CameraAreaView(Context context) {
        this(context, null);
    }

    public CameraAreaView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraAreaView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setBackgroundResource(android.R.color.transparent);
        mPaint = new Paint();
        mPaint.setColor(Color.WHITE);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CameraAreaView);
        cornerWidth = typedArray.getDimensionPixelOffset(R.styleable.CameraAreaView_corner_width,10);
        cornerLength = typedArray.getDimensionPixelOffset(R.styleable.CameraAreaView_corner_width,40);
        typedArray.recycle();

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        width = getMeasuredWidth();
        height = getMeasuredHeight();

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 左上
        canvas.drawRect(0, 0, cornerWidth,
                cornerLength, mPaint);
        canvas.drawRect(0, 0, cornerLength, cornerWidth, mPaint);

        // 右上
        canvas.drawRect(width - cornerLength, 0, width,
                cornerWidth, mPaint);
        canvas.drawRect(width - cornerWidth, 0, width, cornerLength, mPaint);

        // 左下
        canvas.drawRect(0, height - cornerLength, cornerWidth,
                height, mPaint);
        canvas.drawRect(0, height - cornerWidth, cornerLength, height, mPaint);

        // 右下
        canvas.drawRect(width - cornerLength, height - cornerWidth, width,
                height, mPaint);
        canvas.drawRect(width - cornerWidth, height - cornerLength, width, height, mPaint);
    }


}
