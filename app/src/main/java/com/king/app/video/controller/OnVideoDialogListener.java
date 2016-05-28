package com.king.app.video.controller;

public interface OnVideoDialogListener {

	public boolean onCancel();
	public void onOk(Object object);
	public void onLoadData(Object object);
}
