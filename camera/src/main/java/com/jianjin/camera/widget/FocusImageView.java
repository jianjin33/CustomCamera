package com.jianjin.camera.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import com.jianjin.camera.R;


/**
 * Created by Administrator on 2018/5/9.
 */
public class FocusImageView extends android.support.v7.widget.AppCompatImageView {
    public final static String TAG = "FocusImageView";
    private static final int NO_ID = -1;
    private int mFocusImg = NO_ID;
    private int mFocusSucceedImg = NO_ID;
    private int mFocusFailedImg = NO_ID;
    private Animation mAnimation;
    private Handler mHandler;

    public FocusImageView(Context context) {
        this(context, null);
    }

    public FocusImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FocusImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.focusview_show);
        setVisibility(View.GONE);
        mHandler = new Handler();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FocusImageView);
        mFocusImg = a.getResourceId(R.styleable.FocusImageView_focus_focusing_id, NO_ID);
        mFocusSucceedImg = a.getResourceId(R.styleable.FocusImageView_focus_success_id, NO_ID);
        mFocusFailedImg = a.getResourceId(R.styleable.FocusImageView_focus_fail_id, NO_ID);
        a.recycle();

        //聚焦图片不能为空
        if (mFocusImg == NO_ID || mFocusSucceedImg == NO_ID || mFocusFailedImg == NO_ID)
            throw new RuntimeException("mFocusImg,mFocusSucceedImg,mFocusFailedImg is null");
    }

    /**
     * 显示聚焦图案
     *
     * @param point
     */
    public void startFocus(Point point) {
        if (mFocusImg == NO_ID || mFocusSucceedImg == NO_ID || mFocusFailedImg == NO_ID)
            throw new RuntimeException("focus image is null");
        //根据触摸的坐标设置聚焦图案的位置
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) getLayoutParams();
        params.topMargin = point.y - getMeasuredHeight() / 2;
        params.leftMargin = point.x - getMeasuredWidth() / 2;
        setLayoutParams(params);
        //设置控件可见，并开始动画
        setVisibility(View.VISIBLE);
        setImageResource(mFocusImg);
        startAnimation(mAnimation);
        //3秒后隐藏View。在此处设置是由于可能聚焦事件可能不触发。
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setVisibility(View.GONE);
            }
        }, 3500);
    }

    /**
     * 聚焦成功回调
     */
    public void onFocusSuccess() {
        setImageResource(mFocusSucceedImg);
        //移除在startFocus中设置的callback，1秒后隐藏该控件
        mHandler.removeCallbacks(null, null);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setVisibility(View.GONE);
            }
        }, 1000);

    }

    /**
     * 聚焦失败回调
     */
    public void onFocusFailed() {
        setImageResource(mFocusFailedImg);
        //移除在startFocus中设置的callback，1秒后隐藏该控件
        mHandler.removeCallbacks(null, null);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setVisibility(View.GONE);
            }
        }, 1000);
    }

    /**
     * 设置开始聚焦时的图片
     *
     * @param focus
     */
    public void setFocusImg(int focus) {
        this.mFocusImg = focus;
    }

    /**
     * 设置聚焦成功显示的图片
     *
     * @param focusSucceed
     */
    public void setFocusSucceedImg(int focusSucceed) {
        this.mFocusSucceedImg = focusSucceed;
    }

}
