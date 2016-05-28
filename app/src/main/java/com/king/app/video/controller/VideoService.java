package com.king.app.video.controller;

import android.media.MediaPlayer;
import android.widget.VideoView;

public interface VideoService {

	public void onMute(boolean mute);
	public void onScreenshot();
	public void onUpdateVideoSize(float flag);
	public void onClickVideoView(VideoView view);
	public void onPlayForward(VideoView view, int progress);
	public void onPlayBackward(VideoView view, int progress);
	public void onCompletion(VideoView view, MediaPlayer player);
	public void onPrepared(VideoView view, MediaPlayer player);
	public void onError(VideoView view, MediaPlayer player, int fwError, int impError);
}
