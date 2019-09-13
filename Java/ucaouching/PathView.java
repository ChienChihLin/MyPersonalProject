package com.ambiq.ble.ambiqota.TB;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;

/**
 * Created by asus on 2017/12/31.
 */
public class PathView extends CardiographView {
    private static String TAG = PathView.class.getSimpleName();
    private int tstModIndex = 0;
    public static final int GUIUPDATEIDENTIFIER = -1;

    public PathView(Context context) {
        this(context,null);
    }

    public PathView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public PathView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        new Thread(new ViewUpdateThread()).start();
        mPaint = new Paint();
        mPath = new Path();
    }

    public static int[] rawData = new int[5];
    public void setData(int[] value){
        for (int s = 0; s < 5; s++) {
            rawData[s] = value[s];
        }
    }

    private void drawPath(Canvas canvas) {
//        Log.d(TAG, "drawPath!!!!");
//        Log.d(TAG, "mHeight: " + mHeight);
//        Log.d(TAG, "mWidth: " + mWidth);
        // 重置path
        mPath.reset();

        //用path模拟一个心电图样式
        mPath.moveTo(0,mHeight/2);

        int tmp = 0;
        for (int j = 1; j <= 375; j++) {
            //Log.d(TAG, "ecg_test_rawData" + (j-1) + ":" + TestShowViewActivity.hrRawDataArray[j - 1]);
            mPath.lineTo((j*(mWidth/375.0f)), ((mHeight / 2) - ((TestShowViewActivity.hrRawDataArray[j-1])/3)));
            //tmp += 50;
        }
//        for(int i = 0;i<10;i++) {
//            Log.d(TAG,  "mPath.lineTo, tmp:  " + tmp);
//            mPath.lineTo(tmp + 20, 100);
//            mPath.lineTo(tmp+70, mHeight / 2 + 50);
//            mPath.lineTo(tmp+80, mHeight / 2);
//
//            mPath.lineTo(tmp+200, mHeight / 2);
//            tmp = tmp+200;
//        }
//        for (int j = 1; j <= 100;) {
//            mPath.lineTo((j*(mWidth/320.0f)), 100 + tstModIndex);
//            mPath.lineTo(((j+=1)*(mWidth/320.0f)), mHeight / 2 + 50 + tstModIndex);
//            mPath.lineTo(((j+=2)*(mWidth/320.0f)), mHeight / 2 + tstModIndex);
//            mPath.lineTo(((j+=3)*(mWidth/320.0f)), mHeight / 2 + tstModIndex);
//        }

        //设置画笔style
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(mLineColor);
        mPaint.setStrokeWidth(5);
        canvas.drawPath(mPath,mPaint);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawPath(canvas);
//        if ((mHeight / 2 + 50 + tstModIndex) < mHeight) {
//            tstModIndex += 20;
//        } else {
//            tstModIndex = 0;
//        }
//        new Thread(new Runnable() {
//            public void run() {
//                post(new Runnable() {
//                    public void run() {
//                        //scrollBy(1,0);
//                        //postInvalidateDelayed(100);
//                        invalidate();
//                    }
//                });
//            }
//        }).start();
    }

    class ViewUpdateThread implements Runnable {
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                //scrollBy(1,0);
                postInvalidate();
            }
        }
    }
}
