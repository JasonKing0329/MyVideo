package com.king.app.video;

import com.king.app.video.controller.DisplayHelper;
import com.king.app.video.controller.ScreenUtils;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;

public abstract class VideoDialog extends Dialog {

	private Context mContext;
	private View rootView;
	private LinearLayout container;
	private LayoutParams windowParams;

	private Point startPoint, touchPoint;
	private boolean enableDrag = false;
	private boolean isZoom;
	private boolean enableZoom = false;
	private final int ZOOM_AREA = 20;
	private final int ZOOM_TOP = 1;
	private final int ZOOM_BOTTOM = 3;
	private int zoomDirection;

	private class Point {
		float x;
		float y;
	}

	public VideoDialog(Context context) {
		super(context, R.style.VedioDialog);
		mContext = context;
		setContentView(R.layout.video_dialog);

		container = (LinearLayout) findViewById(R.id.video_dialog_container);
		rootView = container.getRootView();
		windowParams = getWindow().getAttributes();
		touchPoint = new Point();
		startPoint = new Point();

		View view = getCustomView();
		if (view != null) {
			container.addView(view);
		}
	}

	public void applyGreyStyle() {
		rootView.setBackgroundColor(mContext.getResources().getColor(R.color.video_dialog_bk_grey));
	}

	public void applyTransparentStyle() {
		rootView.setBackgroundColor(mContext.getResources().getColor(R.color.video_dialog_bk_transp));
	}

	public void enableDrag() {
		enableDrag = true;
	}

	protected abstract View getCustomView();

	/**
	 * 设置dialog的偏移位置
	 * @param x 负数向左，正数向右
	 * @param y 负数向上，正数向下
	 */
	public void setPositionOffset(int x, int y) {

		windowParams.x = x;
		windowParams.y = y;
		getWindow().setAttributes(windowParams);
	}

	public void setWidth(int w) {
		windowParams.width = w;
		getWindow().setAttributes(windowParams);
	}

	public void setHeight(int h) {
		windowParams.height = h;
		getWindow().setAttributes(windowParams);
	}

	@Override
	/**
	 * notice: getRawX/Y是相对屏幕的坐标，getX/Y是相对控件的
	 * 要实现拖动效果，只能用getRawX/Y，用getX/Y会出现拖动不流畅并且抖动的效果
	 * (from internet: getX getY获取的是相对于child 左上角点的 x y 当第一次获取的时候通过layout设置了child一个新的位置 马上 再次获取x y时就会变了 变成了 新的x y
	 * 然后马上layout 然后又获取了新的x y又。。。。所以会看到 一个view不断地在屏幕上闪来闪去)
	 */
	public boolean onTouchEvent(MotionEvent event) {
		if (enableDrag || enableZoom) {
			int action = event.getAction();
			switch (action) {
				case MotionEvent.ACTION_DOWN:
					if (enableZoom) {
						float xToView = event.getX();
						float yToView = event.getY();
						if (yToView < ZOOM_AREA) {
							isZoom = true;
							zoomDirection = ZOOM_TOP;
						}
						else if (yToView > windowParams.height - ZOOM_AREA) {
							isZoom = true;
							zoomDirection = ZOOM_BOTTOM;
						}
						Log.d("VideoDialog", "ACTION_DOWN xToView=" + xToView + ", yToView=" + yToView);
					}

					float x = event.getRawX();//
					float y = event.getRawY();
					startPoint.x = x;
					startPoint.y = y;
					Log.d("CustomDialog", "ACTION_DOWN x=" + x + ", y=" + y);
					break;
				case MotionEvent.ACTION_MOVE:
					x = event.getRawX();
					y = event.getRawY();
					touchPoint.x = x;
					touchPoint.y = y;
					float dx = touchPoint.x - startPoint.x;
					float dy = touchPoint.y - startPoint.y;

					if (enableZoom && isZoom) {
						zoom((int)dy);
					}
					else {
						move((int)dx, (int)dy);
					}

					startPoint.x = x;
					startPoint.y = y;
					break;
				case MotionEvent.ACTION_UP:
					if (enableZoom) {
						isZoom = false;
					}
					break;

				default:
					break;
			}
		}
		return super.onTouchEvent(event);
	}

	private void move(int x, int y) {

		windowParams.x += x;
		windowParams.y += y;
		getWindow().setAttributes(windowParams);//must have
	}

	private void zoom(int dy) {
		if (zoomDirection == ZOOM_TOP) {
			windowParams.height -= dy;
			windowParams.y += dy;
		}
		else if (zoomDirection == ZOOM_BOTTOM) {
			windowParams.height += dy;
		}
		getWindow().setAttributes(windowParams);

		if (zoomDirection == ZOOM_BOTTOM) {
			windowParams.y -= (dy / 2);
			getWindow().setAttributes(windowParams);
		}
		Log.d("VideoDialog", "zoom windowParams.height=" + windowParams.height + " dy=" + dy
				+ " windowParams.y=" + windowParams.y);
	}

	/**
	 * call this to set suitable dialog size match for screen orientation
	 */
	public void computeHeight() {
		if (getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			int screenHeight = ScreenUtils.getScreenHeight(getContext());
			if (DisplayHelper.isTabModel(getContext())) {
				setHeight(screenHeight * 3 / 4);
			}
			else {
				setHeight(screenHeight * 4 / 5);
			}
		}
		else {
			int screenHeight = ScreenUtils.getScreenHeight(getContext());
			if (DisplayHelper.isTabModel(getContext())) {
				setHeight(screenHeight * 2 / 3);
			}
			else {
				setHeight(screenHeight - 80);
			}
		}
	}

}
