package com.king.app.video.data.personal;

public class VideoPersonalData {

	private String id;
	private int lastPlayPosition;
	private String lastPlayStr;
	private int score;
	private String path;
	private String flag;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public int getLastPlayPosition() {
		return lastPlayPosition;
	}
	public void setLastPlayPosition(int lastPlayPosition) {
		this.lastPlayPosition = lastPlayPosition;
	}
	public String getLastPlayStr() {
		return lastPlayStr;
	}
	public void setLastPlayStr(String lastPlayStr) {
		this.lastPlayStr = lastPlayStr;
	}
	public int getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getFlag() {
		return flag;
	}
	public void setFlag(String flag) {
		this.flag = flag;
	}
}
