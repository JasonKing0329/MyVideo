package com.king.app.video.customview;

import com.king.app.video.R;
import com.king.app.video.controller.ScreenUtils;
import com.nineoldandroids.view.ViewHelper;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

public class DragSideBar extends FrameLayout {

	public interface DragSideBarListener {
		/**
		 * switch from open to close
		 */
		public void onSideBarClosed();
		/**
		 * switch from close to open
		 */
		public void onSideBarOpened();
	}

	private final String TAG = "DragSideBar";
	private final boolean DEBUG = false;

	private final int BK_ALPHA_MAX = 0xaa;

	private final int ANIM_SHOW = 0;
	private final int ANIM_CLOSE = 1;

	/**
	 * touch事件当前的偏移量
	 */
	private float offset;
	/**
	 *
	 */
	private float space;
	private int leftPadding;

	private boolean isDrag;

	/**
	 * 打开状态或者关闭状态
	 */
	private boolean isStatusOpen;
	/**
	 * 正在执行打开或结束收尾动画期间，禁止一切touch事件
	 */
	private boolean isAnimming;

	private DragSideBarListener dragSideBarListener;
	private View shadowView;

	public DragSideBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		setBackgroundColor(getResources().getColor(R.color.transparent));

		initParams();
	}

	private void initParams() {
		leftPadding = getResources().getDimensionPixelSize(R.dimen.dragsidebar_padding_left);
		offset = ScreenUtils.getScreenWidth(getContext()) - leftPadding;
		ViewHelper.setTranslationX(this, offset);
	}

	public void setLayoutRes(int resId) {
		View view = LayoutInflater.from(getContext()).inflate(resId, null);
		addView(view);
	}

	public void setDragSideBarListener(DragSideBarListener listener) {
		dragSideBarListener = listener;
	}

	/**
	 * onTouchEvent主要控制关闭阶段，关闭阶段旁边的拖拽区域是属于本View的
	 * 打开的时候是用了父布局的side touch部分，因此由DragSideBarTrigger控制打开的touch事件
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if (!isAnimming) {
			boolean handle = false;

			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					offset = event.getRawX();
					if (offset < getLeft() + getPaddingLeft()) {
						handle = true;
						isDrag = true;
						space = offset;
					}
					break;
				case MotionEvent.ACTION_MOVE:
					if (isDrag) {
						offset = event.getRawX();
						ViewHelper.setTranslationX(this, offset - space);
						setBackgroundAlpha(offset);
					}
					break;
				case MotionEvent.ACTION_UP:
					if (isDrag) {
						dragSideBarOver();
					}
					break;
				case MotionEvent.ACTION_OUTSIDE:
					if (isDrag) {
						dragSideBarOver();
					}
					break;

				default:
					break;
			}

			logParams();

			if (handle) {
				return true;
			}
			else {
				return super.onTouchEvent(event);
			}
		}
		else {
			return true;
		}
	}

	public void logParams() {
//		Log.d(TAG, "width=" + getWidth() + ", left=" + getLeft()
//				+ ", offset=" + offset + "space=" + space);
	}

	private void dragSideBarOver() {
		int width = getWidth();
		if (offset < width / 2) {
			show(true, offset - space);
		}
		else {
			dismiss(true);
		}
		isDrag = false;
	}

	public boolean isOpen() {
		return isStatusOpen;
	}

	/**
	 * execute whole show operation from close to open
	 * @param withAnimation
	 */
	public void forceShow(boolean withAnimation) {
		if (!isStatusOpen) {
			if (withAnimation) {
				new AnimationTask(ANIM_SHOW).execute((float) getWidth(), 0f);
			}
			else {
				show();
			}
		}
	}

	/**
	 * called by DragSideBarTrigger, in process that handle ACTION_UP touch event
	 * @param withAnimation
	 * @param offsetX the current x position while ACTION_UP
	 */
	public void show(boolean withAnimation, float offsetX) {
		Log.d(TAG, "show offsetX=" + offsetX);
		if (withAnimation) {
			new AnimationTask(ANIM_SHOW).execute(offsetX, 0f);
		}
		else {
			show();
		}
	}

	private void show() {
		ViewHelper.setTranslationX(this, 0);
		setBackgroundAlpha(0);

		//从关闭到打开状态，才通知listener打开
		if (!isStatusOpen) {
			if (dragSideBarListener != null) {
				dragSideBarListener.onSideBarOpened();
			}
		}

		isStatusOpen = true;
	}

	/**
	 * execute whole close operation from open to close
	 * @param withAnimation
	 */
	public void forceDismiss(boolean withAnimation) {
		if (isStatusOpen) {
			if (withAnimation) {
				new AnimationTask(ANIM_CLOSE).execute(0f, (float) getWidth());
			}
			else {
				dismiss();
			}
		}
	}

	/**
	 * called by DragSideBarTrigger and this view, in process that handle ACTION_UP touch event
	 * @param withAnimation
	 */
	public void dismiss(boolean withAnimation) {

		Log.d(TAG, "dismiss offset=" + offset);
		if (withAnimation) {
			new AnimationTask(ANIM_CLOSE).execute(offset - space, (float) getWidth());
		}
		else {
			dismiss();
		}
	}

	private void dismiss() {
		ViewHelper.setTranslationX(this, getWidth());
		setBackgroundAlpha(getWidth());

		//从打开到关闭状态，才通知listener关闭
		if (isStatusOpen) {
			if (dragSideBarListener != null) {
				dragSideBarListener.onSideBarClosed();
			}
		}

		isStatusOpen = false;
	}

	/**
	 * 打开后以及打开过程中整个屏幕背景透明度变化
	 * @param view
	 */
	public void setShadowView(View view) {
		shadowView = view;
	}

	public void setBackgroundAlpha(float offsetX) {
		offset = offsetX;
		if (shadowView != null) {
			float f = offsetX / getWidth();
			int alpha = (int) (BK_ALPHA_MAX - BK_ALPHA_MAX * f);
			int color = Color.argb(alpha, 0, 0, 0);
			shadowView.setBackgroundColor(color);
			if (alpha != 0 && shadowView.getVisibility() == View.GONE) {
				shadowView.setVisibility(View.VISIBLE);
			}
			else {
				if (alpha == 0 && shadowView.getVisibility() == View.VISIBLE) {
					shadowView.setVisibility(View.GONE);
				}
			}
		}
	}

	/**
	 * 是否正在执行打开、关闭的收尾动画，期间需要禁止一切拖动touch事件
	 * @return
	 */
	public boolean isAnimming() {
		return isAnimming;
	}

	/**
	 * The animation started in the end of hands up
	 * @author JingYang
	 *
	 */
	private class AnimationTask extends AsyncTask<Float, Float, Float> {

		private int MAX_TIME;
		private int animMode;

		public AnimationTask(int mode) {
			animMode = mode;
			MAX_TIME = getContext().getResources().getInteger(R.integer.dragsidebar_anim_time);
		}

		@Override
		protected void onPreExecute() {
			isAnimming = true;
			super.onPreExecute();
		}

		@Override
		protected Float doInBackground(Float... params) {
			float distance = Math.abs(params[0] - params[1]);
			float maxDis = getWidth() - leftPadding;
			float time = distance / maxDis * MAX_TIME;
			int count = (int) (distance / 3);
			if (count > 0) {
				time = time / (float)count;

				float trans = params[0];
				int step = 3;
				if (params[1] < params[0]) {
					step = -3;
				}

				if (DEBUG) {
					Log.d(TAG, "AnimationTask from " + params[0] + " to " + params[1]
							+ ", count=" + count + ", time=" + time + ", step=" + step);
				}

				for (int i = 0; i < count; i ++) {
					trans += step;
					publishProgress(trans);
					try {
						Thread.sleep((long) time);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			return params[1];
		}

		@Override
		protected void onPostExecute(Float result) {
			if (animMode == ANIM_SHOW) {
				show();
			}
			else {
				dismiss();
			}
			isAnimming = false;
			super.onPostExecute(result);
		}

		@Override
		protected void onProgressUpdate(Float... values) {

			ViewHelper.setTranslationX(DragSideBar.this, values[0]);

			if (DEBUG) {
				Log.d(TAG, "onProgressUpdate setTranslation " + values[0]);
			}
			super.onProgressUpdate(values);
		}

	}

	public void onConfigurationChanged() {
		if (!isStatusOpen) {
			initParams();
			requestLayout();
		}
	}
}
