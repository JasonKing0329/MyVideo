package com.king.app.video.controller;

import android.util.SparseBooleanArray;
import android.view.View;

public interface SelectService {

	public boolean isSelectMode();
	public void setSelectMode(boolean select);
	public SparseBooleanArray getCheckedMap();
	public void checkItem(int position, View view);
	public int getCheckedPosition();
	public int getCheckedItemCount();
	public void selectAll();
	public void unSelectAll();
	public boolean gonnaCheckAll(int position, View view);
	public boolean gonnaUnCheckAll(int position, View view);
}
