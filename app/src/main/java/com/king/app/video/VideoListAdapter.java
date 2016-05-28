package com.king.app.video;

import java.util.List;

import com.king.app.video.model.VideoData;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

public class VideoListAdapter extends AbstractVideoListAdapter {

	public VideoListAdapter(Context context, List<VideoData> list) {
		super(context, list);
	}

	@Override
	protected View getConvertView() {
		return LayoutInflater.from(mContext).inflate(R.layout.video_list_item, null);
	}

	@Override
	protected int getAdapterMode() {
		return ADAPTER_MODE_LIST;
	}
}
