package com.king.app.video.controller;

import com.king.app.video.MyVideoView;
import com.king.app.video.data.personal.VideoPersonalData;
import com.king.app.video.model.VideoData;
import com.king.app.video.setting.SettingProperties;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class VideoController implements OnPreparedListener, OnCompletionListener
		, OnErrorListener{

	private Context mContext;
	private VideoService videoService;
	private MyVideoView videoView;
	private int currentPosition;
	private String srcPath;
	private DataController dataController;
	
	private VideoData videoData;
	
	private boolean isPaused;
	
	public VideoController(Context context, MyVideoView videoView) {
		mContext = context;
		this.videoView = videoView;
		videoView.requestFocus();
		videoView.setOnPreparedListener(this);
		videoView.setOnCompletionListener(this);
		videoView.setOnTouchListener(new VideoTouchListener());
		videoView.setOnErrorListener(this);
		dataController = new DataController();
	}

	public void setVideoPath(String path) {
		srcPath = path;
		videoView.setVideoPath(path);
	}

	public void setVideoUri(Uri uri) {
		videoView.setVideoURI(uri);
		srcPath = uri.getPath();
	}

	public void play() {
		videoView.seekTo(currentPosition);
		videoView.start();
		isPaused = false;
	}

	public void pause() {
		currentPosition = videoView.getCurrentPosition();
		videoView.pause();
		isPaused = true;
	}

	public void stop() {
		videoView.pause();
		currentPosition = 0;
		isPaused = true;
	}

	public void seekTo(int progress) {
		videoView.seekTo(progress);
	}

	public void onPause() {
		if (videoView.isPlaying()) {
			currentPosition = videoView.getCurrentPosition();
			videoView.pause();
		}
	}
	
	public boolean isPaused() {
		return isPaused;
	}

	public String getDuration() {
		long time = videoView.getDuration();
		String duration = VideoFormatter.formatTime(time);
		return duration;
	}
	public long getDurationTime() {
		long time = videoView.getDuration();
		return time;
	}

	public String getCurrentTimeString() {
		currentPosition = videoView.getCurrentPosition();
		String duration = VideoFormatter.formatTime(currentPosition);
		return duration;
	}

	public long getCurrentTime() {
		currentPosition = videoView.getCurrentPosition();
		return currentPosition;
	}


	public void setVideoService(VideoService service) {
		videoService = service;
	}
	
	@Override
	public void onCompletion(MediaPlayer player) {
		currentPosition = 0;
		isPaused = true;
		if (videoService != null) {
			videoService.onCompletion(videoView, player);
		}
	}

	@Override
	public void onPrepared(MediaPlayer player) {
		if (videoService != null) {
			videoService.onPrepared(videoView, player);
		}
	}

	@Override
	public boolean onError(MediaPlayer player, int framework_err, int impl_err) {

		if (videoService != null) {
			videoService.onError(videoView, player, framework_err, impl_err);
		}
		return true;//if false, it'll show framework popup dialog
	}

	private class VideoTouchListener implements OnTouchListener {
		
		private final String TAG = "VideoTouchListener";
		private float lastX, lastY;
		private long downTime;
		@Override
		public boolean onTouch(View view, MotionEvent event) {

			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				lastX = event.getX();
				lastY = event.getY();
				downTime = System.currentTimeMillis();
				break;
			case MotionEvent.ACTION_MOVE:
				
				break;
			case MotionEvent.ACTION_UP:
				float x = event.getX();
				float y = event.getY();
				long time = System.currentTimeMillis();

				Log.d(TAG, "time:" + (time - downTime) + ", disX:" + (x - lastX) + ", disY:" + (y - lastY));
				if (x - lastX > 100 && y - lastY < 200 && y - lastY > -200) {
					//forward
					if (videoService != null) {
						int progress = getForward(x - lastX);
						if (progress != -1) {
							currentPosition += progress;
							videoView.seekTo(currentPosition);
							videoService.onPlayForward(videoView, progress);
						}
					}
				}
				else if (x - lastX < -100 && y - lastY < 200 && y - lastY > -200) {
					//backward
					if (videoService != null) {
						int progress = getBackward(lastX - x);
						if (progress != -1) {
							currentPosition -= progress;
							videoView.seekTo(currentPosition);
							videoService.onPlayBackward(videoView, progress);
						}
					}
				}
				
				if (time - downTime < 100) {
					if (x - lastX < 20 && x - lastX > -20 &&  y - lastY < 20 && y - lastY > -20) {
						//onClick
						if (videoService != null) {
							videoService.onClickVideoView(videoView);
						}
					}
				}
				break;

			default:
				break;
			}
			return true;
		}
		
	}

	public int getForward(float dis) {
		int total = videoView.getDuration();
		int remain = total - currentPosition;
		int factor = SettingProperties.getForwardUnit(mContext) * 1000;
		int disFactor = 100;
		int progress = ((int) dis / disFactor) * factor;
		if (progress < remain - factor) {
			return progress;
		}
		return -1;
	}

	public int getBackward(float dis) {
		int remain = currentPosition;
		int factor = SettingProperties.getForwardUnit(mContext) * 1000;
		int disFactor = 100;
		int progress = ((int) dis / disFactor) * factor;
		if (progress < remain - factor) {
			return progress;
		}
		return -1;
	}

	public boolean backward() {
		int factor = SettingProperties.getForwardUnit(mContext) * 1000;
		int remain = currentPosition;
		if (factor < remain - factor) {
			currentPosition -= factor;
			videoView.seekTo(currentPosition);
			if (videoService != null) {
				videoService.onPlayBackward(videoView, factor);
			}
			return true;
		}
		return false;
	}

	public boolean forward() {
		int total = videoView.getDuration();
		int remain = total - currentPosition;
		int factor = SettingProperties.getForwardUnit(mContext) * 1000;
		if (factor < remain - factor) {
			currentPosition += factor;
			videoView.seekTo(currentPosition);
			if (videoService != null) {
				videoService.onPlayForward(videoView, factor);
			}
			return true;
		}
		return false;
	}

	public Bitmap screenshot() {
		/**
		 * get bitmap from videoView.drawingCache is not available, it always show black
		 */
		//return ScreenUtils.snapShotView(videoView);
		return new DataController().getVideoFrameAt(srcPath, currentPosition);
	}

	public void loadVideoDataByPath() {
		videoData = dataController.queryVideoDataByPath(mContext, srcPath);
	}

	public void loadVideoDataById(String id) {
		videoData = dataController.queryVideoData(mContext, id);
	}

	public void prepareVideo() {
		videoView.setVideoMatchScreen();
		if (videoData != null) {
			VideoPersonalData personalData = videoData.getPersonalData();
			if (personalData != null) {
				currentPosition = personalData.getLastPlayPosition();
				seekTo(currentPosition);
			}
		}
	}

	public void savePlayPosition() {
		VideoPersonalData personalData = videoData.getPersonalData();
		personalData.setLastPlayPosition(currentPosition);
		dataController.updateVideoPersonalData(videoData);
	}

	public void updateVideoSize(float flag) {
		if (flag == MyVideoView.FLAG_MATCHSCREEN) {
			videoView.setVideoMatchScreen();
		}
		else {
			videoView.setVideoWidthHeight((int) (videoData.getWidth() * flag)
					, (int) (videoData.getHeight() * flag));
		}
	}

	public VideoData getVideoData() {
		
		return videoData;
	}

	public void destroy() {
		videoView.destroyDrawingCache();
	}
}
