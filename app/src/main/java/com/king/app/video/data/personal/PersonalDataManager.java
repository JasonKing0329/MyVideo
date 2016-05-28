package com.king.app.video.data.personal;

import java.util.HashMap;
import java.util.List;

import com.king.app.video.model.VideoData;
import com.king.app.video.model.VideoOrder;

public class PersonalDataManager {

	public PersonalDataManager() {

	}

	public void queryPersonalData(VideoData data) {
		if (data != null) {
			PersonalDataService dataService = PersonalDataServiceFactory.create();
			VideoPersonalData personalData = dataService.queryPersonalData(data.getId());
			if (personalData == null) {
				personalData = new VideoPersonalData();
				personalData.setId(data.getId());
				personalData.setPath(data.getPath());
				dataService.addPersonalData(personalData);
			}
			data.setPersonalData(personalData);
		}
	}

	public void deletePersonalData(VideoData data) {
		if (data != null) {
			PersonalDataService dataService = PersonalDataServiceFactory.create();
			dataService.deletePersonalData(data.getPersonalData());
		}
	}

	public void updatePersonalDatabase(List<VideoData> list) {
		PersonalDataService dataService = PersonalDataServiceFactory.create();
		List<VideoPersonalData> datas = dataService.queryAllPersonalData();
		if (datas != null && datas.size() > 0) {
			HashMap<String, VideoPersonalData> personalDataMap = new HashMap<String, VideoPersonalData>();
			HashMap<String, Boolean> referedMap = new HashMap<String, Boolean>();
			for (VideoPersonalData data:datas) {
				personalDataMap.put(data.getId(), data);
			}

			/**
			 * 数据库中不存在则新建数据
			 */
			for (VideoData data:list) {
				VideoPersonalData personalData = personalDataMap.get(data.getId());
				referedMap.put(data.getId(), true);
				if (personalData == null) {
					personalData = new VideoPersonalData();
					personalData.setId(data.getId());
					personalData.setPath(data.getPath());
					dataService.addPersonalData(personalData);
				}
				data.setPersonalData(personalData);
			}

			/**
			 * 数据库中存在然而系统中已无该项文件，则删除数据记录
			 */
			for (VideoPersonalData data:datas) {
				Boolean refered = referedMap.get(data.getId());
				if (refered == null || !refered) {
					dataService.deletePersonalData(personalDataMap.get(data.getId()));
				}
			}
		}
		else {
			for (VideoData data:list) {
				VideoPersonalData personalData = new VideoPersonalData();
				personalData.setId(data.getId());
				personalData.setPath(data.getPath());
				dataService.addPersonalData(personalData);
				data.setPersonalData(personalData);
			}
		}
	}

	public void updateVideoPersonalData(VideoData videoData) {
		PersonalDataService dataService = PersonalDataServiceFactory.create();
		dataService.updatePersonalData(videoData.getPersonalData());
	}

	public List<VideoOrder> queryVideoOrders() {
		PersonalDataService dataService = PersonalDataServiceFactory.create();
		return dataService.queryAllVideoOrders();
	}

	public boolean addNewOrder(VideoOrder order) {
		PersonalDataService dataService = PersonalDataServiceFactory.create();
		return dataService.addVideoOrder(order);
	}

	public void deleteVideoOrder(VideoOrder order) {
		PersonalDataService dataService = PersonalDataServiceFactory.create();
		dataService.deleteVideoOrder(order);
	}

	public void updateVideoOrder(VideoOrder order) {
		PersonalDataService dataService = PersonalDataServiceFactory.create();
		dataService.updateVideoOrder(order);
	}

	public boolean addVideoToOrder(VideoData videoData, VideoOrder order) {
		PersonalDataService dataService = PersonalDataServiceFactory.create();
		return dataService.addVideoToOrder(videoData, order);
	}

	public List<VideoPersonalData> queryVideosFromOrder(VideoOrder order) {
		PersonalDataService dataService = PersonalDataServiceFactory.create();
		return dataService.queryVideoFromOrder(order);
	}

	public boolean addVideosToOrder(List<VideoData> aList, VideoOrder order) {
		PersonalDataService dataService = PersonalDataServiceFactory.create();
		return dataService.addVideosToOrder(aList, order);
	}

	public void deleteVideosFromOrder(List<VideoData> dList, VideoOrder order) {
		PersonalDataService dataService = PersonalDataServiceFactory.create();
		dataService.deleteVideosFromOrder(dList, order);
	}
}
