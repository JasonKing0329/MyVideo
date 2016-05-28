package com.king.app.video;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.king.app.video.SimpleDialogManager.OnDialogActionListener;
import com.king.app.video.controller.DataController;
import com.king.app.video.controller.DisplayHelper;
import com.king.app.video.controller.ImageController;
import com.king.app.video.controller.OnVideoDialogListener;
import com.king.app.video.controller.ScreenUtils;
import com.king.app.video.controller.VideoFormatter;
import com.king.app.video.model.VideoData;
import com.king.app.video.model.VideoOrder;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DetailThumbnailDialog extends VideoDialog implements OnClickListener {

	public interface OnActionListener {
		public void onDelete();
		public void onRename();
		public void onPlay();
	}
	
	private final int NUM_LOAD_THUMB_PUBLISH = 4;
	private final int NUM_THUMBNAIL = 16;
	
	private GridView gridView;
	private TextView titleView, pathView;
	private TextView playAction, closeAction, deleteAction, renameAction, addToAction;
	private TextView saveImageAction;
	private CheckBox showtimeCheck;
	
	private List<Bitmap> bitmapList;
	private List<Integer> timeList;
	private GridAdapter adapter;
	private VideoData videoData;
	
	private OnActionListener actionListener;
	private DataController dataController;

	private int paddingHor;
	public DetailThumbnailDialog(Context context, VideoData data, OnActionListener listener) {
		super(context);
		actionListener = listener;
		videoData = data;
		applyGreyStyle();
		enableDrag();
		titleView.setText(data.getName());
		pathView.setText(videoData.getPath().substring(0, data.getPath().lastIndexOf("/")));

		dataController = new DataController();
		
		bitmapList = new ArrayList<Bitmap>();
		timeList = new ArrayList<Integer>();
		
		createShotTime();
		
		adapter = new GridAdapter();
		gridView.setAdapter(adapter);
		
		calculateSize();
		onConfigurationChanged(getContext().getResources().getConfiguration().orientation);
		new ThumnailTask().execute();
	}

	private void createShotTime() {
		int duration = videoData.getDurationInt();
		int step = duration / NUM_THUMBNAIL;

		for (int i = 0; i < NUM_THUMBNAIL; i ++) {
			timeList.add(i * step);
			bitmapList.add(null);
		}

		Random random = new Random();
		for (int i = 0; i < NUM_THUMBNAIL; i ++) {
			int start = timeList.get(i);
			int end = duration;
			if (i < NUM_THUMBNAIL - 1) {
				end = timeList.get(i + 1) - 1000;
			}
			int time = start + Math.abs(random.nextInt()) % (end - start);
			timeList.set(i, time);
		}
	}

	private void calculateSize() {
		paddingHor = getContext().getResources().getDimensionPixelSize(R.dimen.video_detail_thumb_padding_hor);
		int width = adapter.getItemWidth() * 4 + paddingHor * 2;
		setWidth(width);
	}

	@Override
	protected View getCustomView() {
		View view = LayoutInflater.from(getContext()).inflate(R.layout.video_thumnail_detail, null);
		titleView = (TextView) view.findViewById(R.id.video_detail_thumb_title);
		pathView = (TextView) view.findViewById(R.id.video_detail_thumb_path);
		gridView = (GridView) view.findViewById(R.id.video_detail_thumb_grid);
		playAction = (TextView) view.findViewById(R.id.video_action_play);
		closeAction = (TextView) view.findViewById(R.id.video_action_close);
		deleteAction = (TextView) view.findViewById(R.id.video_action_delete);
		renameAction = (TextView) view.findViewById(R.id.video_action_rename);
		addToAction = (TextView) view.findViewById(R.id.video_action_addto);
		saveImageAction = (TextView) view.findViewById(R.id.video_detail_thumb_save);
		showtimeCheck = (CheckBox) view.findViewById(R.id.video_detail_thumb_showtime);
		playAction.setOnClickListener(this);
		closeAction.setOnClickListener(this);
		deleteAction.setOnClickListener(this);
		renameAction.setOnClickListener(this);
		addToAction.setOnClickListener(this);
		showtimeCheck.setOnClickListener(this);
		saveImageAction.setOnClickListener(this);
		
		if (Application.isLollipop()) {
			playAction.setBackgroundResource(R.drawable.item_background_borderless_material);
			closeAction.setBackgroundResource(R.drawable.item_background_borderless_material);
			deleteAction.setBackgroundResource(R.drawable.item_background_borderless_material);
			renameAction.setBackgroundResource(R.drawable.item_background_borderless_material);
			addToAction.setBackgroundResource(R.drawable.item_background_borderless_material);
			saveImageAction.setBackgroundResource(R.drawable.item_background_material);
		}
		return view;
	}

	private class ThumnailTask extends AsyncTask<Void, Boolean, Void> {

		private DataController dataController;
		@Override
		protected Void doInBackground(Void... arg0) {
			
			boolean isOver = false;
			dataController = new DataController();
			for (int i = 0; i < NUM_THUMBNAIL; i ++) {
				int position = timeList.get(i);
				Bitmap bitmap = dataController.getVideoThumbnailInSeries(videoData.getPath(), position);
				bitmapList.set(i, bitmap);
				if (i % NUM_LOAD_THUMB_PUBLISH == NUM_LOAD_THUMB_PUBLISH - 1 && i != bitmapList.size() - 1) {
					publishProgress(false);
				}
			}
			isOver = true;
			publishProgress(isOver);
			return null;
		}

		@Override
		protected void onProgressUpdate(Boolean... values) {
			adapter.notifyDataSetChanged();
			if (values[0]) {//is over
				dataController.endSeries();
			}
			super.onProgressUpdate(values);
		}
	}
	
	private class GridAdapter extends BaseAdapter {

		private int itemWidth, itemHeight;
		
		public GridAdapter() {
			itemWidth = getContext().getResources().getDimensionPixelSize(R.dimen.video_detail_thumb_item_width);
			itemHeight = getContext().getResources().getDimensionPixelSize(R.dimen.video_detail_thumb_item_height);
		}
		
		public int getItemWidth() {
			return itemWidth;
		}
		
		@Override
		public int getCount() {
			return timeList == null ? 0 : timeList.size();
		}

		@Override
		public Object getItem(int position) {
			return timeList == null ? 0 : timeList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup group) {
			LinearLayout layout = new LinearLayout(getContext());
			layout.setOrientation(LinearLayout.VERTICAL);
			
			ImageView imageView = new ImageView(getContext());
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					itemWidth, itemHeight);
			imageView.setLayoutParams(params);
			imageView.setScaleType(ScaleType.FIT_XY);
			if (bitmapList.get(position) == null) {
				imageView.setImageResource(R.drawable.video_thumb_loading);
			}
			else {
				imageView.setImageBitmap(bitmapList.get(position));
			}
			layout.addView(imageView);
			
			if (showtimeCheck.isChecked()) {
				TextView textView = new TextView(getContext());
				textView.setText(VideoFormatter.formatTime(timeList.get(position)));
				textView.setGravity(Gravity.CENTER_HORIZONTAL);
				layout.addView(textView);
			}
			return layout;
		}
		
	}

	@Override
	public void onClick(View view) {
		if (view == playAction) {
			dismiss();
			actionListener.onPlay();
		}
		else if (view == closeAction) {
			dismiss();
		}
		else if (view == deleteAction) {
			new SimpleDialogManager().openWarningDialog(getContext()
					, R.string.video_dlg_delete_warning, true
					, new OnDialogActionListener() {
						
						@Override
						public void onOk(String name) {
							/**
							 *  files under /storage/extSdCard couldn't be deleted by File method
							 *  permission denied
							 *  and by this way, data still can be queried from content resolver for a long time
							 */
							/*
							File file = new File(videoData.getPath());
							file.setExecutable(true,false);
					        file.setReadable(true,false);
					        file.setWritable(true,false);
							file.delete();
							*/
							dataController.deleteVideo(getContext(), videoData);
							dismiss();
							actionListener.onDelete();
						}

						@Override
						public void onDismiss() {
							
						}
					});
		}
		else if (view == renameAction) {
			new SimpleDialogManager().openTextInputDialog(getContext()
					, getContext().getResources().getString(R.string.video_action_rename)
					, videoData.getName().substring(0, videoData.getName().lastIndexOf("."))
					, new OnDialogActionListener() {
						
						@Override
						public void onOk(String name) {
							//File file = new File(videoData.getPath());
							String extra = videoData.getName().substring(
									videoData.getName().lastIndexOf("."));
							String newName = name + extra;
							/**
							 *  files under /storage/extSdCard couldn't be deleted by File method
							 * exception: rename failed: exdev (cross-devicce link)
							 *  and by this way, data still not change in content resolver for a long time
							 */
							//file.renameTo(new File(newName));
							
							videoData.setName(newName);
							String originPath = videoData.getPath();
							String newPath = new File(videoData.getPath()).getParent() + "/" + newName;
							videoData.setPath(newPath);
							dataController.renameVideo(getContext(), videoData, originPath);
							
							titleView.setText(newName);
							actionListener.onRename();
						}

						@Override
						public void onDismiss() {
							
						}
					});
		}
		else if (view == addToAction) {

			OrderManagerDialog dialog = new OrderManagerDialog(getContext());
			//it shouldn't add contents to default order(default order query all videos)
			dialog.hideDefaultOrder();
			dialog.showAsOrderChooser(
					new OrderManagerDialog.OnOrderSelectListener() {
						
						@Override
						public void onSelect(VideoOrder order) {
							if (!dataController.addVideoToOrder(videoData, order)) {
								Toast.makeText(getContext(), R.string.video_warning_addtoorder_repeat, Toast.LENGTH_LONG).show();
							}
						}
					});
		}
		else if (view == showtimeCheck) {
			adapter.notifyDataSetChanged();
		}
		else if (view == saveImageAction) {
			String name = videoData.getName().substring(0, videoData.getName().lastIndexOf("."));
			new SaveAsDialog(getContext(), name
					, new OnVideoDialogListener() {
						
						@Override
						public void onOk(Object object) {
							if (object != null) {
								String path = (String) object;
								path = path + com.king.app.video.setting.Configuration.DEFAULT_IMAGE_EXTRA;
								new SaveImageTask(path).execute();
							}
						}
						
						@Override
						public void onLoadData(Object object) {
							
						}
						
						@Override
						public boolean onCancel() {
							return false;
						}
					}).show();
		}
	}
	
	private class SaveImageTask extends AsyncTask<Void, Void, Void> {

		private String path;
		public SaveImageTask(String path) {
			this.path = path;
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			ImageController.saveBitmap(ScreenUtils.snapShotView(gridView), path);
			publishProgress();
			return null;
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			Toast.makeText(getContext(), R.string.video_detail_thumb_save_success, Toast.LENGTH_LONG).show();
			super.onProgressUpdate(values);
		}
		
	}
	
	public void onConfigurationChanged(int orientation) {
		if (!DisplayHelper.isTabModel(getContext()) && orientation == Configuration.ORIENTATION_LANDSCAPE) {
			gridView.getLayoutParams().height = getContext().getResources()
					.getDimensionPixelSize(R.dimen.video_detail_thumb_phone_grid_height_land);
		}
	}

	@Override
	public void dismiss() {
		super.dismiss();
		if (bitmapList != null) {
			for (Bitmap bitmap:bitmapList) {
				if (bitmap != null) {
					bitmap.recycle();
				}
			}
		}
	}
}
