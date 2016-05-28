package com.king.app.video.controller;

import java.util.List;

import com.king.app.video.model.VideoData;
import com.king.app.video.model.VideoOrder;

public class ObjectCache {

	private static Object object;
	private static Object object1;
	
	public static void putVideoList(List<VideoData> list) {
		object = list;
	}
	
	@SuppressWarnings("unchecked")
	public static List<VideoData> getVideoList() throws ClassCastException {
		if (object == null) {
			return null;
		}
		return (List<VideoData>) object;
	}

	public static void putVideoOrder(VideoOrder order) {
		object1 = order;
	}

	public static VideoOrder getVideoOrder() throws ClassCastException {
		if (object1 == null) {
			return null;
		}
		return (VideoOrder) object1;
	}
}
