package com.king.app.video;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.king.app.video.customview.AbstractScrollTabAdapter;
import com.king.app.video.model.VideoOrder;

public class ScrollTabAdapter extends AbstractScrollTabAdapter {

	private List<VideoOrder> orderList;
	private Context mContext;
	private int selectIndex;
	
	public ScrollTabAdapter(Context context, List<VideoOrder> list) {
		mContext = context;
		orderList = list;
		selectIndex = 0;
	}
	
	@Override
	public int getCount() {
		return orderList == null ? 0 : orderList.size();
	}

	@Override
	public View getView(int position, View convertView) {
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.video_scrolltab_item, null);
			holder = new ViewHolder();
			holder.textView = (TextView) convertView.findViewById(R.id.video_scrolltab_name);
			holder.line = convertView.findViewById(R.id.video_scrolltab_line);
			convertView.setTag(holder);
		}
		else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		if (position == selectIndex) {
			holder.textView.setTextAppearance(mContext, R.style.ScrollTabFocus);
			holder.line.setVisibility(View.VISIBLE);
		}
		else {
			holder.textView.setTextAppearance(mContext, R.style.ScrollTabNormal);
			holder.line.setVisibility(View.GONE);
		}
		holder.textView.setText(orderList.get(position).getName() + "(" + orderList.get(position).getTotal() + ")");
		return convertView;
	}

	public void onSelect(int position) {
		selectIndex = position;
	}

	private class ViewHolder {
		TextView textView;
		View line;
	}
}
