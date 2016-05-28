package com.king.app.video.controller;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.util.Log;

public class ImageController {

	public static boolean saveBitmap(Bitmap bitmap, String path) {
		Log.d("ImageController", "saveBitmap:" + path);
		try {
			FileOutputStream out = new FileOutputStream(path);
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
			out.flush();
			out.close();
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
}
