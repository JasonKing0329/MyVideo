package com.king.app.video;

import java.util.List;

import com.king.app.video.controller.SelectService;
import com.king.app.video.customview.StarScoreView;
import com.king.app.video.customview.StarScoreView.OnStarCheckListener;
import com.king.app.video.data.personal.VideoPersonalData;
import com.king.app.video.model.VideoData;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public abstract class AbstractVideoListAdapter extends BaseAdapter implements SelectService {

	protected final int ADAPTER_MODE_LIST = 0;
	protected final int ADAPTER_MODE_GRID = 1;
	
	protected Context mContext;
	private List<VideoData> list;
	private OnStarCheckListener onStarClickListener;

	private boolean selectMode;
	private SparseBooleanArray checkMap;
	
	public AbstractVideoListAdapter(Context context, List<VideoData> list) {
		mContext = context;
		this.list = list;
		checkMap = new SparseBooleanArray();
	}

	public void updateDataList(List<VideoData> list) {
		this.list = list;
	}

	public void setOnStarClickListener(OnStarCheckListener listener) {
		onStarClickListener = listener;
	}
	
	@Override
	public int getCount() {

		return list == null ? 0 : list.size();
	}

	@Override
	public Object getItem(int position) {

		return list == null ? 0 : list.get(position);
	}

	@Override
	public long getItemId(int position) {

		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup group) {

		ViewHolder holder = null;
		if (convertView == null) {
			convertView = getConvertView();
			holder = new ViewHolder();
			holder.thumbnail = (ImageView) convertView.findViewById(R.id.video_list_thumb);
			holder.name = (TextView) convertView.findViewById(R.id.video_list_name);
			holder.duration = (TextView) convertView.findViewById(R.id.video_list_duration);
			holder.size = (TextView) convertView.findViewById(R.id.video_list_size);
			holder.check = (CheckBox) convertView.findViewById(R.id.video_list_check);
			
			if (getAdapterMode() == ADAPTER_MODE_LIST) {
				holder.path = (TextView) convertView.findViewById(R.id.video_list_path);
			}
			holder.starScoreView = (StarScoreView) convertView.findViewById(R.id.video_list_starview);
			convertView.setTag(holder);
		}
		else {
			holder = (ViewHolder) convertView.getTag();
		}
		VideoData data = list.get(position);
		holder.name.setText(data.getName());
		
		VideoPersonalData personalData = data.getPersonalData();
		String duration = "";
		if (personalData != null) {
			if (personalData.getLastPlayStr() != null) {
				duration = duration + personalData.getLastPlayStr() + "/";
			}
		}
		duration = duration + data.getDuration();
		holder.duration.setText(duration);
		
		holder.size.setText(data.getSize());
		if (getAdapterMode() == ADAPTER_MODE_LIST) {
			holder.path.setText(data.getPath().substring(0, data.getPath().lastIndexOf("/")));
		}
		if (data.getThumbnail() == null) {
			holder.thumbnail.setImageResource(R.drawable.video_thumb_loading);
		}
		else {
			holder.thumbnail.setImageBitmap(data.getThumbnail());
		}
		
		if (personalData == null) {
			holder.starScoreView.setScore(0);
		}
		else {
			holder.starScoreView.setScore(personalData.getScore());
		}
		holder.starScoreView.setTag(position);
		holder.starScoreView.setOnStarClickListener(onStarClickListener);

		if (selectMode) {
			holder.check.setVisibility(View.VISIBLE);
			holder.check.setChecked(checkMap.get(position));
		}
		else {
			holder.check.setVisibility(View.GONE);
		}
		return convertView;
	}

	private class ViewHolder {
		CheckBox check;
		ImageView thumbnail;
		TextView name, size, duration, path;
		StarScoreView starScoreView;
	}
	
	protected abstract View getConvertView();
	protected abstract int getAdapterMode();
	

	@Override
	public boolean isSelectMode() {
		return selectMode;
	}

	@Override
	public void setSelectMode(boolean select) {
		selectMode = select;
		if (!select) {
			checkMap.clear();
		}
	}

	@Override
	public SparseBooleanArray getCheckedMap() {
		return checkMap;
	}
	@Override
	public void checkItem(int position, View view) {
		if (checkMap.get(position)) {
			checkMap.put(position, false);
		}
		else {
			checkMap.put(position, true);
		}
		ViewHolder holder = (ViewHolder) view.getTag();
		holder.check.setChecked(checkMap.get(position));
	}

	@Override
	public int getCheckedPosition() {
		for (int i = 0; i < checkMap.size(); i ++) {
			if (checkMap.valueAt(i)) {
				return checkMap.keyAt(i);
			}
		}
		return 0;
	}

	@Override
	public int getCheckedItemCount() {
		int count = 0;
		for (int i = 0; i < checkMap.size(); i ++) {
			if (checkMap.valueAt(i)) {
				count ++;
			}
		}
		return count;
	}
	@Override
	public void selectAll() {
		if (list != null) {
			for (int i = 0; i < list.size(); i ++) {
				checkMap.put(i, true);
			}
		}
	}
	@Override
	public void unSelectAll() {
		if (list != null) {
			for (int i = 0; i < list.size(); i ++) {
				checkMap.put(i, false);
			}
		}
	}
	@Override
	public boolean gonnaCheckAll(int position, View view) {

		ViewHolder holder = (ViewHolder) view.getTag();
		boolean check = holder.check.isChecked();
		if (!check) {
			//if all position already have put value, then must judge this by all checked size
			//if (checkMap.size() == list.size() - 1) {
			if (getCheckedItemCount() == list.size() - 1) {
				return true;
			}
		}
		return false;
	}
	@Override
	public boolean gonnaUnCheckAll(int position, View view) {
		ViewHolder holder = (ViewHolder) view.getTag();
		boolean check = holder.check.isChecked();
		if (check) {
			if (checkMap.size() == list.size()) {//all checked, gonna uncheck this one
				return true;
			}
		}
		return false;
	}
}
