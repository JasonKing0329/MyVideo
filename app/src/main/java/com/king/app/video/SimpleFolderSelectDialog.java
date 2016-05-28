package com.king.app.video;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.king.app.video.controller.OnVideoDialogListener;
import com.king.app.video.setting.Configuration;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout.LayoutParams;

public class SimpleFolderSelectDialog extends VideoDialog
		implements OnItemClickListener, OnItemLongClickListener, OnClickListener{

	private List<File> fileList;
	private ListView listView;
	private TextView parentView;
	private SimpleListAdapter adapter;

	private File currentFolder;
	private File selectFolder;
	private View lastSelectView;
	
	private TextView addView, saveView, closeView;
	private TextView titleView;
	
	private OnVideoDialogListener actionListener;
	
	public SimpleFolderSelectDialog(Context context,
			OnVideoDialogListener actionListener) {
		super(context);
		this.actionListener = actionListener;
		titleView.setText(R.string.video_select_path);
		requestOkAction(false);
		
		currentFolder = new File(Configuration.SDCARD);
		changeFileList();
		
		adapter = new SimpleListAdapter();
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
		listView.setOnItemLongClickListener(this);

		applyGreyStyle();
		computeHeight();
	}

	@Override
	protected View getCustomView() {

		View view = LayoutInflater.from(getContext()).inflate(R.layout.video_folder_select, null);
		listView = (ListView) view.findViewById(R.id.video_folderselect_list);
		parentView = (TextView) view.findViewById(R.id.video_folderselect_parent);
		closeView = (TextView) view.findViewById(R.id.video_action_close);
		addView = (TextView) view.findViewById(R.id.video_action_add);
		saveView = (TextView) view.findViewById(R.id.video_action_save);
		titleView = (TextView) view.findViewById(R.id.video_folderdlg_title);
		parentView.setOnClickListener(this);
		closeView.setOnClickListener(this);
		addView.setOnClickListener(this);
		saveView.setOnClickListener(this);
		if (Application.isLollipop()) {
			closeView.setBackgroundResource(R.drawable.item_background_borderless_material);
			addView.setBackgroundResource(R.drawable.item_background_borderless_material);
			saveView.setBackgroundResource(R.drawable.item_background_borderless_material);
		}
		return view;
	}
	
	FileFilter folderFilter = new FileFilter() {
		
		@Override
		public boolean accept(File file) {
			return file.isDirectory();
		}
	};

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View view, int position,
			long arg3) {
		selectFolder(position, view);
		return true;
	}

	private void selectFolder(int position, View view) {

		SimpleListAdapter.ContainerHolder holder = (SimpleListAdapter.ContainerHolder) view.getTag();
		if (holder.bkLayout == lastSelectView) {
			holder.bkLayout.setBackground(null);
			selectFolder = null;
			lastSelectView = null;
			requestOkAction(false);
		}
		else {
			if (lastSelectView != null) {
				lastSelectView.setBackground(null);
			}
			holder.bkLayout.setBackgroundColor(getContext().getResources().getColor(R.color.video_list_item_selected));
			selectFolder = fileList.get(position);
			lastSelectView = holder.bkLayout;
			requestOkAction(true);
		}
		
	}

	private void requestOkAction(boolean show) {
		saveView.setVisibility(show ? View.VISIBLE:View.GONE);
	}

	private void refreshFileList() {
		requestOkAction(false);
		adapter.notifyDataSetChanged();
	}
	
	private void changeFileList() {
		if (!isRootFolder(currentFolder.getPath())) {
			parentView.setVisibility(View.VISIBLE);
		}
		selectFolder = null;
		if (lastSelectView != null) {
			lastSelectView.setBackground(null);
		}
		lastSelectView = null;
		File[] files = currentFolder.listFiles(folderFilter);
		if (files.length > 0) {
			fileList = new ArrayList<File>();
			for (File file:files) {
				fileList.add(file);
			}
			Collections.sort(fileList, new Comparator<File>() {

				@Override
				public int compare(File file0, File file1) {

					return file0.getName().toLowerCase(Locale.CHINA)
							.compareTo(file1.getName().toLowerCase(Locale.CHINA));
				}
			});
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {

		File file = fileList.get(position);
		if (file.listFiles(folderFilter).length == 0) {
			selectFolder(position, view);
			return;
		}
		
		currentFolder = fileList.get(position);
		changeFileList();
		if (fileList.size() > 0) {
			refreshFileList();
		}
	}
	
	@Override
	public void onClick(View view) {

		if (view == parentView) {
			currentFolder = currentFolder.getParentFile();
			if (isRootFolder(currentFolder.getPath())) {
				parentView.setVisibility(View.GONE);
			}
			changeFileList();
			refreshFileList();
		}
		else if (view == saveView) {
			actionListener.onOk(selectFolder.getPath());
			dismiss();
		}
		else if (view == addView) {
			new SimpleDialogManager().openTextInputDialog(getContext()
					, getContext().getResources().getString(R.string.video_folderselect_new)
					, ""
					, new SimpleDialogManager.OnDialogActionListener() {
						
						@Override
						public void onOk(String name) {
							String path = null;
							if (selectFolder == null) {
								path = currentFolder.getPath() + "/" + name;
							}
							else {
								path = selectFolder.getPath() + "/" + name;
							}
							
							File file = new File(path);
							
							if (file.exists()) {
								Toast.makeText(getContext(), R.string.video_folderselect_folder_exist, Toast.LENGTH_LONG).show();
							}
							else {
								file.mkdir();
								changeFileList();
								refreshFileList();
								Toast.makeText(getContext(), R.string.video_save_success, Toast.LENGTH_LONG).show();
							}
						}

						@Override
						public void onDismiss() {
							
						}
					});
		}
		else if (view == closeView) {
			dismiss();
		}
	}

	private boolean isRootFolder(String path) {
		if (path.equals(Configuration.SDCARD)) {
			return true;
		}
		return false;
	}
	
	private class SimpleListAdapter extends BaseAdapter {

		private int iconSize;
		private int expandIconSize;
		private int textLeftSpace;
		private Drawable icon, hasExpandIcon;
		
		public SimpleListAdapter() {

			icon = getContext().getResources().getDrawable(R.drawable.video_directory_icon);
			hasExpandIcon = getContext().getResources().getDrawable(R.drawable.video_arrow_expand_right);
			iconSize = getContext().getResources().getDimensionPixelSize(R.dimen.video_folderdlg_icon_size);
			expandIconSize = iconSize / 2;
			textLeftSpace = getContext().getResources().getDimensionPixelSize(R.dimen.video_folderdlg_text_left_space);
		}
		@Override
		public int getCount() {

			return fileList == null ? 0 : fileList.size();
		}

		@Override
		public Object getItem(int arg0) {
			
			return fileList == null ? 0 : fileList.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup group) {
			ContainerHolder containerHolder = null;
			if (convertView == null) {
				containerHolder = new ContainerHolder();

				LinearLayout container = new LinearLayout(getContext());
				LayoutParams params0 = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
				container.setLayoutParams(params0);
				container.setOrientation(LinearLayout.HORIZONTAL);
				container.setGravity(Gravity.CENTER_VERTICAL);
				container.setPadding(20, 20, 20, 20);

				containerHolder.arrowIconView = new ImageView(getContext());
				LayoutParams params = new LayoutParams(expandIconSize, expandIconSize);
				//int leftMargin = levelSpace;
				int leftMargin = 0;
				((MarginLayoutParams) params).leftMargin = leftMargin;
				containerHolder.arrowIconView.setImageDrawable(hasExpandIcon);
				containerHolder.arrowIconView.setLayoutParams(params);
				containerHolder.arrowIconView.setScaleType(ScaleType.FIT_CENTER);
				container.addView(containerHolder.arrowIconView);
				containerHolder.arrowIconView.setVisibility(View.INVISIBLE);
				
				containerHolder.folderIconView = new ImageView(getContext());
				params = new LayoutParams(iconSize, iconSize);
				containerHolder.folderIconView.setImageDrawable(icon);
				containerHolder.folderIconView.setLayoutParams(params);
				container.addView(containerHolder.folderIconView);
				
				containerHolder.nameView = new TextView(getContext());
				params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
				leftMargin = textLeftSpace;
				((MarginLayoutParams) params).leftMargin = leftMargin;
				containerHolder.nameView.setLayoutParams(params);
				containerHolder.nameView.setTextColor(getContext().getResources().getColor(R.color.black));
				containerHolder.nameView.setGravity(Gravity.CENTER_VERTICAL);
				container.addView(containerHolder.nameView);
				
				containerHolder.bkLayout = container;
				convertView = container;
				convertView.setTag(containerHolder);
			}
			else {
				containerHolder = (ContainerHolder) convertView.getTag();
			}
			
			File file = fileList.get(position);
			containerHolder.nameView.setText(file.getName());
			if (file.listFiles(folderFilter).length > 0) {
				containerHolder.arrowIconView.setVisibility(View.VISIBLE);
			}
			else {
				containerHolder.arrowIconView.setVisibility(View.INVISIBLE);
			}
			return convertView;
		}

		public class ContainerHolder {
			LinearLayout bkLayout;
			ImageView arrowIconView;
			ImageView folderIconView;
			TextView nameView;
		}
	}

	public void notifyOrientaionChanged() {
		computeHeight();
	}

}
