package com.king.app.video;

import com.king.app.video.controller.DisplayHelper;
import com.king.app.video.controller.ScreenUtils;
import com.king.app.video.controller.VideoService;

import android.content.Context;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class SoundPopup extends VideoDialog 
		implements OnSeekBarChangeListener, OnClickListener{

	private int width, height;
	private AudioManager audioManager;
	private int maxVolumn, curVolumn;
	private SeekBar seekBar;
	private VideoService videoService;
	
	private TextView soundValueView;
	private CheckBox muteCheckBox;
	private int volumnBeforeMute;
	
	public SoundPopup(Context context, VideoService service) {
		super(context);
		videoService = service;
		applyGreyStyle();
		initVolumn();
	}
	
	private void initVolumn() {
		audioManager = (AudioManager) getContext().getSystemService(
				Context.AUDIO_SERVICE);
		maxVolumn = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		curVolumn = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		seekBar.setMax(maxVolumn);
		seekBar.setProgress(curVolumn);
		updateValueBar();
	}

	private void updateValueBar() {
		if (curVolumn == 0) {
			muteCheckBox.setChecked(true);
		}
		else {
			muteCheckBox.setChecked(false);
		}
		String text = getContext().getResources().getString(R.string.video_popup_sound_value);
		text = text.replace("%d", "" + curVolumn);
		soundValueView.setText(text);
	}

	public void showAt(int anchorX, int anchorY, int anchorWidth) {
		int screenWidth = ScreenUtils.getScreenWidth(getContext());
		int screenHeight = ScreenUtils.getScreenHeight(getContext());
		int x = anchorX + anchorWidth/2 - width / 2;
		int minRight = getContext().getResources().getDimensionPixelSize(R.dimen.video_popup_min_right);
		int vertical = getContext().getResources().getDimensionPixelSize(R.dimen.video_popup_min_vertical);
		if (x + width > screenWidth - minRight) {
			x = screenWidth - minRight - width - (screenWidth - width) / 2;
		}
		int y = anchorY - vertical - height - (screenHeight - height) / 2;
		setPositionOffset(x, y);
		show();
	}

	@Override
	protected View getCustomView() {
		if (DisplayHelper.isTabModel(getContext())
				|| getContext().getResources().getConfiguration().orientation
				== Configuration.ORIENTATION_LANDSCAPE) {
			width = ScreenUtils.getScreenWidth(getContext()) / 3;
		}
		else {
			width = ScreenUtils.getScreenWidth(getContext()) / 2;
		}
		height = getContext().getResources().getDimensionPixelSize(R.dimen.video_sound_popup_height);
		setWidth(width);
		setHeight(height);
		
		View view = LayoutInflater.from(getContext()).inflate(R.layout.video_popup_sound, null);
		seekBar = (SeekBar) view.findViewById(R.id.video_popup_seekbar);
		soundValueView = (TextView) view.findViewById(R.id.video_popup_sound_value);
		muteCheckBox = (CheckBox) view.findViewById(R.id.video_popup_sound_mute_check);
		seekBar.setOnSeekBarChangeListener(this);
		muteCheckBox.setOnClickListener(this);
		return view;
	}

	@Override
	public void onProgressChanged(SeekBar view, int progress, boolean fromUser) {
		if (fromUser) {
			curVolumn = progress;
			if (curVolumn == 0) {
				videoService.onMute(true);
			}
			else {
				videoService.onMute(false);
			}
			audioManager.setStreamVolume(AudioManager.STREAM_MUSIC
					, curVolumn, AudioManager.FLAG_PLAY_SOUND);
			updateValueBar();
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClick(View view) {
		if (view == muteCheckBox) {
			if (muteCheckBox.isChecked()) {
				volumnBeforeMute = curVolumn;
				curVolumn = 0;
				videoService.onMute(true);
			}
			else {
				curVolumn = volumnBeforeMute;
				videoService.onMute(false);
			}
			seekBar.setProgress(curVolumn);
			audioManager.setStreamVolume(AudioManager.STREAM_MUSIC
					, curVolumn, AudioManager.FLAG_PLAY_SOUND);
			updateValueBar();
		}
	}

}
