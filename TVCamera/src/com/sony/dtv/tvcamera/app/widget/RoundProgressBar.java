package com.sony.dtv.tvcamera.app.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.sony.dtv.tvcamera.R;

public class RoundProgressBar extends View {

	private Paint paint;

	private int roundProgressColor;

	private float roundWidth;

	private int max;

	private int progress;

	private RectF oval;

	public RoundProgressBar(Context context) {
		this(context, null);
	}

	public RoundProgressBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public RoundProgressBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		paint = new Paint();
		
		TypedArray mTypedArray = context.obtainStyledAttributes(attrs,
				R.styleable.RoundProgressBar);

		roundProgressColor = mTypedArray.getColor(R.styleable.RoundProgressBar_roundProgressColor, Color.WHITE);
//		roundWidth = mTypedArray.getDimension(R.styleable.RoundProgressBar_roundWidth, 2);
        roundWidth = mTypedArray.getDimension(R.styleable.RoundProgressBar_roundWidth, 4);
		max = mTypedArray.getInteger(R.styleable.RoundProgressBar_max, 99);
		mTypedArray.recycle();
	}
	

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

        int centre = getWidth() / 2;
        int radius = (int) (centre - roundWidth * 1.5f);
        oval = new RectF(centre - radius, centre - radius, centre
                + radius, centre + radius);

		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(roundWidth);
		paint.setAntiAlias(true);

		paint.setStrokeWidth(roundWidth);
		paint.setColor(roundProgressColor);
		
		canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));

		canvas.drawArc(oval, -90, getInterpolation(progress), false, paint);
	}

	public float getInterpolation(int progress){
        return (float)(Math.sin(Math.PI * progress / max) * 360.0 / Math.PI + 360.0 * progress / max);
    }
	
	public synchronized int getMax() {
		return max;
	}

	public synchronized void setMax(int max) {
		if(max < 0){
			throw new IllegalArgumentException("max not less than 0");
		}
		this.max = max;
	}

	public synchronized int getProgress() {
		return progress;
	}

	public synchronized void setProgress(int progress) {
		if(progress < 0){
			throw new IllegalArgumentException("progress not less than 0");
		}
		if(progress > max){
			progress = max;
		}
		if(progress <= max){
			this.progress = progress;
			postInvalidate();
		}
		
	}
}
