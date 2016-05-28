package com.king.app.video.open.image;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;

public class CropHelper {

	public static class MatrixValues {
		public float xScale;
		public float yScale;
		public float left;
		public float top;
	}

	public static Rect computeRectArea(float[] matrixValues, Rect cropArea) {

		Log.d("CropHelper", "cropArea " + cropArea.toString());
		printDValues(matrixValues);

		MatrixValues values = new MatrixValues();
		values.xScale = matrixValues[0];
		values.left = matrixValues[2];
		values.top = matrixValues[5];
		values.yScale = matrixValues[4];

		int width = cropArea.right - cropArea.left;
		int height = cropArea.bottom - cropArea.top;

		float leftToBitmap = cropArea.left - values.left;
		float topToBitmap = cropArea.top - values.top;

		Rect rect = new Rect();
		rect.left = (int) (leftToBitmap / values.xScale);
		rect.top = (int) (topToBitmap / values.yScale);
		rect.right = (int) ((leftToBitmap + width) / values.xScale);
		rect.bottom = (int) ((topToBitmap + height) / values.xScale);
		Log.d("CropHelper", "targetArea " + rect.toString());
		return rect;
	}

	public static Bitmap crop(Bitmap bitmap, Rect targetArea) {

		if (bitmap != null) {
			
			//v5.9.1 fix v5.9 bug, crop area over image, FC occur
			Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
			if (targetArea.left < 0) {
				targetArea.left = 0;
			}
			if (targetArea.top < 0) {
				targetArea.top = 0;
			}
			if (targetArea.right > rect.right) {
				targetArea.right = rect.right;
			}
			if (targetArea.bottom > rect.bottom) {
				targetArea.bottom = rect.bottom;
			}
			int square = (targetArea.right - targetArea.left) * (targetArea.bottom - targetArea.top);
			if (square < 400) {
				return null;
			}
			
			Bitmap target = Bitmap.createBitmap(bitmap, targetArea.left,
					targetArea.top, targetArea.right - targetArea.left,
					targetArea.bottom - targetArea.top);
			return target;
		}
		return null;
	}

	private static void printDValues(float[] values) {
		StringBuffer buffer = new StringBuffer("dValues ");
		buffer.append(values[0]);
		for (int i = 1; i < values.length; i++) {
			buffer.append(",").append(values[i]);
		}

		Log.d("CropView", buffer.toString());
	}

	public static boolean saveBitmap(Bitmap cropBitmap, String path) {
		Log.d("CropView", "saveBitmap:" + path);
		try {
			FileOutputStream out = new FileOutputStream(path);
			cropBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
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
