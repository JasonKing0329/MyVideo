package com.king.app.video;

import java.util.HashMap;
import java.util.List;

import com.king.app.video.controller.OnVideoDialogListener;
import com.king.app.video.setting.SettingProperties;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
public class SaveAsDialog extends VideoDialog implements OnItemSelectedListener
		, OnClickListener{

	private Spinner pathSpinner;
	private ImageView browseButton;
	private EditText nameEdit;
	private TextView okButton, cancelButton;

	private List<String> pathList;
	private OnVideoDialogListener actionListener;

	//private ArrayAdapter<String> pathAdapter;
	private PathListAdapter pathAdapter;

	private SimpleFolderSelectDialog folderSelectDialog;

	public SaveAsDialog(Context context, String preName,
						OnVideoDialogListener actionListener) {
		super(context);
		this.actionListener = actionListener;
		applyGreyStyle();
		enableDrag();

		HashMap<String, Object> map = new HashMap<String, Object>();
		actionListener.onLoadData(map);

		pathList = SettingProperties.getLatestPaths(context);
		//ArrayAdapter的下拉列表，每一项都是限定长度，文字超过会被省略，由于360dp的手机宽度太窄，因此对于长路径需要多行显示，因此自定义适配
//		pathAdapter = new ArrayAdapter<String>(context
//				, android.R.layout.simple_dropdown_item_1line, pathList);
		pathAdapter = new PathListAdapter();
		pathSpinner.setAdapter(pathAdapter);
		pathSpinner.setOnItemSelectedListener(this);
		pathSpinner.setSelection(0);

		nameEdit.setText(preName);
	}

	@Override
	protected View getCustomView() {
		View view = LayoutInflater.from(getContext()).inflate(R.layout.video_saveas_dialog, null);
		pathSpinner = (Spinner) view.findViewById(R.id.video_saveas_path);
		browseButton = (ImageView) view.findViewById(R.id.video_saveas_path_browse);
		nameEdit = (EditText) view.findViewById(R.id.video_saveas_filename);
		okButton = (TextView) view.findViewById(R.id.video_saveas_ok);
		cancelButton = (TextView) view.findViewById(R.id.video_saveas_cancel);
		browseButton.setOnClickListener(this);
		cancelButton.setOnClickListener(this);
		okButton.setOnClickListener(this);
		if (Application.isLollipop()) {
			okButton.setBackgroundResource(R.drawable.item_background_material);
			cancelButton.setBackgroundResource(R.drawable.item_background_material);
			browseButton.setBackgroundResource(R.drawable.item_background_borderless_material);
		}
		return view;
	}

	@Override
	public void onClick(View view) {

		if (view == okButton) {
			String name = nameEdit.getText().toString();
			if (name.trim().length() == 0) {
				nameEdit.setError(getContext().getResources().getString(R.string.video_saveas_title_notnull));
				return;
			}

			setPathAsLatest();
			SettingProperties.saveLatestPath(getContext(), pathList);
			actionListener.onOk(pathList.get(pathSpinner.getSelectedItemPosition()) + "/" + name);
			dismiss();
		}
		else if (view == cancelButton) {
			dismiss();
		}
		else if (view == browseButton) {
			folderSelectDialog = new SimpleFolderSelectDialog(getContext(), new OnVideoDialogListener() {

				@Override
				public void onOk(Object object) {
					String path = (String) object;
					onSelectNewPath(path);
				}

				@Override
				public void onLoadData(Object object) {

				}

				@Override
				public boolean onCancel() {
					return false;
				}
			});
			folderSelectDialog.show();
		}
	}

	private void setPathAsLatest() {
		int index = pathSpinner.getSelectedItemPosition();
		if (index != 0) {
			String path = pathList.remove(index);
			pathList.add(0, path);
		}
	}

	protected void onSelectNewPath(String path) {
		int existIndex = -1;
		for (int i = 0; i < pathList.size(); i ++) {
			if (pathList.get(i).equals(path)) {
				existIndex = i;
				break;
			}
		}
		if (existIndex == -1) {
			pathList.add(0, path);
			pathAdapter.notifyDataSetChanged();
			pathSpinner.setSelection(0);
		}
		else {
			pathSpinner.setSelection(existIndex);
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int index,
							   long arg3) {

	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {

	}

	public void notifyOrientaionChanged(int orientation) {
		if (folderSelectDialog != null && folderSelectDialog.isShowing()) {
			folderSelectDialog.notifyOrientaionChanged();
		}
	}

	private class PathListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return pathList.size();
		}

		@Override
		public Object getItem(int position) {
			return pathList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup group) {
			TextView textView = null;
			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.video_saveas_spinner_item, null);
				textView = (TextView) convertView.findViewById(R.id.browser_spinner_text);
				convertView.setTag(textView);
			}
			else {
				textView = (TextView) convertView.getTag();
			}

			textView.setText(pathList.get(position));
			return convertView;
		}

	}
}
