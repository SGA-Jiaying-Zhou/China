package com.sony.dtv.tvcamera.app.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.sony.dtv.tvcamera.R;

import java.util.ArrayList;
import java.util.List;

public class FaceRecognitionView extends View{

    final static String TAG = FaceRecognitionView.class.getSimpleName();
    List<RectF> mRectList = new ArrayList<>();
    Paint mPaintMain = new Paint();
    Paint mPaintOutSide = new Paint();

    public FaceRecognitionView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        Log.v(TAG,"onFinishInflate");
        int color_recognition_outside = getResources().getColor(R.color.recognition_outside);
        int color_recognition_main = getResources().getColor(R.color.recognition_main);
        mPaintMain.setColor(color_recognition_main);
        mPaintMain.setStyle(Paint.Style.STROKE);
        mPaintMain.setStrokeWidth(4);

        mPaintOutSide.setColor(color_recognition_outside);
        mPaintOutSide.setStyle(Paint.Style.STROKE);
        mPaintOutSide.setStrokeWidth(8);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(mRectList!=null) {
            for (RectF rect : mRectList) {
                canvas.drawRect(rect, mPaintOutSide);
                canvas.drawRect(rect, mPaintMain);
            }
        }
    }

    public void updateFaceRecognitionRect(List<RectF> rectList){
        mRectList = transformRect(rectList);
        invalidate();
    }

    private List<RectF> transformRect(List<RectF> rectList){
        List<RectF> list = new ArrayList<>() ;
        for(RectF rectF:rectList){
            RectF rect = new RectF();
            rect.top = rectF.top *getHeight();
            rect.bottom = rectF.bottom *getHeight();
            rect.left = rectF.left *getWidth();
            rect.right = rectF.right *getWidth();
            list.add(rect);
        }
        return list;
    }
}
