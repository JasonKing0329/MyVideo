package com.king.lib.image.crop;

import android.content.Context;
import android.graphics.Point;
import android.view.WindowManager;

public class DisplayHelper {

	public static boolean isTabModel(Context context) {
		if (context.getResources().getConfiguration().smallestScreenWidthDp >= 600) {
			return true;
		}
		return false;
	}
	
	public static float getDpiDensityNum(Context context) {
		float result = 1.5f;
		switch (context.getResources().getConfiguration().densityDpi) {
		case 120:
			result = 0.75f;
			break;
		case 160:
			result = 1.0f;
			break;
		case 240:
			result = 1.5f;
			break;
		case 320:
			result = 2.0f;
			break;
		case 480:
			result = 3.0f;
			break;
		case 640:
			result = 4.0f;
			break;

		default:
			break;
		}
		return result;
	}
	
	public static Point getScreenSize(Context context) {
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Point point = new Point();
		wm.getDefaultDisplay().getSize(point);
		return point;
	}
	
}
