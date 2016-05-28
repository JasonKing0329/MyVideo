package com.king.app.video;

import java.util.Timer;
import java.util.TimerTask;

import com.king.app.video.controller.Constants;
import com.king.app.video.controller.DisplayHelper;
import com.king.app.video.controller.ObjectCache;
import com.king.app.video.controller.VideoController;
import com.king.app.video.controller.VideoService;
import com.king.app.video.customview.DragSideBar;
import com.king.app.video.customview.DragSideBarTrigger;
import com.king.app.video.customview.DragSideBar.DragSideBarListener;
import com.king.app.video.data.personal.DatabaseInfor;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.VideoView;

public class VideoActivity extends Activity implements OnClickListener
		, OnSeekBarChangeListener, VideoService, DragSideBarListener{

	private final String TAG = "VideoActivity";

	private VideoController videoController;

	/** 底部功能布局 **/
	private RelativeLayout controlLayout;
	/** 播放、暂停、快进、快退控制按钮布局 **/
	private LinearLayout secCtrlLayout;
	/** 底部各功能按钮 **/
	private ImageView backButton, stopButton, playButton, pauseButton
			, forwardButton, volumnButton, moreButton;
	/** 视频进度 **/
	private SeekBar seekBar;
	/** 视频当前播放时长与总时长布局 **/
	private LinearLayout secTimeLayout;
	/** 视频当前播放时长与总时长 **/
	private TextView durationText, currentText;

	/** 顶部title view与播放列表控制按钮 **/
	private RelativeLayout topBarLayout;
	/** 视频名称 **/
	private TextView titleView;
	/** 打开/关闭播放列表按钮 **/
	private ImageView sideCtrlButton;

	/*******Play list********/
	private DragSideBar playListSideBar;
	private DragSideBarTrigger dragSideBarTrigger;
	private PlayListManager playListManager;
	/** 播放列表隐藏时，拖动播放列表出现的区域 **/
	private View touchSideView;

	private Handler playHandler;
	private TimeProgressor timeProgressor;

	private final int TIME_DELAY = 1000;
	private final int TIME_OVER_RESTART = 100;
	private final int TIME_DISP_CTRLBAR = 5000;

	private int orientation;

	private SoundPopup soundPopup;
	private MorePopup morePopup;
	private ScreenshotDialog showImageDialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.video_play);
		orientation = getResources().getConfiguration().orientation;

		controlLayout = (RelativeLayout) findViewById(R.id.video_controlbar);
		topBarLayout = (RelativeLayout) findViewById(R.id.video_topbar);
		secCtrlLayout = (LinearLayout) findViewById(R.id.video_sec_control);
		secTimeLayout = (LinearLayout) findViewById(R.id.video_sec_time);
		seekBar = (SeekBar) findViewById(R.id.video_seekbar);
		durationText = (TextView) findViewById(R.id.video_duration);
		currentText = (TextView) findViewById(R.id.video_current);
		backButton = (ImageView) findViewById(R.id.video_btn_back);
		stopButton = (ImageView) findViewById(R.id.video_btn_stop);
		playButton = (ImageView) findViewById(R.id.video_btn_play);
		pauseButton = (ImageView) findViewById(R.id.video_btn_pause);
		forwardButton = (ImageView) findViewById(R.id.video_btn_forward);
		moreButton = (ImageView) findViewById(R.id.video_btn_more);
		volumnButton = (ImageView) findViewById(R.id.video_btn_volumn);
		titleView = (TextView) findViewById(R.id.video_title);
		sideCtrlButton = (ImageView) findViewById(R.id.video_side_ctrl);

		backButton.setOnClickListener(this);
		stopButton.setOnClickListener(this);
		playButton.setOnClickListener(this);
		pauseButton.setOnClickListener(this);
		forwardButton.setOnClickListener(this);
		volumnButton.setOnClickListener(this);
		moreButton.setOnClickListener(this);
		sideCtrlButton.setOnClickListener(this);

		if (Application.isLollipop()) {
			//ripple_borderless_white, this type in landscape mode,
			//it will be covered by video view. so strange
			backButton.setBackgroundResource(R.drawable.ripple_white);
			stopButton.setBackgroundResource(R.drawable.ripple_white);
			playButton.setBackgroundResource(R.drawable.ripple_white);
			pauseButton.setBackgroundResource(R.drawable.ripple_white);
			forwardButton.setBackgroundResource(R.drawable.ripple_white);
			volumnButton.setBackgroundResource(R.drawable.ripple_white);
			moreButton.setBackgroundResource(R.drawable.ripple_white);
		}

		initPlayList();

		Uri uri = getIntent().getData();
		String id = getIntent().getStringExtra(Constants.PLAY_VIDEO_ID);
		boolean fromFileSystem = (id == null);
		playVideo(uri, id, fromFileSystem, false);

		adjustControlBar();

		AudioManager audioManager = (AudioManager) getSystemService(
				Context.AUDIO_SERVICE);
		onMute(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0);
	}

	/**
	 *
	 * @param uri 从文件系统点击视频文件打开
	 * @param id 从本程序视频列表打开
	 * @param fromFileSystem 从文件系统点击视频文件打开, id=null
	 * @param fromList 从播放列表点击/顺序打开
	 */
	public void playVideo(Uri uri, String id, boolean fromFileSystem, boolean fromList) {
		videoController = new VideoController(this, (MyVideoView) findViewById(R.id.video_playview));
		//videoController.setVideoPath(path);
		videoController.setVideoUri(uri);
		videoController.setVideoService(this);
		if (id == null) {//

			if (fromFileSystem) {
				DatabaseInfor.prepare(this);//一定要执行这个，否则数据库位置没有加载上
			}
			videoController.loadVideoDataByPath();

			if (fromFileSystem) {
				touchSideView.setVisibility(View.GONE);
				sideCtrlButton.setVisibility(View.GONE);
				playListManager.disable();
			}
		}
		else {//从本程序视频列表打开
			videoController.loadVideoDataById(id);
		}
		videoController.prepareVideo();
		videoController.play();

		titleView.setText(videoController.getVideoData().getName());

		playButton.setVisibility(View.GONE);
		pauseButton.setVisibility(View.VISIBLE);
		seekBar.setOnSeekBarChangeListener(this);
	}

	private void initPlayList() {
		touchSideView = findViewById(R.id.video_touch_side);
		playListSideBar = (DragSideBar) findViewById(R.id.video_playlist);
		playListSideBar.setDragSideBarListener(this);

		dragSideBarTrigger = new DragSideBarTrigger(this, playListSideBar);
		playListManager = new PlayListManager(this, playListSideBar);
		touchSideView.setOnTouchListener(sideTouchListener);
		if (ObjectCache.getVideoList() != null) {
			String videoId = getIntent().getStringExtra(Constants.PLAY_VIDEO_ID);
			playListManager.initListData(ObjectCache.getVideoList(), videoId, ObjectCache.getVideoOrder().getId());
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {

		if (newConfig.orientation != orientation) {
			orientation = newConfig.orientation;
			adjustControlBar();
			if (soundPopup != null) {
				soundPopup.dismiss();
			}
			if (morePopup != null) {
				morePopup.dismiss();
			}

//			LayoutParams params = controlLayout.getLayoutParams();
//			params.width = LayoutParams.MATCH_PARENT;
//			params.width = LayoutParams.WRAP_CONTENT;
//			controlLayout.setLayoutParams(params);
			playListManager.onConfigurationChanged(orientation);
		}
		super.onConfigurationChanged(newConfig);
	}

	/**
	 * 触发sidebar的touch事件
	 */
	OnTouchListener sideTouchListener = new OnTouchListener() {

		@SuppressLint("ClickableViewAccessibility")
		@Override
		public boolean onTouch(View view, MotionEvent event) {
			if (dragSideBarTrigger.onTriggerTouch(event)) {
				return true;
			}
			return true;
		}
	};

	private void adjustControlBar() {
		if (!DisplayHelper.isTabModel(this)) {
			if (orientation == Configuration.ORIENTATION_PORTRAIT) {
				RelativeLayout.LayoutParams params =
						(RelativeLayout.LayoutParams) secCtrlLayout.getLayoutParams();
				params.addRule(RelativeLayout.BELOW, secTimeLayout.getId());
				params = (RelativeLayout.LayoutParams) volumnButton.getLayoutParams();
				params.addRule(RelativeLayout.BELOW, secTimeLayout.getId());
				params = (RelativeLayout.LayoutParams) moreButton.getLayoutParams();
				params.addRule(RelativeLayout.BELOW, secTimeLayout.getId());

				params = (RelativeLayout.LayoutParams) secTimeLayout.getLayoutParams();
				params.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
				params.topMargin = 0;
				params.bottomMargin = 0;
			}
			else {
				RelativeLayout.LayoutParams params =
						(RelativeLayout.LayoutParams) secCtrlLayout.getLayoutParams();
				params.removeRule(RelativeLayout.BELOW);
				params.addRule(RelativeLayout.BELOW, seekBar.getId());
				params = (RelativeLayout.LayoutParams) volumnButton.getLayoutParams();
				params.addRule(RelativeLayout.BELOW, seekBar.getId());
				params = (RelativeLayout.LayoutParams) moreButton.getLayoutParams();
				params.addRule(RelativeLayout.BELOW, seekBar.getId());

				params = (RelativeLayout.LayoutParams) secTimeLayout.getLayoutParams();
				params.height = getResources().getDimensionPixelSize(R.dimen.video_control_btn_width);
				params.topMargin = getResources().getDimensionPixelSize(R.dimen.video_control_btn_top_margin);
				params.bottomMargin = getResources().getDimensionPixelSize(R.dimen.video_control_btn_bottom_margin);
			}
		}

		if (!playListSideBar.isOpen()) {
			startControlBarTimer();
		}
	}

	public void pause() {
		videoController.pause();
		playHandler.removeCallbacks(timeProgressor);
		playButton.setVisibility(View.VISIBLE);
		pauseButton.setVisibility(View.GONE);
		if (videoController != null) {
			videoController.savePlayPosition();
		}
	}

	@Override
	public void onClick(View view) {
		if (view == backButton) {
			if (!videoController.backward()) {
				Toast.makeText(this, R.string.video_no_backward, Toast.LENGTH_SHORT).show();
			}
		}
		else if (view == stopButton) {
			videoController.stop();
			stopPlay();
		}
		else if (view == playButton) {
			videoController.play();
			playHandler.postDelayed(timeProgressor, TIME_DELAY);
			playButton.setVisibility(View.GONE);
			pauseButton.setVisibility(View.VISIBLE);
		}
		else if (view == pauseButton) {
			pause();
		}
		else if (view == forwardButton) {
			if (!videoController.forward()) {
				Toast.makeText(this, R.string.video_no_forward, Toast.LENGTH_SHORT).show();
			}
		}
		else if (view == volumnButton) {
			soundPopup = new SoundPopup(this, this);
			int location[] = new int[2];
			volumnButton.getLocationInWindow(location);
			soundPopup.showAt(location[0], location[1], volumnButton.getWidth());
		}
		else if (view == moreButton) {
			morePopup = new MorePopup(this, this);
			int location[] = new int[2];
			volumnButton.getLocationInWindow(location);
			morePopup.showAt(location[0], location[1], volumnButton.getWidth());
		}
		else if (view == sideCtrlButton) {
			playListManager.onSideCtrlClicked(sideCtrlButton);
		}
	}

	@Override
	protected void onRestart() {

		if (!videoController.isPaused()) {
			if (playHandler != null) {
				playHandler.postDelayed(timeProgressor, TIME_DELAY);
			}
			videoController.play();
		}
		Log.d(TAG, "onRestart");
		super.onRestart();
	}

	@Override
	protected void onResume() {

		Log.d(TAG, "onResume");
		super.onResume();
	}

	@Override
	protected void onPause() {
		if (playHandler != null) {
			playHandler.removeCallbacks(timeProgressor);
		}
		videoController.onPause();
		Log.d(TAG, "onPause");
		super.onPause();
	}

	@Override
	protected void onStop() {

		Log.d(TAG, "onStop");
		if (videoController != null) {
			videoController.savePlayPosition();
		}
		super.onStop();
	}

	@Override
	public void onBackPressed() {
		if (playListManager != null) {
			if (playListManager.onBackPressed()) {
				return;
			}
		}
//		Intent intent = new Intent();
//		intent.putExtra(Constants.PLAY_VIDEO_TIME_UPDATE, (int) videoController.getCurrentTime());
//		setResult(Constants.PLAY_VIDEO_RESULT_ID, intent);
		super.onBackPressed();
	}

	@Override
	protected void onDestroy() {

		Log.d(TAG, "onDestroy");
		videoController.destroy();
		super.onDestroy();
	}

	public void destroy() {
		if (videoController != null) {
			videoController.destroy();
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		Log.d(TAG, "onProgressChanged " + progress);
		if (fromUser) {
			Log.d(TAG, "onProgressChanged fromUser " + progress);
			videoController.seekTo(progress);
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {

	}

	@Override
	public void onStopTrackingTouch(SeekBar arg0) {

	}

	private class TimeProgressor implements Runnable {

		@Override
		public void run() {
			changeProgress();
			playHandler.postDelayed(timeProgressor, TIME_DELAY);
		}

	}
	private class RestartTask implements Runnable {

		@Override
		public void run() {
			currentText.setText("00:00:00");
			seekBar.setProgress(0);
		}

	}


	public void changeProgress() {
		Log.d(TAG, "changeProgress");
		currentText.setText(videoController.getCurrentTimeString());
		seekBar.setProgress((int) videoController.getCurrentTime());
	}

	@Override
	public void onClickVideoView(VideoView view) {
		changeControlBarStatus();
		if (controlLayout.getVisibility() == View.VISIBLE) {
			startControlBarTimer();
		}
	}

	private boolean isTimeCounting;
	private void startControlBarTimer() {
		isTimeCounting = true;
		Timer timer = new Timer();
		timer.schedule(new ControlBarTask(), TIME_DISP_CTRLBAR);
	}

	private class ControlBarTask extends TimerTask {

		@Override
		public void run() {
			if (isTimeCounting) {
				delayHandler.sendEmptyMessage(1);
				isTimeCounting = false;
			}
		}

	}

	@SuppressLint("HandlerLeak")
	private Handler delayHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 1) {
				//当在时间范围内手动隐藏control bar后，时间到了就不再重复执行一次动画了
				if (controlLayout.getVisibility() == View.VISIBLE) {
					isTimeCounting = false;
					changeControlBarStatus();
				}
			}
			super.handleMessage(msg);
		}

	};

	private void changeControlBarStatus() {
		if (controlLayout.getVisibility() == View.VISIBLE) {
			controlLayout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.video_control_disappear));
			controlLayout.setVisibility(View.INVISIBLE);
			topBarLayout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.video_topbar_disappear));
			topBarLayout.setVisibility(View.INVISIBLE);
		}
		else {
			controlLayout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.video_control_appear));
			controlLayout.setVisibility(View.VISIBLE);
			topBarLayout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.video_topbar_appear));
			topBarLayout.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onPlayForward(VideoView view, int progress) {
		Log.d(TAG, "onPlayForward " + progress);
		//play的情况timeProgressor会通知更新
		if (videoController.isPaused()) {
			changeProgress();
		}
		String string = getResources().getString(R.string.video_forward_option);
		string = string.replace("%s", "" + (progress / 1000));
		Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onPlayBackward(VideoView view, int progress) {
		Log.d(TAG, "onPlayBackward " + progress);
		//play的情况timeProgressor会通知更新
		if (videoController.isPaused()) {
			changeProgress();
		}
		String string = getResources().getString(R.string.video_backward_option);
		string = string.replace("%s", "" + (progress / 1000));
		Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onCompletion(VideoView view, MediaPlayer player) {
		Log.d(TAG, "onCompletion");
		stopPlay();
		playListManager.playNext();
	}

	private void stopPlay() {
		if (playHandler != null) {
			playHandler.removeCallbacks(timeProgressor);
			playButton.setVisibility(View.VISIBLE);
			pauseButton.setVisibility(View.GONE);
			playHandler.postDelayed(new RestartTask(), TIME_OVER_RESTART);
		}
	}

	@Override
	public void onPrepared(VideoView view, MediaPlayer player) {
		Log.d(TAG, "onPrepared");
		durationText.setText(videoController.getDuration());
		currentText.setText("00:00:00");
		seekBar.setMax((int) videoController.getDurationTime());

		playHandler = new Handler();
		timeProgressor = new TimeProgressor();
		playHandler.postDelayed(timeProgressor, TIME_DELAY);
	}

	@Override
	public void onMute(boolean mute) {
		if (mute) {
			volumnButton.setImageResource(R.drawable.video_ic_mute_mtrl);
		}
		else {
			volumnButton.setImageResource(R.drawable.video_ic_volume_mtrl);
		}
	}

	@Override
	public void onScreenshot() {
		pause();
		showImageDialog = new ScreenshotDialog(this);
		showImageDialog.setImage(videoController.screenshot());
		//showImageDialog.setImage(null);
		showImageDialog.show();
	}

	@Override
	public void onError(VideoView view, MediaPlayer player, int fwError,
						int impError) {
		Log.d(TAG, "onError " + fwError + ", " + impError);
		if (fwError == MediaPlayer.MEDIA_ERROR_UNKNOWN) {
			new SimpleDialogManager().openWarningDialog(this, R.string.video_dlg_not_support_play
					, false, new SimpleDialogManager.OnDialogActionListener() {

						@Override
						public void onOk(String name) {
							finish();
						}

						@Override
						public void onDismiss() {
							finish();
						}
					});
		}
	}

	@Override
	public void onUpdateVideoSize(float flag) {
		videoController.updateVideoSize(flag);
	}

	@Override
	public void onSideBarClosed() {
		startControlBarTimer();
	}

	@Override
	public void onSideBarOpened() {
		isTimeCounting = false;
		controlLayout.setVisibility(View.VISIBLE);
		topBarLayout.setVisibility(View.VISIBLE);
	}
}
