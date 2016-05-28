package com.king.app.video.customview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.king.app.video.Application;
import com.king.app.video.R;
import com.king.app.video.controller.ScreenUtils;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler.Callback;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

public class ScrollTab extends HorizontalScrollView implements Callback
		, OnClickListener{

	public static final int MSG_DATA_SET_CHANGED = 100;

	private final String TAG = "ScrollTab";
	private final int MAX_TAB_IN_SCREEN_VER = 3;
	private final int MAX_TAB_IN_SCREEN_HOR = 5;

	private int nTabInScreen;

	private AbstractScrollTabAdapter mAdapter;
	private LinearLayout container;

	private List<View> convertViewList;
	private int itemWidth;
	private ScrollParams scrollParams;

	private OnTabSelectListener onTabSelectListener;

	private boolean enable = true;

	public interface OnTabSelectListener {
		public void onSelect(View view, int position);
	}

	public ScrollTab(Context context) {
		super(context);
		init();
	}
	public ScrollTab(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	public ScrollTab(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init() {
		container = new LinearLayout(getContext());
		addView(container, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT
				, ViewGroup.LayoutParams.MATCH_PARENT));
		scrollParams = new ScrollParams();
		nTabInScreen = MAX_TAB_IN_SCREEN_VER;
	}

	public void setOnTabSelectListener(OnTabSelectListener listener) {
		onTabSelectListener = listener;
	}

	public void setAdapter(AbstractScrollTabAdapter adapter) {
		mAdapter = adapter;
		mAdapter.setCallback(this);

		container.removeAllViews();
		convertViewList = new ArrayList<View>();
		for (int i = 0; i < mAdapter.getCount(); i ++) {
			convertViewList.add(null);
		}

		if (getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {

			nTabInScreen = MAX_TAB_IN_SCREEN_HOR;
		}

		int total = mAdapter.getCount();
		if (mAdapter.getCount() > 0) {

			int start = 0;
			int end = nTabInScreen - 1;

			updateItemWidth();
			if (total > nTabInScreen) {
				end = nTabInScreen;//for enable scroll, init max + 1
			}
			else {
				end = total - 1;
			}

			scrollParams.start = 0;
			scrollParams.end = end;
			initView(start, end);
		}
	}

	private void initView(int start, int end) {

		Log.d(TAG, "initView " + start + ", " + end);
		for (int i = start; i <= end && i < mAdapter.getCount(); i ++) {
			View view = mAdapter.getView(i, convertViewList.get(i));
			if (convertViewList.get(i) == null) {
				convertViewList.set(i, view);
				addTab(view);
			}
		}
	}

	private void addTab(View view) {
		Log.d(TAG, "addTab itemWidth=" + itemWidth);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(itemWidth
				, LinearLayout.LayoutParams.MATCH_PARENT);
		container.addView(view, params);
		//不能用setTag，因为在adapter中也会对view进行setTag操作
		//view.setTag(index);
		view.setClickable(true);
		view.setFocusable(true);
		view.setOnClickListener(this);
		if (Application.isLollipop()) {
			view.setBackgroundResource(R.drawable.item_background_material);
		}
	}

	private class ScrollParams {
		int start;
		int end;
	}



	/**
	 * 可以再这里处理屏幕旋转事件
	 */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		Log.d(TAG, "onSizeChanged");
		boolean orientationChanged = false;
		if (getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			if (nTabInScreen != MAX_TAB_IN_SCREEN_HOR) {
				orientationChanged = true;
			}
			nTabInScreen = MAX_TAB_IN_SCREEN_HOR;
		}
		else {
			if (nTabInScreen != MAX_TAB_IN_SCREEN_VER) {
				orientationChanged = true;
			}
			nTabInScreen = MAX_TAB_IN_SCREEN_VER;
		}

		if (orientationChanged) {

			//for an example: total item = 8, in vertical mode, it initial 4 items
			//when orientation changed, it should initial 5 items
			if (container.getChildCount() < nTabInScreen
					&& mAdapter.getCount() > nTabInScreen) {
				Collections.fill(convertViewList, null);
				container.removeAllViews();
				initView(0, nTabInScreen);//one more than nTabInScreen
			}

			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					resetTabWidth();
					updateScrollParams();
				}
			}, 100);
		}
		super.onSizeChanged(w, h, oldw, oldh);
	}

	protected void updateScrollParams() {
		scrollParams.start = getScrollX() / itemWidth;
		scrollParams.end = (getScrollX() + getWidth()) / itemWidth;
	}

	private void resetTabWidth() {
		updateItemWidth();
		Log.d(TAG, "resetTabWidth " + itemWidth);

		LinearLayout.LayoutParams params = null;
		for (int i = 0; i < convertViewList.size(); i ++) {
			View view = convertViewList.get(i);
			if (view != null) {
				params = (LinearLayout.LayoutParams) view.getLayoutParams();
				params.width = itemWidth;
			}
		}
		container.requestLayout();//must delay execute, otherwise item width will not change in screen
	}

	private void updateItemWidth() {
		if (mAdapter.getCount() > nTabInScreen) {
			itemWidth = ScreenUtils.getScreenWidth(getContext()) / nTabInScreen;
		}
		else {
			itemWidth = ScreenUtils.getScreenWidth(getContext()) / mAdapter.getCount();
		}
	}
	@Override
	public void onScrollChanged(int x, int y, int oldx, int oldy) {
		if (x - oldx > 0) {//scroll to right
			if ((x + 10) / itemWidth != scrollParams.start) {
				if (scrollParams.end + 1 < mAdapter.getCount()) {
					scrollParams.end ++;
					initViewAt(scrollParams.end);
				}
				scrollParams.start ++;
			}
		}
		else {
			if ((x - 10) / itemWidth != scrollParams.start) {
				if (scrollParams.start - 1 >= 0) {
					scrollParams.start --;
					initViewAt(scrollParams.start);
				}
				scrollParams.end --;
			}
		}


	}

	private void initViewAt(int i) {
		Log.d(TAG, "initViewAt " + i);
		if (i >= 0 && i < mAdapter.getCount()) {
			View view = mAdapter.getView(i, convertViewList.get(i));
			if (convertViewList.get(i) == null) {
				convertViewList.set(i, view);
				addTab(view);
			}
		}
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case MSG_DATA_SET_CHANGED:
				updateItemWidth();
				int start = getScrollX() / itemWidth;
				int end = (getScrollX() + getWidth()) / itemWidth;;
				if (end >= mAdapter.getCount()) {
					end = mAdapter.getCount() - 1;
				}
				if (start >= mAdapter.getCount()) {
					start = end - nTabInScreen;
				}
				if (start < 0) {
					start = 0;
				}
				initView(start, end);
				break;

			default:
				break;
		}
		return false;
	}
	@Override
	public void onClick(View view) {
		if (enable && onTabSelectListener != null) {
			int position = 0;
			for (int i = 0; i < convertViewList.size(); i ++) {
				if (view == convertViewList.get(i)) {
					position = i;
				}
			}
			Log.d(TAG, "onClick " + position);
			onTabSelectListener.onSelect(convertViewList.get(position), position);
		}
	}
	public void disable() {
		enable = false;
	}
	public void enable() {
		enable = true;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (enable) {
			return super.onInterceptTouchEvent(ev);
		}
		else {
			return false;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev)
	{
		if (enable) {
			return super.onTouchEvent(ev);
		}
		else {
			return false;
		}
	}
}
