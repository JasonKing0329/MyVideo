package com.king.app.video;

import java.util.List;

import com.king.app.video.model.VideoData;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class PlayListAdapter extends BaseAdapter implements OnClickListener {

	public interface DeleteCallback {
		public void onDeleteItem(int position);
	}
	
	private boolean enableDrag;
	private List<VideoData> list;
	private Context mContext;
	
	private DeleteCallback mCallback;
	
	private int mPlayPosition;
	private int mFocusColor, mNormalColor;
	
	public PlayListAdapter(Context context, List<VideoData> list, DeleteCallback callback) {
		this.list = list;
		mContext = context;
		mCallback = callback;
		
		mFocusColor = context.getResources().getColor(R.color.playlist_name_text_focus);
		mNormalColor = context.getResources().getColor(R.color.playlist_name_text);
	}
	
	public void setPlayPosition(int position) {
		mPlayPosition = position;
	}
	
	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.adapter_playlist_item, null);
			holder = new ViewHolder();
			holder.name = (TextView) convertView.findViewById(R.id.playlist_item_name);
			holder.time = (TextView) convertView.findViewById(R.id.playlist_item_time);
			holder.deleteIcon = (ImageView) convertView.findViewById(R.id.playlist_item_delete);
			holder.drag = (ImageView) convertView.findViewById(R.id.playlist_item_drag);
			convertView.setTag(holder);
		}
		else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		VideoData data = list.get(position);
		holder.name.setText(data.getName());
		if (position == mPlayPosition) {
			holder.name.setTextColor(mFocusColor);
		}
		else {
			holder.name.setTextColor(mNormalColor);
		}
		holder.time.setText(data.getDuration());

		if (enableDrag) {
			holder.drag.setVisibility(View.VISIBLE);
		}
		else {
			holder.drag.setVisibility(View.GONE);
		}
		holder.deleteIcon.setTag(position);
		holder.deleteIcon.setOnClickListener(this);
		return convertView;
	}

	private class ViewHolder {
		TextView name;
		TextView time;
		ImageView deleteIcon;
		ImageView drag;
	}

	public void enableDrag(boolean drag) {
		enableDrag = drag;
	}
	@Override
	public void onClick(View v) {
		if (mCallback != null) {
			int position = (Integer) v.getTag();
			mCallback.onDeleteItem(position);
		}
	}
}
