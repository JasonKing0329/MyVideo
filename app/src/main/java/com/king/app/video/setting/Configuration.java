package com.king.app.video.setting;

import java.io.File;

import android.os.Environment;

public class Configuration {

	public static final String SDCARD = Environment.getExternalStorageDirectory().getPath();
	public static final String APP_ROOT = SDCARD + "/myvideo";
	public static final String APP_HISTORY = APP_ROOT + "/history";
	public static final String APP_SAVEAS = APP_ROOT + "/saveas";
	
	public static final String DEFAULT_IMAGE_EXTRA = ".png";
	
	public static void init() {
		File file = new File(APP_ROOT);
		if (!file.exists() && !file.isDirectory()) {
			file.mkdir();
		}
		file = new File(APP_SAVEAS);
		if (!file.exists()) {
			file.mkdir();
		}
		file = new File(APP_HISTORY);
		if (!file.exists()) {
			file.mkdir();
		}
	}
}
