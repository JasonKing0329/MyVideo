package com.king.app.video.customview;

import com.king.app.video.controller.ScreenUtils;
import com.nineoldandroids.view.ViewHelper;

import android.content.Context;
import android.view.MotionEvent;

/**
 * 该类用于处理DragSideBar View之外的拖动事件
 * DragSideBar里的touch事件只能发生在DragSideBar的view区域内
 * 而程序初衷是在DragSideBar左侧的空白处触摸也能拖动DragSideBar
 * 因此，本类仅仅控制DragSideBar的打开过程，关闭过程由DragSideBar本身处理
 * @author JingYang
 *
 */
public class DragSideBarTrigger {

	private DragSideBar dragSideBar;

	private float offsetX, startX;
	private boolean isDragSide;
	private int screenWidth;

	public DragSideBarTrigger(Context context, DragSideBar dragSideBar) {
		this.dragSideBar = dragSideBar;
		screenWidth = ScreenUtils.getScreenWidth(context);
	}

	public boolean onTriggerTouch(MotionEvent event) {

		if (!dragSideBar.isAnimming()) {
			boolean result = false;

			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					startX = event.getRawX();
					if (startX > screenWidth - 50) {
						isDragSide = true;
					}

					break;
				case MotionEvent.ACTION_MOVE:
					if (isDragSide) {
						offsetX = event.getRawX() - dragSideBar.getPaddingLeft();
						if (offsetX < 0) {
							offsetX = 0;
						}
						ViewHelper.setTranslationX(dragSideBar, offsetX);
						dragSideBar.setBackgroundAlpha(offsetX);
					}
					break;
				case MotionEvent.ACTION_UP:
					if (isDragSide) {
						dragSideBarOver();
						isDragSide = false;
						result = true;//这里要单独return true否则触发view会触发ACTION_UP
					}
					break;
				case MotionEvent.ACTION_OUTSIDE:
					break;
			}

			dragSideBar.logParams();
			if (result) {
				return result;
			}

			return isDragSide;
		}
		return true;
	}

	private void dragSideBarOver() {
		int width = dragSideBar.getWidth();
		if (offsetX < width - (width - dragSideBar.getPaddingLeft()) / 2) {
			dragSideBar.show(true, offsetX);
		}
		else {
			dragSideBar.dismiss(true);
		}
	}

}
