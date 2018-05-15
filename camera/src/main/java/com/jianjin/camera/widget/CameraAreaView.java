package com.jianjin.camera.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Administrator on 2018/5/10.
 */
public class CameraAreaView extends View {

    private Paint mPaint;
    private int height;
    private int width;
    private int CORNER_WIDTH = 8;
    private int CORNER_LENGTH = 40;

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
        canvas.drawRect(0, 0, CORNER_WIDTH,
                CORNER_LENGTH, mPaint);
        canvas.drawRect(0, 0, CORNER_LENGTH, CORNER_WIDTH, mPaint);

        // 右上
        canvas.drawRect(width - CORNER_LENGTH, 0, width,
                CORNER_WIDTH, mPaint);
        canvas.drawRect(width - CORNER_WIDTH, 0, width, CORNER_LENGTH, mPaint);

        // 左下
        canvas.drawRect(0, height - CORNER_LENGTH, CORNER_WIDTH,
                height, mPaint);
        canvas.drawRect(0, height - CORNER_WIDTH, CORNER_LENGTH, height, mPaint);

        // 右下
        canvas.drawRect(width - CORNER_LENGTH, height - CORNER_WIDTH, width,
                height, mPaint);
        canvas.drawRect(width - CORNER_WIDTH, height - CORNER_LENGTH, width, height, mPaint);
    }


}
