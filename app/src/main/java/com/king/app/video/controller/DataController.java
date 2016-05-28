package com.king.app.video.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;
import android.provider.MediaStore;
import android.provider.MediaStore.Video;
import android.util.SparseBooleanArray;

import com.king.app.video.data.personal.PersonalDataManager;
import com.king.app.video.data.personal.VideoPersonalData;
import com.king.app.video.model.VideoData;
import com.king.app.video.model.VideoOrder;

public class DataController {

	public enum SortType {
		NONE, NAME, ADDED_DATE, TYPE, DURATION, SIZE, SCORE
	}
	
	private final int THUMB_WIDTH = 400;
	private final int THUMB_HEIGHT = 300;
	private PersonalDataManager dataManager;

	String[] columns = new String[] {
			Video.Media.DATA,
			Video.Media._ID,
			Video.Media.TITLE,
			Video.Media.DISPLAY_NAME,
			Video.Media.SIZE,
			Video.Media.DURATION,
			Video.Media.DATE_ADDED,
			Video.Media.MIME_TYPE,
			Video.Media.WIDTH,
			Video.Media.HEIGHT
	};
	
	public DataController() {
		dataManager = new PersonalDataManager();
	}

	public void deleteVideo(Context context, VideoData videoData) {
		new File(videoData.getPath()).delete();
		context.getContentResolver().delete(Video.Media.EXTERNAL_CONTENT_URI
				, Video.Media._ID + " = ?", new String[]{videoData.getId()});
		dataManager.deletePersonalData(videoData);
	}

	public void renameVideo(Context context, VideoData videoData, String originPath) {
		new File(originPath).renameTo(new File(videoData.getPath()));
		ContentValues values = new ContentValues();
		values.put(Video.Media.DATA, videoData.getPath());
		values.put(Video.Media.DISPLAY_NAME, videoData.getName());
		context.getContentResolver().update(Video.Media.EXTERNAL_CONTENT_URI
				, values, Video.Media._ID + " = ?", new String[]{videoData.getId()});
		updateVideoPersonalData(videoData);
	}

	public VideoData queryVideoDataByPath(Context context, String path) {
		VideoData data = null;
		Cursor cursor = context.getContentResolver().query(
				Video.Media.EXTERNAL_CONTENT_URI,
				columns, Video.Media.DATA + " = ?", new String[]{path}, null);
		if (cursor.moveToNext()) {
			data = getVideoDataFromCursor(cursor);
		}
		cursor.close();
		dataManager.queryPersonalData(data);
		return data;
	}
	
	public VideoData queryVideoData(Context context, String id) {
		VideoData data = null;
		Cursor cursor = context.getContentResolver().query(
				Video.Media.EXTERNAL_CONTENT_URI,
				columns, Video.Media._ID + " = ?", new String[]{id}, null);
		if (cursor.moveToNext()) {
			data = getVideoDataFromCursor(cursor);
		}
		cursor.close();
		
		dataManager.queryPersonalData(data);
		return data;
	}
	
