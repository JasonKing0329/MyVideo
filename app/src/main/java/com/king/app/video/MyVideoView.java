package com.king.app.video;

import com.king.app.video.controller.ScreenUtils;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.VideoView;

public class MyVideoView extends VideoView {

	public static final float FLAG_MATCHSCREEN = -1;
	
	private final String TAG = "MyVideoView";
	private int mVideoWidth;
	private int mVideoHeight;
	private boolean autoMatchScreen;
	
	public MyVideoView(Context context) {
		super(context);
	}

	public MyVideoView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MyVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	
	public void setVideoWidthHeight(int width, int height) {
		autoMatchScreen = false;
		mVideoWidth = width;
		mVideoHeight = height;

		Log.d(TAG, "setVideoWidthHeight w=" + mVideoWidth + ", h=" + mVideoHeight);
		fitVideoToScreen();
		requestLayout();
	}
	public void setVideoMatchScreen() {
		autoMatchScreen = true;
		requestLayout();
	}

	private void fitVideoToScreen() {
		int screenWidth = ScreenUtils.getScreenWidth(getContext());
		int screenHeight = ScreenUtils.getScreenHeight(getContext());
		if (mVideoWidth >= screenWidth) {
			float factor = (float) mVideoWidth / (float) screenWidth;
			mVideoWidth = screenWidth;
			
			mVideoHeight = (int) ((float) mVideoHeight / factor);
			if (mVideoHeight > screenHeight) {
				factor = (float) mVideoHeight / (float) screenHeight;
				mVideoHeight = screenHeight;

				mVideoWidth = (int) ((float) mVideoWidth / factor);
			}
		}
		else if (mVideoHeight > screenHeight) {
			float factor = (float) mVideoHeight / (float) screenHeight;
			mVideoHeight = screenHeight;
			
			mVideoWidth = (int) ((float) mVideoWidth / factor);
		}
		
		Log.d(TAG, "fitVideoToScreen w=" + mVideoWidth + ", h=" + mVideoHeight);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		
		if (!autoMatchScreen) {
	        widthMeasureSpec = MeasureSpec.makeMeasureSpec(mVideoWidth, MeasureSpec.EXACTLY);
	        heightMeasureSpec = MeasureSpec.makeMeasureSpec(mVideoHeight, MeasureSpec.EXACTLY);
		}
        
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

}
