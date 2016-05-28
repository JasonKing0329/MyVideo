package com.king.app.video;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.king.app.video.PlayListAdapter.DeleteCallback;
import com.king.app.video.customview.DragSideBar;
import com.king.app.video.model.VideoData;
import com.king.app.video.setting.SettingProperties;
import com.king.lib.listview.DragSortController;
import com.king.lib.listview.DragSortListView;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;

public class PlayListManager implements OnClickListener, OnItemClickListener
		, DeleteCallback {

//	private final String TAG = "PlayListManager";

	private Context mContext;

	/**
	 * 可拖拽打开、关闭的播放列表
	 */
	private DragSideBar dragSideBar;
	/**
	 * 播放列表的空白部分，注册监听事件避免下层view接收touch事件
	 */
	private View bkView;

	/*******************播放列表采用可拖动改变顺序的开源库********************/
	private DragSortListView playListView;
	private DragSortController dragSortController;
	public int dragStartMode = DragSortController.ON_DOWN;
	public int removeMode = DragSortController.FLING_REMOVE;

	private PlayListAdapter mAdapter;
	private List<VideoData> videoList;

	/**
	 * 当前播放index
	 */
	private int currentPlayPosition;

	public PlayListManager(Context context, DragSideBar sideBar) {
		mContext = context;
		dragSideBar = sideBar;
		dragSideBar.setLayoutRes(R.layout.layout_playlist);

		Activity view = (Activity) context;
		bkView = view.findViewById(R.id.dragsidebar_bk);
		playListView = (DragSortListView) view
				.findViewById(R.id.playlist_listview);

		bkView.setOnClickListener(this);
		playListView.setOnItemClickListener(this);
	}

	public void initListData(List<VideoData> list, String videoId, int orderId) {
		videoList = list;

		//如果是打开上次播放的列表，则按照上次的列表顺序加载。否则按照新的列表默认顺序打开
//		sortListFromPref(orderId);

		mAdapter = new PlayListAdapter(mContext, videoList, this);
		for (int i = 0; i < videoList.size(); i ++) {
			if (videoId.equals(videoList.get(i).getId())) {
				currentPlayPosition = i;
				break;
			}
		}
		mAdapter.setPlayPosition(currentPlayPosition);

		dragSortController = buildController(playListView);
		playListView.setFloatViewManager(dragSortController);
		playListView.setOnTouchListener(dragSortController);
		playListView.setDragEnabled(true);
		playListView.setAdapter(mAdapter);
		playListView.setDropListener(onDrop);
		mAdapter.enableDrag(true);
	}

	/*考虑到视频经常更换，暂时就不对列表进行排序了
	private void sortListFromPref(int orderId) {
		if (videoList != null && videoList.size() > 0) {
			int cacheOrderId = SettingProperties.getCacheOrderId(mContext);
			if (cacheOrderId == orderId) {//缓存的是当前列表
				List<String> ids = SettingProperties.getPlayListIds(mContext);
				if (ids != null) {
					HashMap<String, VideoData> map = new HashMap<String, VideoData>();
					for (VideoData data : videoList) {
						map.put(data.getId(), data);
					}
					for (int i = 0; i < ids.size(); i++) {
						videoList.set(i, map.get(ids.get(i)));
					}
				}
			}
			else {//打开了新的列表
				SettingProperties.saveCacheOrderId(mContext, orderId);
				saveSortedList();
			}
		}
	}
	*/

	private void saveSortedList() {
		new Thread() {
			public void run() {
				List<String> ids = new ArrayList<String>();
				for (VideoData order : videoList) {
					ids.add(order.getId());
				}
				if (ids.size() > 0) {
					SettingProperties.savePlayIdList(mContext, ids);
				}
			}
		}.start();
	}

	private DragSortListView.DropListener onDrop = new DragSortListView.DropListener() {
		@Override
		public void drop(int from, int to) {
			if (from != to) {

				//改变排序后，正在播放的位置可能会随之改变，要通知adapter更新
				if (from == currentPlayPosition) {
					currentPlayPosition = to;
				}
				else {
					if (from > to) {
						if (currentPlayPosition >= to && currentPlayPosition < from) {
							currentPlayPosition ++;
						}
					}
					else {
						if (currentPlayPosition > from && currentPlayPosition <= to) {
							currentPlayPosition --;
						}
					}
				}
				mAdapter.setPlayPosition(currentPlayPosition);

				//改变位置
				VideoData data = videoList.remove(from);
				videoList.add(to, data);
				mAdapter.notifyDataSetChanged();

				//保存当前列表
				saveSortedList();
			}
		}
	};

	public DragSortController buildController(DragSortListView dslv) {
		// defaults are
		// dragStartMode = onDown
		// removeMode = flingRight
		DragSortController controller = new DragSortController(dslv);
		controller.setDragHandleId(R.id.playlist_item_drag);
		// controller.setClickRemoveId(R.id.click_remove);
		controller.setRemoveEnabled(false);
		controller.setSortEnabled(true);
		controller.setDragInitMode(dragStartMode);
		controller.setRemoveMode(removeMode);
		return controller;
	}

	@Override
	public void onClick(View v) {

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
							long id) {
		currentPlayPosition = position;
		mAdapter.setPlayPosition(position);
		mAdapter.notifyDataSetChanged();
		playVideo(position);
	}

	@Override
	public void onDeleteItem(int position) {
		videoList.remove(position);
		mAdapter.notifyDataSetChanged();
	}

	/**
	 * 播放position位置的视频
	 * @param position
	 */
	private void playVideo(int position) {
		String path = videoList.get(position).getPath();
		((VideoActivity) mContext).pause();
		((VideoActivity) mContext).destroy();
		((VideoActivity) mContext).playVideo(Uri.fromFile(new File(path)), null, false, true);
	}

	/**
	 * 当前视频播放完毕之后播放列表中下一个视频内容
	 */
	public void playNext() {
		if (videoList != null) {
			if (currentPlayPosition < videoList.size() - 1) {
				currentPlayPosition ++;
				mAdapter.setPlayPosition(currentPlayPosition);
				mAdapter.notifyDataSetChanged();
				playVideo(currentPlayPosition);
			}
		}
	}

	public void onConfigurationChanged(int orientation) {
		dragSideBar.onConfigurationChanged();
	}

	/**
	 * 外部按钮控制播放列表打开、关闭
	 * @param sideCtrlButton
	 */
	public void onSideCtrlClicked(ImageView sideCtrlButton) {
		if (dragSideBar.isOpen()) {
			dragSideBar.forceDismiss(true);
			sideCtrlButton.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.playlist_ctrl_btn_open));
		}
		else {
			dragSideBar.forceShow(true);
			sideCtrlButton.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.playlist_ctrl_btn_close));
		}
	}

	public boolean onBackPressed() {

		if (dragSideBar.isOpen()) {
			dragSideBar.forceDismiss(true);
			return true;
		}
		return false;
	}

	/**
	 * disable DragSideBar
	 */
	public void disable() {
		dragSideBar.setVisibility(View.GONE);
	}
}
