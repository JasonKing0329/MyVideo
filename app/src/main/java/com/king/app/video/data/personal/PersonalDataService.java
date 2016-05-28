package com.king.app.video.data.personal;

import java.util.List;

import com.king.app.video.model.VideoData;
import com.king.app.video.model.VideoOrder;

public interface PersonalDataService {


	public VideoPersonalData queryPersonalData(String id);
	public List<VideoPersonalData> queryAllPersonalData();
	
	public boolean updatePersonalData(VideoPersonalData data);
	public boolean deletePersonalData(VideoPersonalData data);
	public boolean addPersonalData(VideoPersonalData data);

	public List<VideoOrder> queryAllVideoOrders();
	public boolean updateVideoOrder(VideoOrder order);
	public boolean deleteVideoOrder(VideoOrder order);
	public boolean addVideoOrder(VideoOrder order);
	
	public boolean addVideoToOrder(VideoData videoData, VideoOrder order);
	public boolean addVideosToOrder(List<VideoData> list, VideoOrder order);
	public boolean deleteVideoFromOrder(VideoData videoData, VideoOrder order);
	public List<VideoPersonalData> queryVideoFromOrder(VideoOrder order);
	public void deleteVideosFromOrder(List<VideoData> list, VideoOrder order);
}
