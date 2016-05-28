package com.king.lib.image.crop;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.MarginLayoutParams;

public class CropInforView extends View implements OnTouchListener {

	protected int lastX;
	protected int lastY;
	protected int screenWidth;
	protected int screenHeight;
	private int offset;
	private Paint paint = new Paint();
	private int COLOR_BK;
	private int COLOR_TEXT_TITLE;
	private int COLOR_TEXT_CONTENT;
	//private int SIZE_TEXT;

	private int contentLeft, contentTop, contentWidth, contentHeight;

	public CropInforView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public CropInforView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public CropInforView(Context context) {
		super(context);
		init();
	}

	public void init() {
		setOnTouchListener(this);
		screenHeight = getResources().getDisplayMetrics().heightPixels;
		screenWidth = getResources().getDisplayMetrics().widthPixels;
		COLOR_BK = getResources().getColor(R.color.video_crop_position_frame_bk);
		COLOR_TEXT_TITLE = getResources().getColor(R.color.red);
		COLOR_TEXT_CONTENT = getResources().getColor(R.color.orange);
		//SIZE_TEXT = getResources().getDimensionPixelSize(R.dimen.cropinfor_text_size);
	}

	@Override
	protected void onDraw(Canvas canvas) {

		paint.setColor(COLOR_BK);
		paint.setStyle(Style.FILL);
		canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
		paint.setColor(COLOR_TEXT_TITLE);
		paint.setTextSize(28);
		canvas.drawText("left:", 60, 80, paint);
		canvas.drawText("top:", 260, 80, paint);
		canvas.drawText("width:", 60, 160, paint);
		canvas.drawText("height:", 260, 160, paint);
		paint.setColor(COLOR_TEXT_CONTENT);
		paint.setTextSize(28);
		canvas.drawText("" + contentLeft, 160, 80, paint);
		canvas.drawText("" + contentTop, 360, 80, paint);
		canvas.drawText("" + contentWidth, 160, 160, paint);
		canvas.drawText("" + contentHeight, 360, 160, paint);

		super.onDraw(canvas);
	}

	public void setContentLeft(int contentLeft) {
		this.contentLeft = contentLeft;
	}

	public void setContentTop(int contentTop) {
		this.contentTop = contentTop;
	}

	public void setContentWidth(int contentWidth) {
		this.contentWidth = contentWidth;
	}

	public void setContentHeight(int contentHeight) {
		this.contentHeight = contentHeight;
	}

	public void setInfor(int left, int top, int width, int height) {
		setContentLeft(left);
		setContentTop(top);
		setContentWidth(width);
		setContentHeight(height);
	}
	public void setInfor(int width, int height) {
		setContentWidth(width);
		setContentHeight(height);
	}


	public void setArea(int left, int top, int right, int bottom) {
		/**
		 * onTouch事件中，用layout和invalidate可以实时刷新view的位置大小，但是在UI主线程调用这种方法却又无法改变
		 * 不知道具体原因，可能是跟UI线程有关
		 //    	layout(left, top, right, bottom);
		 //    	invalidate();
		 */
		//采用这种方法可以
		ViewGroup.LayoutParams params = getLayoutParams();
		params.width = right - left;
		params.height = bottom - top;
		((MarginLayoutParams) params).leftMargin = left;
		((MarginLayoutParams) params).topMargin = top;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {

		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				lastY = (int) event.getRawY();
				lastX = (int) event.getRawX();
				break;
			case MotionEvent.ACTION_MOVE:
				int dx = (int) event.getRawX() - lastX;
				int dy = (int) event.getRawY() - lastY;
				move(v, dx, dy);
				lastX = (int) event.getRawX();
				lastY = (int) event.getRawY();
				break;
			case MotionEvent.ACTION_UP:

				break;

			default:
				break;
		}
		invalidate();
		return false;
	}

	private void move(View v, int dx, int dy) {
		int left = v.getLeft() + dx;
		int top = v.getTop() + dy;
		int right = v.getRight() + dx;
		int bottom = v.getBottom() + dy;
		if (left < -offset) {
			left = -offset;
			right = left + v.getWidth();
		}
		if (right > screenWidth + offset) {
			right = screenWidth + offset;
			left = right - v.getWidth();
		}
		if (top < -offset) {
			top = -offset;
			bottom = top + v.getHeight();
		}
		if (bottom > screenHeight + offset) {
			bottom = screenHeight + offset;
			top = bottom - v.getHeight();
		}
		v.layout(left, top, right, bottom);
	}


}
