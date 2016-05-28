package com.king.app.video.setting;

import java.util.ArrayList;
import java.util.List;

import com.king.app.video.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SettingProperties {

	private static final String KEY_PATHLIST = "key_saveas_path_list";
	private static final int MAX_PATH_NUMBER = 7;

	private static final String KEY_ORDER_LIST = "key_order_list";
	private static final String KEY_ORDER_ID = "key_order_id";
	private static final String KEY_PLAY_LIST = "key_play_list";
	
	public static String getDisplayMode(Context context) {
		String defValue = context.getResources().getStringArray(R.array.video_setting_display_mode)[0];
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return preferences.getString("video_setting_display_mode", defValue);
	}

	public static String getSortType(Context context) {
		String defValue = context.getResources().getStringArray(R.array.video_setting_sort_type)[0];
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return preferences.getString("video_setting_default_sort", defValue);
	}

	/**
	 * 
	 * @param context
	 * @return unit(s)
	 */
	public static int getForwardUnit(Context context) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return preferences.getInt("video_forward_unit", 3);
	}
	
	public static void saveForwardUnit(Context context, int unit) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt("video_forward_unit", unit);
		editor.commit();
	}

	public static List<String> getLatestPaths(Context context) {

		List<String> list = new ArrayList<String>();
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		String string = preferences.getString(KEY_PATHLIST, null);
		if (string == null) {
			list.add(Configuration.APP_SAVEAS);
		}
		else {
			String[] array = string.split(",");
			for (String path:array) {
				list.add(path);
			}
			if (array.length == 0) {
				list.add(Configuration.APP_SAVEAS);
			}
		}
		return list;
	}

	public static void saveLatestPath(Context context, List<String> pathList) {
		if (pathList.size() > MAX_PATH_NUMBER) {
			for (int i = pathList.size() - 1; i > MAX_PATH_NUMBER - 1 ; i --) {
				pathList.remove(i);
			}
		}
		
		StringBuffer buffer = new StringBuffer();
		buffer.append(pathList.get(0));
		for (int i = 1; i < pathList.size(); i ++) {
			buffer.append(",").append(pathList.get(i));
		}

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(KEY_PATHLIST, buffer.toString());
		editor.commit();
	}

	public static void saveOrderList(Context context, List<String> orderList) {
		
		StringBuffer buffer = new StringBuffer();
		buffer.append(orderList.get(0));
		for (int i = 1; i < orderList.size(); i ++) {
			buffer.append(",").append(orderList.get(i));
		}

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(KEY_ORDER_LIST, buffer.toString());
		editor.commit();
	}

	public static List<String> getOrderList(Context context) {

		List<String> list = new ArrayList<String>();
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		String string = preferences.getString(KEY_ORDER_LIST, null);
		if (string == null) {
			return null;
		}
		else {
			String[] array = string.split(",");
			for (String path:array) {
				list.add(path);
			}
			if (array.length == 0) {
				return null;
			}
		}
		return list;
	}

	public static void savePlayIdList(Context context, List<String> videoIdList) {
		
		StringBuffer buffer = new StringBuffer();
		buffer.append(videoIdList.get(0));
		for (int i = 1; i < videoIdList.size(); i ++) {
			buffer.append(",").append(videoIdList.get(i));
		}

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(KEY_PLAY_LIST, buffer.toString());
		editor.commit();
	}

	public static List<String> getPlayListIds(Context context) {
		List<String> list = new ArrayList<String>();
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		String string = preferences.getString(KEY_PLAY_LIST, null);
		if (string == null) {
			return null;
		}
		else {
			String[] array = string.split(",");
			for (String path:array) {
				list.add(path);
			}
			if (array.length == 0) {
				return null;
			}
		}
		return list;
	}

	public static int getCacheOrderId(Context context) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return preferences.getInt(KEY_ORDER_ID, -1);
	}

	public static void saveCacheOrderId(Context context, int orderId) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt(KEY_ORDER_ID, orderId);
		editor.commit();
	}

}
