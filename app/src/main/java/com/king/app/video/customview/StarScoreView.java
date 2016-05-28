package com.king.app.video.customview;

import com.king.app.video.Application;
import com.king.app.video.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ImageView.ScaleType;

public class StarScoreView extends LinearLayout implements
		OnTouchListener{

	public interface OnStarCheckListener {
		public void onCheckStar(View view, int score);
	}

	private int starNumber;
	private int starSize;
	private int starLeftMargin;

	private Drawable starOnRes, starOffRes;

	private int maxScore;
	private int starOnNumber;
	private ImageView[] starViews;
	private boolean[] starStatusOn;
	private OnStarCheckListener onStarClickListener;

	private int onIndex;
	private int touchExtra = 100;

	public StarScoreView(Context context) {
		super(context);
		starNumber = 5;
		starSize = 30;
		starLeftMargin = 10;
		maxScore = 100;
		starOnRes = getResources().getDrawable(R.drawable.video_star_on);
		starOffRes = getResources().getDrawable(R.drawable.video_star_off);
		init();
	}

	public StarScoreView(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray array = getContext().getTheme().obtainStyledAttributes(attrs
				, R.styleable.StarScoreViewParams, 0, 0);
		starNumber = array.getInteger(R.styleable.StarScoreViewParams_starNumber, 5);
		maxScore = array.getInteger(R.styleable.StarScoreViewParams_starMaxScore, 100);
		starSize = array.getDimensionPixelSize(R.styleable.StarScoreViewParams_starSize, 30);
		starLeftMargin = array.getDimensionPixelSize(R.styleable.StarScoreViewParams_starLeftMargin, 10);
		starOnRes = array.getDrawable(R.styleable.StarScoreViewParams_starOnDrawable);
		starOffRes = array.getDrawable(R.styleable.StarScoreViewParams_starOffDrawable);
		array.recycle();
		init();

	}

	public StarScoreView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		TypedArray array = getContext().getTheme().obtainStyledAttributes(attrs
				, R.styleable.StarScoreViewParams, defStyleAttr, 0);
		starNumber = array.getInteger(R.styleable.StarScoreViewParams_starNumber, 5);
		maxScore = array.getInteger(R.styleable.StarScoreViewParams_starMaxScore, 100);
		starSize = array.getDimensionPixelSize(R.styleable.StarScoreViewParams_starSize, 30);
		starLeftMargin = array.getDimensionPixelSize(R.styleable.StarScoreViewParams_starLeftMargin, 10);
		starOnRes = array.getDrawable(R.styleable.StarScoreViewParams_starOnDrawable);
		starOffRes = array.getDrawable(R.styleable.StarScoreViewParams_starOffDrawable);
		array.recycle();
		init();
	}

	private void init() {
		setGravity(Gravity.CENTER_VERTICAL);
		starViews = new ImageView[starNumber];
		starStatusOn = new boolean[starNumber];

		for (int i = 0; i < starNumber; i ++) {
			ImageView imageView = new ImageView(getContext());
			LayoutParams params = new LayoutParams(starSize, starSize);
			if (i > 0) {
				((MarginLayoutParams) params).leftMargin = starLeftMargin;
			}
			imageView.setLayoutParams(params);
			imageView.setScaleType(ScaleType.FIT_XY);
			imageView.setImageDrawable(starOffRes);
			imageView.setTag(i);
			imageView.setClickable(false);
			imageView.setFocusable(false);
			imageView.setFocusableInTouchMode(false);
			starViews[i] = imageView;
			addView(imageView);
		}
		setOnTouchListener(this);
	}

	public void setOnStarClickListener(OnStarCheckListener listener) {
		onStarClickListener = listener;
	}

	public void setMaxScore(int max) {
		maxScore = max;
	}

	public void setScore(int num) {
		int step = maxScore / starNumber;
		starOnNumber = num / step;
		setStarOnBefore(starOnNumber);
	}

	public int getScore() {
		int step = maxScore / starNumber;
		return (onIndex + 1) * step;
	}

	private void setStarOnBefore(int index) {
		for (int i = 0; i < index; i ++) {
			starViews[i].setImageDrawable(starOnRes);
			starStatusOn[i] = true;
		}
		for (int i = index; i < starNumber; i ++) {
			starViews[i].setImageDrawable(starOffRes);
			starStatusOn[i] = false;
		}
	}


	/************************touch move to check star on/off*****************************/
	private float lastX = -1;
	private int lastAction = MotionEvent.ACTION_OUTSIDE;
	@Override
	public boolean onTouch(View view, MotionEvent event) {

		int action = event.getAction();
		if (event.getX() > starViews[starViews.length - 1].getRight() + touchExtra
				|| event.getY() < starViews[0].getTop() - touchExtra
				|| event.getY() > starViews[0].getBottom() + touchExtra) {
			action = MotionEvent.ACTION_OUTSIDE;
		}

		switch (action) {
			case MotionEvent.ACTION_DOWN:
				onIndex = getTouchStarIndex(event.getX());
				break;
			case MotionEvent.ACTION_MOVE:
				float x = event.getX();
				if (Application.DEBUG) {
					Log.d("StarScoreView", "x=" + x + ", lastX=" + lastX);
				}
				if (x - lastX > 0) {//move to right
					for (int i = starNumber - 1; i >= 0; i --) {
						if (x > starViews[i].getLeft() && starStatusOn[i] == false) {
							onIndex = i;
							setStarOnBefore(i + 1);
							break;
						}
					}
				}
				else if (x - lastX < 0) {//move to left
					for (int i = 0; i < starNumber - 1; i ++) {
						if (x < starViews[i].getLeft() && starStatusOn[i + 1] == true) {
							onIndex = i;
							setStarOnBefore(i + 1);
							break;
						}
					}
				}
				lastX = x;
				break;
			case MotionEvent.ACTION_UP:
				onCheckEvent();
				break;
			case MotionEvent.ACTION_OUTSIDE:
				/**
				 * 由于限定了touch边界，如果在move过程中移除了边界就不会有ACTION_UP事件，就无法完成onCheckEvent了
				 */
				if (lastAction == MotionEvent.ACTION_MOVE || lastAction == MotionEvent.ACTION_DOWN) {
					onCheckEvent();
				}
				break;

			default:
				break;
		}
		lastAction = action;
		return true;
	}

	private void onCheckEvent() {
		setStarOnBefore(onIndex + 1);
		if (onStarClickListener != null) {
			onStarClickListener.onCheckStar(this, getScore());
		}
	}

	private int getTouchStarIndex(float x) {
		Log.d("StarScoreView", "getTouchStarIndex " + x);
		int index = 0;
		for (int i = starNumber - 1; i >= 0; i --) {
			if (x > starViews[i].getLeft()) {
				index = i;
				break;
			}
		}
		Log.d("StarScoreView", "getTouchStarIndex return" + index);
		return index;
	}

}
