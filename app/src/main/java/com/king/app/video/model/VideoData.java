package com.king.app.video.model;

import com.king.app.video.data.personal.VideoPersonalData;

import android.graphics.Bitmap;
import android.net.Uri;

public class VideoData {

	private String id;
	private String name;
	private String path;
	private Bitmap thumbnail;
	private String size;
	private String duration;
	private String mimeType;
	private int height;
	private int width;
	private long dateAdded;
	private int durationInt;
	private long sizeLong;
	private VideoPersonalData personalData;
	
	public Uri getUri() {
		return Uri.parse("file://" + path);
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public Bitmap getThumbnail() {
		return thumbnail;
	}
	public void setThumbnail(Bitmap thumbnail) {
		this.thumbnail = thumbnail;
	}
	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}
	public String getDuration() {
		return duration;
	}
	public void setDuration(String duration) {
		this.duration = duration;
	}
	public int getDurationInt() {
		return durationInt;
	}
	public void setDurationInt(int durationInt) {
		this.durationInt = durationInt;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public long getSizeLong() {
		return sizeLong;
	}
	public void setSizeLong(long sizeLong) {
		this.sizeLong = sizeLong;
	}
	public long getDateAdded() {
		return dateAdded;
	}
	public void setDateAdded(long dateAdded) {
		this.dateAdded = dateAdded;
	}
	public String getMimeType() {
		return mimeType;
	}
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	public VideoPersonalData getPersonalData() {
		return personalData;
	}
	public void setPersonalData(VideoPersonalData personalData) {
		this.personalData = personalData;
	}
}
