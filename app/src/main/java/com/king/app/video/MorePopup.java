package com.king.app.video;

import com.king.app.video.controller.DisplayHelper;
import com.king.app.video.controller.ScreenUtils;
import com.king.app.video.controller.VideoService;
import com.king.app.video.setting.SettingProperties;

import android.content.Context;
import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class MorePopup extends VideoDialog implements OnSeekBarChangeListener
		, OnClickListener{

	private final int MAX_UNIT = 10;
	
	private int width, height;
	private SeekBar seekBar;
	private TextView unitText, screenshotText;
	private VideoService videoService;

	private float[] screenSizeValues;
	private RadioButton[] screenSizeGroup;
	private RadioButton matchButton, originButton, size1_5Button, size2Button, size3Button;
	
	public MorePopup(Context context, VideoService service) {
		super(context);
		this.videoService = service;
		updateUnitText(SettingProperties.getForwardUnit(context));
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
		height = getContext().getResources().getDimensionPixelSize(R.dimen.video_more_popup_height);
		setWidth(width);
		setHeight(height);
		
		View view = LayoutInflater.from(getContext()).inflate(R.layout.video_popup_more, null);
		seekBar = (SeekBar) view.findViewById(R.id.video_popup_forward_unit_seekbar);
		unitText = (TextView) view.findViewById(R.id.video_popup_forrward_unit);
		screenshotText = (TextView) view.findViewById(R.id.video_popup_screenshot);
		matchButton = (RadioButton) view.findViewById(R.id.video_popup_screen_size_matchscreen);
		originButton = (RadioButton) view.findViewById(R.id.video_popup_screen_size_origin);
		size1_5Button = (RadioButton) view.findViewById(R.id.video_popup_screen_size_1_5);
		size2Button = (RadioButton) view.findViewById(R.id.video_popup_screen_size_2);
		size3Button = (RadioButton) view.findViewById(R.id.video_popup_screen_size_3);
		screenshotText.setOnClickListener(this);
		matchButton.setOnClickListener(this);
		originButton.setOnClickListener(this);
		size1_5Button.setOnClickListener(this);
		size2Button.setOnClickListener(this);
		size3Button.setOnClickListener(this);
		screenSizeGroup = new RadioButton[5];
		screenSizeValues = new float[5];
		screenSizeGroup[0] = matchButton;
		screenSizeValues[0] = MyVideoView.FLAG_MATCHSCREEN;
		screenSizeGroup[1] = originButton;
		screenSizeValues[1] = 1;
		screenSizeGroup[2] = size1_5Button;
		screenSizeValues[2] = 1.5f;
		screenSizeGroup[3] = size2Button;
		screenSizeValues[3] = 2;
		screenSizeGroup[4] = size3Button;
		screenSizeValues[4] = 3;
		if (DisplayHelper.isTabModel(getContext())
				&& getContext().getResources().getConfiguration().orientation
				== Configuration.ORIENTATION_LANDSCAPE) {
			LinearLayout layout = (LinearLayout) view.findViewById(R.id.video_popup_screen_size_container);
			layout.setOrientation(LinearLayout.HORIZONTAL);
		}
		matchButton.setChecked(true);
		
		seekBar.setOnSeekBarChangeListener(this);
		seekBar.setMax(MAX_UNIT);
		seekBar.setProgress(SettingProperties.getForwardUnit(getContext()));
		
		if (Application.isLollipop()) {
			screenshotText.setBackgroundResource(R.drawable.item_background_material);
		}
		
		return view;
	}

	@Override
	public void onProgressChanged(SeekBar view, int progress, boolean fromUser) {
		if (fromUser) {
			updateUnitText(progress);
			SettingProperties.saveForwardUnit(getContext(), progress);
		}
	}

	public void updateUnitText(int unit) {
		String text = getContext().getResources().getString(R.string.video_popup_forward_unit_sec);
		text = text.replace("%d", "" + unit);
		unitText.setText(text);
	}
	
	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar arg0) {
	}

	@Override
	public void onClick(View view) {
		if (view == screenshotText) {
			if (videoService != null) {
				dismiss();
				videoService.onScreenshot();
			}
		}
		else {//screen size radio button
			float flag = checkScreenSizeRadio(view);
			if (videoService != null) {
				videoService.onUpdateVideoSize(flag);
			}
		}
	}

	private float checkScreenSizeRadio(View view) {
		float flag = MyVideoView.FLAG_MATCHSCREEN;
		for (int i = 0; i < screenSizeGroup.length; i ++) {
			if (view == screenSizeGroup[i]) {
				flag = screenSizeValues[i];
			}
			else {
				screenSizeGroup[i].setChecked(false);
			}
		}
		return flag;
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

}
