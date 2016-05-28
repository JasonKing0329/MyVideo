package com.king.app.video;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.king.app.video.controller.Constants;
import com.king.app.video.controller.DataController;
import com.king.app.video.controller.DisplayHelper;
import com.king.app.video.controller.ObjectCache;
import com.king.app.video.controller.DataController.SortType;
import com.king.app.video.controller.VideoFormatter;
import com.king.app.video.customview.ScrollTab;
import com.king.app.video.customview.StarScoreView.OnStarCheckListener;
import com.king.app.video.data.personal.DatabaseInfor;
import com.king.app.video.data.personal.VideoPersonalData;
import com.king.app.video.model.VideoData;
import com.king.app.video.model.VideoOrder;
import com.king.app.video.setting.SettingActivity;
import com.king.app.video.setting.SettingProperties;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;

public class VideoListActivity extends Activity implements OnItemClickListener
		, OnClickListener, OnItemLongClickListener, OnMenuItemClickListener
		, TextWatcher, OnStarCheckListener {

	private final int MODE_LIST = 0;
	private final int MODE_GRID = 1;
	private int currentMode;
	private SortType sortType;

	private final int NUM_LOAD_THUMB_PUBLISH = 5;

	private ListView listView;
	private VideoListAdapter adapter;
	private GridView gridView;
	private VideoGridAdapter gridAdapter;
	private AbstractVideoListAdapter abstractAdapter;

	private LinearLayout progressLayout;
	private PopupMenu sortMenu, moreMenu;

	private List<VideoData> videoList;
	private List<VideoData> totalVideoList;
	private DataController dataController;

	private int orientation;
	private DetailThumbnailDialog detailThumbnailDialog;
	private OrderManagerDialog orderManagerDialog;

	private TextView moreAction, gridAction, sortAction, searchAction, closeAction, refreshAction
			, deleteAction, addToAction;
	private EditText searchEdit;
	private LinearLayout selectAllLayout;
	private CheckBox selectAllCheck;
	private ScrollTab scrollTab;
	private ScrollTabAdapter scrollTabAdapter;
	private List<VideoOrder> orderList;

	private TextView actionGroupNormal[], actionGroupSelect[];

	private VideoOrder currentOrder;

	private int selectVideoPosition;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		com.king.app.video.setting.Configuration.init();
		DatabaseInfor.prepare(this);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.video_list);
		orientation = getResources().getConfiguration().orientation;
		listView = (ListView) findViewById(R.id.video_listview);
		gridView = (GridView) findViewById(R.id.video_gridview);
		progressLayout = (LinearLayout) findViewById(R.id.video_list_progress);
		moreAction = (TextView) findViewById(R.id.video_action_more);
		gridAction = (TextView) findViewById(R.id.video_action_grid);
		sortAction = (TextView) findViewById(R.id.video_action_sort);
		searchAction = (TextView) findViewById(R.id.video_action_search);
		closeAction = (TextView) findViewById(R.id.video_action_searchclose);
		searchEdit = (EditText) findViewById(R.id.video_action_search_edit);
		refreshAction = (TextView) findViewById(R.id.video_action_refresh);
		scrollTab = (ScrollTab) findViewById(R.id.video_scrolltab);
		selectAllCheck = (CheckBox) findViewById(R.id.video_action_selectall);
		selectAllLayout = (LinearLayout) findViewById(R.id.video_actionbar_selectall);
		deleteAction = (TextView) findViewById(R.id.video_action_delete);
		addToAction = (TextView) findViewById(R.id.video_action_addto);

		actionGroupNormal = new TextView[5];
		actionGroupNormal[0] = moreAction;
		actionGroupNormal[1] = refreshAction;
		actionGroupNormal[2] = gridAction;
		actionGroupNormal[3] = sortAction;
		actionGroupNormal[4] = searchAction;
		actionGroupSelect = new TextView[2];
		actionGroupSelect[0] = deleteAction;
		actionGroupSelect[1] = addToAction;

		loadDisplayMode();

		if (Application.isLollipop()) {
			moreAction.setBackgroundResource(R.drawable.item_background_borderless_material);
			gridAction.setBackgroundResource(R.drawable.item_background_borderless_material);
			sortAction.setBackgroundResource(R.drawable.item_background_borderless_material);
			searchAction.setBackgroundResource(R.drawable.item_background_borderless_material);
			closeAction.setBackgroundResource(R.drawable.item_background_borderless_material);
			refreshAction.setBackgroundResource(R.drawable.item_background_borderless_material);
			deleteAction.setBackgroundResource(R.drawable.item_background_borderless_material);
			addToAction.setBackgroundResource(R.drawable.item_background_borderless_material);
		}

		listView.setOnItemClickListener(this);
		gridView.setOnItemClickListener(this);
		listView.setOnItemLongClickListener(this);
		gridView.setOnItemLongClickListener(this);

		if (currentMode == MODE_LIST) {
			gridAction.setText(getResources().getString(R.string.video_action_grid));
		}
		else {
			gridAction.setText(getResources().getString(R.string.video_action_list));
		}

		dataController = new DataController();

		initScrollTab();
	}

	public void initAction() {
		moreAction.setOnClickListener(this);
		gridAction.setOnClickListener(this);
		sortAction.setOnClickListener(this);
		searchAction.setOnClickListener(this);
		closeAction.setOnClickListener(this);
		refreshAction.setOnClickListener(this);
		selectAllCheck.setOnClickListener(this);
		searchEdit.addTextChangedListener(this);
		deleteAction.setOnClickListener(this);
		addToAction.setOnClickListener(this);

		sortMenu = new PopupMenu(this, sortAction);
		sortMenu.getMenuInflater().inflate(R.menu.video_list_sort, sortMenu.getMenu());
		sortMenu.setOnMenuItemClickListener(this);

		moreMenu = new PopupMenu(this, moreAction);
		moreMenu.getMenuInflater().inflate(R.menu.video, moreMenu.getMenu());
		moreMenu.setOnMenuItemClickListener(this);
	}

	private void loadDisplayMode() {
		currentMode = MODE_LIST;
		if (SettingProperties.getDisplayMode(this).equals(
				getResources().getStringArray(R.array.video_setting_display_mode)[1])) {
			currentMode = MODE_GRID;
		}
	}

	public void loadSortType() {
		sortType = SortType.NONE;
		String[] types = getResources().getStringArray(R.array.video_setting_sort_type);
		String typePref = SettingProperties.getSortType(this);
		if (typePref.equals(types[1])) {
			sortType = SortType.NAME;
		}
		else if (typePref.equals(types[2])) {
			sortType = SortType.ADDED_DATE;
		}
		else if (typePref.equals(types[3])) {
			sortType = SortType.TYPE;
		}
		else if (typePref.equals(types[4])) {
			sortType = SortType.DURATION;
		}
		else if (typePref.equals(types[5])) {
			sortType = SortType.SIZE;
		}
		else if (typePref.equals(types[6])) {
			sortType = SortType.SCORE;
		}
	}

	private void initScrollTab() {
		orderList = dataController.queryVideoOrders();
		sortOrderListFromPref();

		scrollTabAdapter = new ScrollTabAdapter(this, orderList);
		scrollTab.setAdapter(scrollTabAdapter);
		scrollTab.setOnTabSelectListener(new ScrollTab.OnTabSelectListener() {

			@Override
			public void onSelect(View view, int position) {
				scrollTabAdapter.onSelect(position);
				scrollTabAdapter.notifyDataSetChanged();

				currentOrder = orderList.get(position);
				progressLayout.setVisibility(View.VISIBLE);
				new VideoLoadTask(currentOrder).execute();
			}
		});
		currentOrder = orderList.get(0);
		new VideoLoadTask(currentOrder).execute();
	}

	private void sortOrderListFromPref() {
		if (orderList != null && orderList.size() > 0) {
			List<String> names = SettingProperties.getOrderList(this);
			if (names != null) {
				HashMap<String, VideoOrder> map = new HashMap<String, VideoOrder>();
				for (VideoOrder order:orderList) {
					map.put(order.getName(), order);
				}
				for (int i = 0; i < names.size(); i ++) {
					orderList.set(i, map.get(names.get(i)));
				}
			}
		}
	}

	/**
	 * only load video from content provider
	 * after done, execute load image and personal data
	 * @author Yang Jing
	 *
	 */
	private class VideoLoadTask extends AsyncTask<Void, Void, Void> {

		private VideoOrder order;
		/**
		 * if order is null, query default(all from content provider)
		 * @param order
		 */
		public VideoLoadTask(VideoOrder order) {
			this.order = order;
		}
		@Override
		protected Void doInBackground(Void... arg0) {

			if (currentOrder.getId() == Constants.ORDER_DEFAULT_ID) {//load from default
				videoList = dataController.queryVideos(VideoListActivity.this);
			}
			else {
				videoList = dataController.queryVideos(VideoListActivity.this, order);
			}
			//防止刷新或者切换列表后total list没有变化
			totalVideoList = null;
			formatVideoSize();
			formatVideoTime();
			publishProgress();
			return null;
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			initAction();
			loadSortType();

			//score is personal data, need execute after DatabaseTask done
			if (sortType != SortType.SCORE) {
				dataController.sortList(videoList, sortType);
			}
			if (currentMode == MODE_LIST) {
				adapter = new VideoListAdapter(VideoListActivity.this, videoList);
				adapter.setOnStarClickListener(VideoListActivity.this);
				listView.setAdapter(adapter);
				listView.setVisibility(View.VISIBLE);
				abstractAdapter = adapter;

				if (gridAdapter != null) {
					gridAdapter.updateDataList(videoList);
					gridAdapter.notifyDataSetChanged();
				}
			}
			else {
				gridAdapter = new VideoGridAdapter(VideoListActivity.this, videoList);
				gridAdapter.setOnStarClickListener(VideoListActivity.this);
				gridView.setAdapter(gridAdapter);
				gridView.setVisibility(View.VISIBLE);
				abstractAdapter = gridAdapter;

				if (adapter != null) {
					adapter.updateDataList(videoList);
					adapter.notifyDataSetChanged();
				}
			}
			progressLayout.setVisibility(View.GONE);

			if (order.getId() == Constants.ORDER_DEFAULT_ID) {
				//只有在查询全部video的时候执行这个，如果是查询列表中的记录一定不能执行该操作，会引起大量删除操作
				new DatabaseTask().execute();
			}
			else {
				new ThumnailTask().execute();
			}
			/**
			 * 由于DatabaseTask和ThumnailTask都有notifyRefresh的操作，最好不要异步执行，容易造成问题
			 */
			//new ThumnailTask().execute();
			super.onProgressUpdate(values);
		}

	}

	/**
	 * update personal data(load personal data and delete not existed record)
	 * @author Yang Jing
	 *
	 */
	private class DatabaseTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... arg0) {
			if (videoList != null) {
				dataController.updatePersonalDatabase(videoList);
				publishProgress();
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			formatVideoLastPlayTime();
			if (sortType == SortType.SCORE) {
				dataController.sortList(videoList, sortType);
			}
			notifyRefresh();

			/**
			 * 由于DatabaseTask和ThumnailTask都有notifyRefresh的操作，最好不要异步执行，容易造成问题
			 */
			new ThumnailTask().execute();
			super.onProgressUpdate(values);
		}
	}

	/**
	 * load video thumbnail
	 * @author Yang Jing
	 *
	 */
	private class ThumnailTask extends AsyncTask<Void, Boolean, Void> {

		@Override
		protected Void doInBackground(Void... arg0) {
			boolean isOver = false;
			if (videoList != null) {
				for (int i = 0; i < videoList.size(); i ++) {
					VideoData data = videoList.get(i);
					Bitmap bitmap = dataController.getVideoThumbnail(data.getPath());
					data.setThumbnail(bitmap);
					if (i % NUM_LOAD_THUMB_PUBLISH == NUM_LOAD_THUMB_PUBLISH - 1 && i != videoList.size() - 1) {
						publishProgress(false);
					}
				}
			}
			isOver = true;
			publishProgress(isOver);
			return null;
		}

		@Override
		protected void onProgressUpdate(Boolean... values) {
			if (currentMode == MODE_LIST) {
				adapter.notifyDataSetChanged();
			}
			else {
				gridAdapter.notifyDataSetChanged();
			}
			super.onProgressUpdate(values);
		}
	}

	public void formatVideoSize() {
		if (videoList != null) {
			for (VideoData data:videoList) {
				data.setSizeLong(Long.parseLong(data.getSize()));
				data.setSize(VideoFormatter.formatSize(data.getSizeLong()));
			}
		}
	}

	public void formatVideoLastPlayTime() {
		if (videoList != null) {
			for (VideoData videoData:videoList) {
				//v2.2 video download from xunlei, duration is null
				VideoPersonalData data = videoData.getPersonalData();
				if (data != null) {
					if (data.getLastPlayPosition() != 0) {
						data.setLastPlayStr(VideoFormatter.formatTime(data.getLastPlayPosition()));
					}
				}
			}
		}
	}

	public void formatVideoTime() {
		if (videoList != null) {
			for (VideoData data:videoList) {
				//v2.2 video download from xunlei, duration is null
				if (data.getDuration() == null) {
					data.setDuration("00:00:00");
				}
				else {
					data.setDurationInt(Integer.parseInt(data.getDuration()));
				}
				data.setDuration(VideoFormatter.formatTime(data.getDurationInt()));
			}
		}
	}

	private void openVideo(VideoData data) {

		Intent intent = new Intent();
		Uri uri = data.getUri();
		intent.setData(uri);
		intent.putExtra(Constants.PLAY_VIDEO_ID, data.getId());
		intent.setClass(VideoListActivity.this, VideoActivity.class);
		List<VideoData> copyList = new ArrayList<VideoData>(videoList);
		ObjectCache.putVideoList(copyList);
		ObjectCache.putVideoOrder(currentOrder);
		startActivityForResult(intent, Constants.PLAY_VIDEO_REQUEST_ID);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == Constants.PLAY_VIDEO_REQUEST_ID) {
			if (resultCode == Constants.PLAY_VIDEO_RESULT_ID) {
				int updateTime = data.getIntExtra(Constants.PLAY_VIDEO_TIME_UPDATE, 0);
				VideoPersonalData personalData = videoList.get(selectVideoPosition).getPersonalData();
				personalData.setLastPlayPosition(updateTime);
				personalData.setLastPlayStr(VideoFormatter.formatTime(personalData.getLastPlayPosition()));
				notifyRefresh();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {

		if (newConfig.orientation != orientation) {
			orientation = newConfig.orientation;
			if (gridView != null && currentMode == MODE_GRID) {
				gridView.setNumColumns(getResources().getInteger(R.integer.video_grid_column));
			}
			if (detailThumbnailDialog != null && detailThumbnailDialog.isShowing()) {
				detailThumbnailDialog.onConfigurationChanged(newConfig.orientation);
			}
		}
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long arg3) {

		if (abstractAdapter.isSelectMode()) {
			if (abstractAdapter.gonnaCheckAll(position, view)) {
				notifyAllChecked(true);
			}
			else if (abstractAdapter.gonnaUnCheckAll(position, view)) {
				notifyAllChecked(false);
			}
			abstractAdapter.checkItem(position, view);
		}
		else {
			selectVideoPosition = position;
			openVideo(videoList.get(position));
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long arg3) {
		selectVideoPosition = position;
		detailThumbnailDialog = new DetailThumbnailDialog(this, videoList.get(position)
				, new DetailThumbnailDialog.OnActionListener() {

			@Override
			public void onRename() {
				notifyRefresh();
			}

			@Override
			public void onDelete() {
				videoList.remove(selectVideoPosition);
				notifyRefresh();
			}

			@Override
			public void onPlay() {
				openVideo(videoList.get(selectVideoPosition));
			}
		});
		detailThumbnailDialog.show();
		return true;
	}
	@Override
	public void onClick(View view) {
		if (view == moreAction) {
			moreMenu.show();
		}
		else if (view ==  refreshAction) {
			progressLayout.setVisibility(View.VISIBLE);
			initScrollTab();
		}
		else if (view ==  sortAction) {
			sortMenu.show();
		}
		else if (view ==  gridAction) {
			if (currentMode == MODE_LIST) {
				currentMode = MODE_GRID;
				gridAction.setText(getResources().getString(R.string.video_action_list));
				if (gridAdapter == null) {
					gridAdapter = new VideoGridAdapter(this, videoList);
					gridView.setAdapter(gridAdapter);
				}
				abstractAdapter = gridAdapter;
				gridView.setNumColumns(getResources().getInteger(R.integer.video_grid_column));
				listView.setVisibility(View.GONE);
				gridView.setVisibility(View.VISIBLE);
			}
			else {
				currentMode = MODE_LIST;
				gridAction.setText(getResources().getString(R.string.video_action_grid));
				if (adapter == null) {
					adapter = new VideoListAdapter(VideoListActivity.this, videoList);
					listView.setAdapter(adapter);
				}
				abstractAdapter = adapter;
				listView.setVisibility(View.VISIBLE);
				gridView.setVisibility(View.GONE);
			}
		}
		else if (view ==  searchAction) {
			if (!DisplayHelper.isTabModel(this)) {
				sortAction.setVisibility(View.GONE);
				gridAction.setVisibility(View.GONE);
				refreshAction.setVisibility(View.GONE);
				moreAction.setVisibility(View.GONE);
			}
			closeAction.setVisibility(View.VISIBLE);
			searchAction.setVisibility(View.GONE);
			searchEdit.setVisibility(View.VISIBLE);
			searchEdit.startAnimation(AnimationUtils.loadAnimation(this, R.anim.video_appear));
		}
		else if (view ==  closeAction) {
			if (!DisplayHelper.isTabModel(this)) {
				sortAction.setVisibility(View.VISIBLE);
				gridAction.setVisibility(View.VISIBLE);
				refreshAction.setVisibility(View.VISIBLE);
				moreAction.setVisibility(View.VISIBLE);
			}
			closeAction.setVisibility(View.GONE);
			searchAction.setVisibility(View.VISIBLE);
			searchEdit.setVisibility(View.GONE);
			searchEdit.startAnimation(AnimationUtils.loadAnimation(this, R.anim.video_disappear));
		}
		else if (view == selectAllCheck) {
			notifyAllChecked(selectAllCheck.isChecked());
			if (selectAllCheck.isChecked()) {
				selectAll();
			}
			else {
				unSelectAll();
			}
		}
		else if (view == deleteAction) {
			if (abstractAdapter.getCheckedItemCount() > 0) {
				dataController.deleteVideoFromOrder(abstractAdapter.getCheckedMap()
						, videoList, currentOrder);
				notifyRefresh();
				exitSelectMode();
			}
		}
		else if (view == addToAction) {
			OrderManagerDialog dialog = new OrderManagerDialog(this);
			//it shouldn't add contents to default order(default order query all videos)
			dialog.hideDefaultOrder();
			dialog.showAsOrderChooser(
					new OrderManagerDialog.OnOrderSelectListener() {

						@Override
						public void onSelect(VideoOrder order) {
							dataController.addVideoListToOrder(abstractAdapter.getCheckedMap()
									, videoList, order);
							exitSelectMode();
						}
					});
		}
	}

	private void selectAll() {
		abstractAdapter.selectAll();
		abstractAdapter.notifyDataSetChanged();
	}

	private void unSelectAll() {
		abstractAdapter.unSelectAll();
		abstractAdapter.notifyDataSetChanged();
	}

	public void notifyAllChecked(boolean checked) {
		selectAllCheck.setChecked(checked);
		if (checked) {
			selectAllCheck.setText(getResources().getString(R.string.video_action_unselectall));
		}
		else {
			selectAllCheck.setText(getResources().getString(R.string.video_action_selectall));
		}
	}

	@Override
	public void onBackPressed() {
		if (abstractAdapter != null && abstractAdapter.isSelectMode()) {
			exitSelectMode();
		}
		else {
			super.onBackPressed();
		}
	}

	private void exitSelectMode() {
		scrollTab.enable();
		selectAllLayout.setVisibility(View.GONE);
		notifyAllChecked(false);
		setActionGroup(actionGroupSelect, false);
		setActionGroup(actionGroupNormal, true);
		abstractAdapter.setSelectMode(false);
		abstractAdapter.notifyDataSetChanged();
	}

	private void enterSelectMode() {
		scrollTab.disable();
		selectAllLayout.setVisibility(View.VISIBLE);
		setActionGroup(actionGroupNormal, false);
		setActionGroup(actionGroupSelect, true);

		//contents in default order shouldn't be deleted
		if (currentOrder == null || currentOrder.getId() == Constants.ORDER_DEFAULT_ID) {
			deleteAction.setVisibility(View.GONE);
		}

		abstractAdapter.setSelectMode(true);
		abstractAdapter.notifyDataSetChanged();
	}

	private void setActionGroup(TextView[] group, boolean visible) {
		for (TextView tv:group) {
			tv.setVisibility(visible ? View.VISIBLE : View.GONE);
		}
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.video_menu_sort_date_added:
				dataController.sortList(videoList, SortType.ADDED_DATE);
				notifyRefresh();
				break;
			case R.id.video_menu_sort_duration:
				dataController.sortList(videoList, SortType.DURATION);
				notifyRefresh();
				break;
			case R.id.video_menu_sort_size:
				dataController.sortList(videoList, SortType.SIZE);
				notifyRefresh();
				break;
			case R.id.video_menu_sort_name:
				dataController.sortList(videoList, SortType.NAME);
				notifyRefresh();
				break;
			case R.id.video_menu_sort_type:
				dataController.sortList(videoList, SortType.TYPE);
				notifyRefresh();
				break;
			case R.id.video_menu_sort_score:
				dataController.sortList(videoList, SortType.SCORE);
				notifyRefresh();
				break;
			case R.id.video_menu_setting:
				startActivity(new Intent().setClass(this, SettingActivity.class));
				break;
			case R.id.video_menu_export:
				DatabaseInfor.export(this);
				break;
			case R.id.video_menu_order_manage:
				orderManagerDialog = new OrderManagerDialog(this);
				orderManagerDialog.show();
				break;
			case R.id.video_menu_select:
				if (videoList != null && videoList.size() > 0) {
					enterSelectMode();
				}
				break;

			default:
				break;
		}
		return true;
	}

	private void notifyRefresh() {
		if (adapter != null) {
			adapter.notifyDataSetChanged();
		}
		if (gridAdapter != null) {
			gridAdapter.notifyDataSetChanged();
		}
	}
	@Override
	public void afterTextChanged(Editable arg0) {

	}

	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
								  int arg3) {

	}

	@Override
	public void onTextChanged(CharSequence text, int arg1, int arg2, int arg3) {
		if (totalVideoList == null) {
			totalVideoList = new ArrayList<VideoData>();
			for (int i = 0; i < videoList.size(); i ++) {
				totalVideoList.add(videoList.get(i));
			}
		}

		videoList.clear();
		if (text.toString().trim().length() == 0) {
			for (int i = 0; i < totalVideoList.size(); i ++) {
				videoList.add(totalVideoList.get(i));
			}
		}
		else {
			for (int i = 0; i < totalVideoList.size(); i ++) {
				if (totalVideoList.get(i).getName().toLowerCase(Locale.CHINA).contains(text.toString().toLowerCase())) {
					videoList.add(totalVideoList.get(i));
				}
			}
		}
		notifyRefresh();
	}

	@Override
	public void onCheckStar(View view, int score) {
		int position = (Integer) view.getTag();
		Log.d("VideoListActivity", "onCheckStar " + position + " score=" + score);
		VideoData videoData = videoList.get(position);
		videoData.getPersonalData().setScore(score);
		dataController.updateVideoPersonalData(videoData);
	}

}
