package com.king.app.video.customview;

import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;
import android.view.View;

public abstract class AbstractScrollTabAdapter {
	
	private Handler eventHandler;

	void setCallback(Callback callback) {
		eventHandler = new Handler(callback);
	}
	
	public void notifyDataSetChanged() {
		Message message = new Message();
		message.what = ScrollTab.MSG_DATA_SET_CHANGED;
		eventHandler.sendMessage(message);
	}
	
	public abstract int getCount();
	public abstract View getView(int position, View convertView);
}
