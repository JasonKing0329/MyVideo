package com.king.app.video.data.personal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

import com.king.app.video.setting.Configuration;

import android.content.Context;

public class DatabaseInfor {

	public static String DATABASE_NAME = "myvideo.db";
	public static String DB_PATH;

	public static boolean prepare(Context context) {
		DB_PATH = context.getFilesDir().getPath() + "/" + DATABASE_NAME;
		if (!new File(DB_PATH).exists()) {
			try {
				InputStream in = context.getAssets().open(DATABASE_NAME);
				FileOutputStream out = new FileOutputStream(DB_PATH);
				byte[] buffer = new byte[1024];
				int byteread = 0;
				while ((byteread = in.read(buffer)) != -1) {
					out.write(buffer, 0, byteread);
				}
				in.close();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	public static boolean export(Context context) {
		DB_PATH = context.getFilesDir().getPath() + "/" + DATABASE_NAME;
		if (new File(DB_PATH).exists()) {
			try {
				Calendar calendar = Calendar.getInstance();
				StringBuffer targetPath = new StringBuffer(Configuration.APP_HISTORY);
				targetPath.append("/myvideo_");
				targetPath.append(calendar.get(Calendar.YEAR)).append("_");
				targetPath.append(calendar.get(Calendar.MONTH) + 1).append("_");
				targetPath.append(calendar.get(Calendar.DAY_OF_MONTH)).append("_");
				targetPath.append(calendar.get(Calendar.HOUR)).append("_");
				targetPath.append(calendar.get(Calendar.MINUTE)).append("_");
				targetPath.append(calendar.get(Calendar.SECOND)).append(".db");
				InputStream in = new FileInputStream(DB_PATH);
				FileOutputStream out = new FileOutputStream(targetPath.toString());
				byte[] buffer = new byte[1024];
				int byteread = 0;
				while ((byteread = in.read(buffer)) != -1) {
					out.write(buffer, 0, byteread);
				}
				in.close();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	/**
	 *
	 * @param context
	 */
	public static boolean replaceDatabase(Context context, String target) {

		if (target == null || !new File(target).exists()) {
			return false;
		}

		//先检查是否存在，存在则删除
		DB_PATH = context.getFilesDir().getPath() + "/" + DATABASE_NAME;

		File defaultDb = new File(DB_PATH);
		if (defaultDb.exists()) {
			defaultDb.delete();
		}
		try {
			InputStream in = new FileInputStream(target);
			File file = new File(DB_PATH);
			OutputStream fileOut = new FileOutputStream(file);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = in.read(buffer))>0){
				fileOut.write(buffer, 0, length);
			}

			fileOut.flush();
			fileOut.close();
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
