package com.king.app.video;

import android.os.Build;

public class Application {

	public static int getSDKVersion() {
		return Build.VERSION.SDK_INT;
	}
	
	/**
	 * use number 21 to mark, make codes runs well under android L
	 * @return
	 */
	public static boolean isLollipop() {
		return Build.VERSION.SDK_INT >= 21;//Build.VERSION_CODES.L;
	}
	
	public static boolean DEBUG = false;
}