	private VideoData getVideoDataFromCursor(Cursor cursor) {
		VideoData data = new VideoData();
		data.setId(cursor.getString(cursor.getColumnIndexOrThrow(Video.Media._ID)));
		data.setName(cursor.getString(cursor.getColumnIndexOrThrow(Video.Media.DISPLAY_NAME)));
		data.setPath(cursor.getString(cursor.getColumnIndexOrThrow(Video.Media.DATA)));
		data.setSize(cursor.getString(cursor.getColumnIndexOrThrow(Video.Media.SIZE)));
		data.setDuration(cursor.getString(cursor.getColumnIndexOrThrow(Video.Media.DURATION)));
		try {
			data.setDateAdded(Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow(Video.Media.DATE_ADDED))));
		} catch (Exception e) {
			data.setDateAdded(0);
		}
		try {
			data.setWidth(Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(Video.Media.WIDTH))));
		} catch (Exception e) {
			data.setWidth(0);
		}
		try {
			data.setHeight(Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(Video.Media.HEIGHT))));
		} catch (Exception e) {
			data.setHeight(0);
		}
		data.setMimeType(cursor.getString(cursor.getColumnIndexOrThrow(Video.Media.MIME_TYPE)));
		return data;
	}

	public List<VideoData> queryVideos(Context context) {
		
		List<VideoData> list = new ArrayList<VideoData>();
		Cursor cursor = context.getContentResolver().query(
				Video.Media.EXTERNAL_CONTENT_URI,
				columns, null, null, null);
		
		for (int i = 0; i < cursor.getCount(); i ++) {
			if (cursor.moveToNext()) {
				VideoData data = getVideoDataFromCursor(cursor);
				list.add(data);
			}
		}
		cursor.close();
		
		return list;
	}

	public List<VideoData> queryVideos(Context context, VideoOrder order) {

		List<VideoData> list = null;
		List<VideoPersonalData> pList = dataManager.queryVideosFromOrder(order);
		if (pList != null && pList.size() > 0) {
			list = new ArrayList<VideoData>();
			VideoData data = null;
			ContentResolver resolver = context.getContentResolver();
			for (int i = 0; i < pList.size(); i ++) {
				Cursor cursor = resolver.query(
						Video.Media.EXTERNAL_CONTENT_URI,
						columns, Video.Media._ID + " = ?", new String[]{pList.get(i).getId()}, null);
				if (cursor.moveToNext()) {
					data = getVideoDataFromCursor(cursor);
					data.setPersonalData(pList.get(i));
					list.add(data);
				}
				cursor.close();
			}
		}
		return list;
	}

	public void sortList(List<VideoData> list, final SortType sortType) {
		if (list == null) {
			return;
		}
		Collections.sort(list, new Comparator<VideoData>() {

			@Override
			public int compare(VideoData data0, VideoData data1) {

				int result = 0;
				if (sortType == SortType.ADDED_DATE) {
					result = (int) (data1.getDateAdded() - data0.getDateAdded());
				}
				else if (sortType == SortType.DURATION) {
					result = data0.getDurationInt() - data1.getDurationInt();
				}
				else if (sortType == SortType.NAME) {
					result = data0.getName().compareTo(data1.getName());
				}
				else if (sortType == SortType.SIZE) {
					result = (int) (data1.getSizeLong() - data0.getSizeLong());
				}
				else if (sortType == SortType.TYPE) {
					result = data0.getMimeType().compareTo(data1.getMimeType());
				}
				else if (sortType == SortType.SCORE) {
					int score0 = 0;
					int score1 = 0;
					if (data0.getPersonalData() != null) {
						score0 = data0.getPersonalData().getScore();
					}
					if (data0.getPersonalData() != null) {
						score1 = data1.getPersonalData().getScore();
					}
					result = score1 - score0;
				}
				return result;
			}
		});
	}
	
	public Bitmap getVideoThumbnail(String filePath) {  
        Bitmap bitmap = null;  
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();  
        try {  
            retriever.setDataSource(filePath);  
            bitmap = retriever.getFrameAtTime();  
            bitmap = convertToVideoThumbnail(bitmap);
        }   
        catch(IllegalArgumentException e) {  
            e.printStackTrace();  
        }   
        catch (RuntimeException e) {  
            e.printStackTrace();  
        }   
        finally {  
            try {  
                retriever.release();  
            }   
            catch (RuntimeException e) {  
                e.printStackTrace();  
            }  
        }  
        return bitmap;  
    }

	/**
	 * 
	 * @param filePath
	 * @param position (ms)
	 * @return
	 */
	public Bitmap getVideoThumbnailAt(String filePath, int position) {  
        Bitmap bitmap = null;  
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();  
        try {  
            retriever.setDataSource(filePath);  
            /**
             * public Bitmap getFrameAtTime(long timeUs, int option) 
             * ��һ�������Ǵ���ʱ�䣬ֻ����us(΢��) ��������ms����ȡ�����ǵ�һ֡��
             * �ڶ��������ȿ����ٷ����ͣ�
             * OPTION_CLOSEST    �ڸ��ʱ�䣬�������һ��֡,���֡��һ���ǹؼ�֡��
             * OPTION_CLOSEST_SYNC   �ڸ��ʱ�䣬�������һ��ͬ�������Դ������ĵ�֡���ؼ�֡����
             * OPTION_NEXT_SYNC �ڸ�ʱ��֮�����һ��ͬ�������Դ������Ĺؼ�֡��
             * OPTION_PREVIOUS_SYNC  ����˼�壬ͬ��
             */
            bitmap = retriever.getFrameAtTime(position * 1000, MediaMetadataRetriever.OPTION_CLOSEST);  
            bitmap = convertToVideoThumbnail(bitmap);
        }   
        catch(IllegalArgumentException e) {  
            e.printStackTrace();  
        }   
        catch (RuntimeException e) {  
            e.printStackTrace();  
        }   
        finally {  
            try {  
                retriever.release();  
            }   
            catch (RuntimeException e) {  
                e.printStackTrace();  
            }  
        }  
        return bitmap;  
    }

	/**
	 * get high quality bitmap
	 * @param filePath
	 * @param position (ms)
	 * @return
	 */
	public Bitmap getVideoFrameAt(String filePath, int position) {  
        Bitmap bitmap = null;  
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();  
        try {  
            retriever.setDataSource(filePath);  
            /**
             * public Bitmap getFrameAtTime(long timeUs, int option) 
             * ��һ�������Ǵ���ʱ�䣬ֻ����us(΢��) ��������ms����ȡ�����ǵ�һ֡��
             * �ڶ��������ȿ����ٷ����ͣ�
             * OPTION_CLOSEST    �ڸ��ʱ�䣬�������һ��֡,���֡��һ���ǹؼ�֡��
             * OPTION_CLOSEST_SYNC   �ڸ��ʱ�䣬�������һ��ͬ�������Դ������ĵ�֡���ؼ�֡����
             * OPTION_NEXT_SYNC �ڸ�ʱ��֮�����һ��ͬ�������Դ������Ĺؼ�֡��
             * OPTION_PREVIOUS_SYNC  ����˼�壬ͬ��
             */
            bitmap = retriever.getFrameAtTime(position * 1000, MediaMetadataRetriever.OPTION_CLOSEST);
        }   
        catch(IllegalArgumentException e) {  
            e.printStackTrace();  
        }   
        catch (RuntimeException e) {  
            e.printStackTrace();  
        }   
        finally {  
            try {  
                retriever.release();  
            }   
            catch (RuntimeException e) {  
                e.printStackTrace();  
            }  
        }  
        return bitmap;  
    }

    private MediaMetadataRetriever retriever;
    public void endSeries() {
        try {  
        	if (retriever != null) {
                retriever.release();  
                retriever = null;
			}
        }   
        catch (RuntimeException e) {  
            e.printStackTrace();  
        }  
    }
	/**
	 * 
	 * @param filePath
	 * @param position (ms)
	 * @return
	 */
	public Bitmap getVideoThumbnailInSeries(String filePath, int position) {  
        Bitmap bitmap = null;  
        if (retriever == null) {
        	retriever = new MediaMetadataRetriever();
            retriever.setDataSource(filePath);  
		}
        try {  
            /**
             * public Bitmap getFrameAtTime(long timeUs, int option) 
             * ��һ�������Ǵ���ʱ�䣬ֻ����us(΢��) ��������ms����ȡ�����ǵ�һ֡��
             * �ڶ��������ȿ����ٷ����ͣ�
             * OPTION_CLOSEST    �ڸ��ʱ�䣬�������һ��֡,���֡��һ���ǹؼ�֡��
             * OPTION_CLOSEST_SYNC   �ڸ��ʱ�䣬�������һ��ͬ�������Դ������ĵ�֡���ؼ�֡����
             * OPTION_NEXT_SYNC �ڸ�ʱ��֮�����һ��ͬ�������Դ������Ĺؼ�֡��
             * OPTION_PREVIOUS_SYNC  ����˼�壬ͬ��
             */
            bitmap = retriever.getFrameAtTime(position * 1000, MediaMetadataRetriever.OPTION_CLOSEST);  
            bitmap = convertToVideoThumbnail(bitmap);
        }   
        catch(IllegalArgumentException e) {  
            e.printStackTrace();  
        }   
        catch (RuntimeException e) {  
            e.printStackTrace();  
        }
        return bitmap;  
    }
	/**
	 * create thumb nail mode image, to save the memory
	 * 
	 * @param src
	 * @return
	 */
	public Bitmap convertToVideoThumbnail(Bitmap src) {
		Bitmap bitmap = null;

		Matrix matrix = new Matrix();
		float dx = (float) THUMB_WIDTH / (float) src.getWidth();
		float dy = (float) THUMB_HEIGHT / (float) src.getHeight();
		matrix.postScale(dx, dy);
		try {
			bitmap = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
		} catch (Exception e) {
			bitmap = null;
		}
		src.recycle();
		return bitmap;
	}

	public void updatePersonalDatabase(List<VideoData> videoList) {
		dataManager.updatePersonalDatabase(videoList);
	}

	public void updateVideoPersonalData(VideoData videoData) {
		dataManager.updateVideoPersonalData(videoData);
	}

	public List<VideoOrder> queryVideoOrders() {
		
		return dataManager.queryVideoOrders();
	}

	public boolean addNewOrder(VideoOrder order) {
		return dataManager.addNewOrder(order);
	}

	public void deleteVideoOrder(SparseBooleanArray checkedMap, List<VideoOrder> list) {
		List<Integer> delIds = new ArrayList<Integer>();
		for (int i = 0; i < list.size(); i ++) {
			if (checkedMap.get(i)) {
				delIds.add(0, i);
				dataManager.deleteVideoOrder(list.get(i));
			}
		}
		for (int i = 0; i < delIds.size(); i ++) {
			int index = delIds.get(i);
			list.remove(index);
		}
	}

	public void updateOrder(VideoOrder order) {
		dataManager.updateVideoOrder(order);
	}

	public boolean addVideoToOrder(VideoData videoData, VideoOrder order) {
		return dataManager.addVideoToOrder(videoData, order);
	}

	public boolean  addVideoListToOrder(SparseBooleanArray checkedMap
			, List<VideoData> list, VideoOrder order) {
		if (list == null) {
			return false;
		}
		List<VideoData> aList = new ArrayList<VideoData>();
		for (int i = 0; i < list.size(); i ++) {
			if (checkedMap.get(i)) {
				aList.add(list.get(i));
			}
		}
		return dataManager.addVideosToOrder(aList, order);
	}

	public void deleteVideoFromOrder(SparseBooleanArray checkedMap
			, List<VideoData> list, VideoOrder order) {

		List<VideoData> dList = new ArrayList<VideoData>();
		for (int i = list.size() - 1; i >= 0; i --) {
			if (checkedMap.get(i)) {
				dList.add(list.get(i));
				list.remove(i);
			}
		}
		dataManager.deleteVideosFromOrder(dList, order);
	}
}
