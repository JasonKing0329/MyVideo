package com.king.app.video;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.king.app.video.controller.Constants;
import com.king.app.video.controller.DataController;
import com.king.app.video.controller.SelectService;
import com.king.app.video.model.VideoOrder;
import com.king.app.video.setting.SettingProperties;
import com.king.lib.listview.DragSortController;
import com.king.lib.listview.DragSortItemView;
import com.king.lib.listview.DragSortListView;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Toast;

public class OrderManagerDialog extends VideoDialog implements OnClickListener
		, OnItemLongClickListener, OnItemClickListener{

	public interface OnOrderSelectListener {
		public void onSelect(VideoOrder order);
	}

	private TextView titleView;
	private CheckBox selectAllCheck;
	private TextView addAction, closeAction, deleteAction, renameAction;

	private DragSortListView listView;
	private DragSortController mController;
	public int dragStartMode = DragSortController.ON_DOWN;
	public int removeMode = DragSortController.FLING_REMOVE;

	private DataController dataController;
	private List<VideoOrder> orderList;
	private OrderAdapter orderAdapter;
	private OnOrderSelectListener onOrderSelectListener;

	private boolean showAsOrderChooser;

	public OrderManagerDialog(Context context) {
		super(context);
		applyGreyStyle();
		enableDrag();
		//calculateSize();

		dataController = new DataController();
		orderList = dataController.queryVideoOrders();
		sortOrderListFromPref();
		orderAdapter = new OrderAdapter();


		mController = buildController(listView);
		listView.setFloatViewManager(mController);
		listView.setOnTouchListener(mController);
		listView.setDragEnabled(true);
		listView.setAdapter(orderAdapter);
		listView.setDropListener(onDrop);
		orderAdapter.enableDrag(true);
		//listView.setRemoveListener(onRemove);

	}

	private void sortOrderListFromPref() {
		if (orderList != null && orderList.size() > 0) {
			List<String> names = SettingProperties.getOrderList(getContext());
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

	private void saveSortedOrder() {
		new Thread() {
			public void run() {
				List<String> names = new ArrayList<String>();
				for (VideoOrder order:orderList) {
					names.add(order.getName());
				}
				if (names.size() > 0) {
					SettingProperties.saveOrderList(getContext(), names);
				}
			}
		}.start();
	}

	private DragSortListView.DropListener onDrop =
			new DragSortListView.DropListener() {
				@Override
				public void drop(int from, int to) {
					if (from != to) {
						VideoOrder order = orderList.remove(from);
						orderList.add(to, order);
						orderAdapter.notifyDataSetChanged();

						saveSortedOrder();
					}
				}
			};

	public DragSortController buildController(DragSortListView dslv) {
		// defaults are
		//   dragStartMode = onDown
		//   removeMode = flingRight
		DragSortController controller = new DragSortController(dslv);
		controller.setDragHandleId(R.id.video_order_manager_item_drag_ctrl);
		//controller.setClickRemoveId(R.id.click_remove);
		controller.setRemoveEnabled(false);
		controller.setSortEnabled(true);
		controller.setDragInitMode(dragStartMode);
		controller.setRemoveMode(removeMode);
		return controller;
	}
	@Override
	protected View getCustomView() {
		View view = LayoutInflater.from(getContext()).inflate(R.layout.video_order_manage, null);
		addAction = (TextView) view.findViewById(R.id.video_action_add);
		closeAction = (TextView) view.findViewById(R.id.video_action_close);
		deleteAction = (TextView) view.findViewById(R.id.video_action_delete);
		renameAction = (TextView) view.findViewById(R.id.video_action_rename);
		listView = (DragSortListView) view.findViewById(R.id.video_order_manager_list);
		titleView = (TextView) view.findViewById(R.id.video_order_manager_title);
		selectAllCheck = (CheckBox) view.findViewById(R.id.video_order_manager_selectall);
		addAction.setOnClickListener(this);
		closeAction.setOnClickListener(this);
		deleteAction.setOnClickListener(this);
		renameAction.setOnClickListener(this);
		listView.setOnItemClickListener(this);
		listView.setOnItemLongClickListener(this);
		selectAllCheck.setOnClickListener(this);
		if (Application.isLollipop()) {
			addAction.setBackgroundResource(R.drawable.item_background_borderless_material);
			closeAction.setBackgroundResource(R.drawable.item_background_borderless_material);
			deleteAction.setBackgroundResource(R.drawable.item_background_borderless_material);
			renameAction.setBackgroundResource(R.drawable.item_background_borderless_material);
		}
		return view;
	}

	public void showAsOrderChooser(OnOrderSelectListener listener) {
		showAsOrderChooser = true;
		onOrderSelectListener = listener;
		titleView.setText(getContext().getString(R.string.video_action_addto));
		orderAdapter.enableDrag(false);
		listView.setDragEnabled(false);
		show();
	}

	@Override
	public void onClick(View view) {
		if (view == addAction) {
			new SimpleDialogManager().openTextInputDialog(getContext()
					, getContext().getResources().getString(R.string.video_action_add), null
					, new SimpleDialogManager.OnDialogActionListener() {

						@Override
						public void onOk(String name) {
							VideoOrder order = new VideoOrder();
							order.setName(name);
							if (dataController.addNewOrder(order)) {
								orderList.add(order);
								orderAdapter.notifyDataSetChanged();
								saveSortedOrder();
							}
							else {
								Toast.makeText(getContext(), R.string.video_warning_addorder_repeat, Toast.LENGTH_LONG).show();
							}
						}

						@Override
						public void onDismiss() {

						}
					});
		}
		else if (view == deleteAction) {
			dataController.deleteVideoOrder(orderAdapter.getCheckedMap(), orderList);
			exitSelectMode();
			orderAdapter.notifyDataSetChanged();
			saveSortedOrder();
		}
		else if (view == renameAction) {
			new SimpleDialogManager().openTextInputDialog(getContext()
					, getContext().getResources().getString(R.string.video_action_rename)
					, orderList.get(orderAdapter.getCheckedPosition()).getName()
					, new SimpleDialogManager.OnDialogActionListener() {

						@Override
						public void onOk(String name) {
							VideoOrder order = orderList.get(orderAdapter.getCheckedPosition());
							order.setName(name);
							dataController.updateOrder(order);
							exitSelectMode();
							orderAdapter.notifyDataSetChanged();
							saveSortedOrder();
						}

						@Override
						public void onDismiss() {

						}
					});
		}
		else if (view == closeAction) {
			dismiss();
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
	}

	public void notifyAllChecked(boolean checked) {
		selectAllCheck.setChecked(checked);
		if (checked) {
			selectAllCheck.setText(getContext().getResources().getString(R.string.video_action_unselectall));
		}
		else {
			selectAllCheck.setText(getContext().getResources().getString(R.string.video_action_selectall));
		}
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position,
							long arg3) {
		if (showAsOrderChooser) {
			if (onOrderSelectListener != null) {
				onOrderSelectListener.onSelect(orderList.get(position));
				dismiss();
			}
		}
		else {
			if (orderAdapter.isSelectMode()) {
				if (orderAdapter.gonnaCheckAll(position, view)) {
					notifyAllChecked(true);
				}
				else if (orderAdapter.gonnaUnCheckAll(position, view)) {
					notifyAllChecked(false);
				}

				orderAdapter.checkItem(position, view);
				int checkedItem = orderAdapter.getCheckedItemCount();
				if (checkedItem > 1) {
					renameAction.setVisibility(View.GONE);
					addAction.setVisibility(View.GONE);
					deleteAction.setVisibility(View.VISIBLE);
				}
				else if (checkedItem == 1) {
					deleteAction.setVisibility(View.VISIBLE);
					renameAction.setVisibility(View.VISIBLE);
					addAction.setVisibility(View.GONE);
				}
				else {
					addAction.setVisibility(View.VISIBLE);
					deleteAction.setVisibility(View.GONE);
					renameAction.setVisibility(View.GONE);
				}
			}
		}
	}
	@Override
	public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position,
								   long arg3) {
		if (!showAsOrderChooser && orderList.size() > 1) {//如果只有default order也不需要进入select mode，因为默认列表不支持删除和重命名
			if (selectAllCheck.getVisibility() == View.GONE) {
				titleView.setVisibility(View.GONE);
				selectAllCheck.setVisibility(View.VISIBLE);
				orderAdapter.setSelectMode(true);
				listView.setDragEnabled(false);
				orderAdapter.checkItem(position, view);

				deleteAction.setVisibility(View.VISIBLE);
				renameAction.setVisibility(View.VISIBLE);
				addAction.setVisibility(View.GONE);
			}
			else {
				exitSelectMode();
			}
			orderAdapter.notifyDataSetChanged();
		}
		return true;
	}

	private void exitSelectMode() {
		titleView.setVisibility(View.VISIBLE);
		selectAllCheck.setVisibility(View.GONE);
		deleteAction.setVisibility(View.GONE);
		renameAction.setVisibility(View.GONE);
		addAction.setVisibility(View.VISIBLE);
		notifyAllChecked(false);
		orderAdapter.setSelectMode(false);
		listView.setDragEnabled(true);
	}

	@Override
	public void onBackPressed() {
		if (orderAdapter.isSelectMode()) {
			exitSelectMode();
			orderAdapter.notifyDataSetChanged();
		}
		else {
			super.onBackPressed();
		}
	}
	private void selectAll() {
		orderAdapter.selectAll();

		renameAction.setVisibility(View.GONE);
		addAction.setVisibility(View.GONE);
		deleteAction.setVisibility(View.VISIBLE);

		orderAdapter.notifyDataSetChanged();
	}

	private void unSelectAll() {
		orderAdapter.unSelectAll();

		addAction.setVisibility(View.VISIBLE);
		deleteAction.setVisibility(View.GONE);
		renameAction.setVisibility(View.GONE);

		orderAdapter.notifyDataSetChanged();
	}

	private class OrderAdapter extends BaseAdapter implements SelectService {

		private boolean enableDrag;
		private boolean selectMode;
		private SparseBooleanArray checkMap;
		public OrderAdapter() {

			checkMap = new SparseBooleanArray();
		}
		@Override
		public int getCount() {
			return orderList == null ? 0 : orderList.size();
		}

		@Override
		public boolean isSelectMode() {
			return selectMode;
		}

		@Override
		public void setSelectMode(boolean select) {
			selectMode = select;
			enableDrag = !select;

			if (!select) {
				checkMap.clear();
			}
		}

		public void enableDrag(boolean drag) {
			enableDrag = drag;
		}

		@Override
		public Object getItem(int position) {
			return orderList == null ? 0 : orderList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup group) {
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.video_order_manager_item, null);
				holder = new ViewHolder();
				holder.check = (CheckBox) convertView.findViewById(R.id.video_order_manager_item_check);
				holder.textView = (TextView) convertView.findViewById(R.id.video_order_manager_item_title);
				holder.drag = (ImageView) convertView.findViewById(R.id.video_order_manager_item_drag_ctrl);
				convertView.setTag(holder);
			}
			else {
				holder = (ViewHolder) convertView.getTag();
			}

			VideoOrder order = orderList.get(position);
			holder.textView.setText(order.getName() + "(" + order.getTotal() + ")");
			if (selectMode && order.getId() != Constants.ORDER_DEFAULT_ID) {
				holder.check.setVisibility(View.VISIBLE);
				holder.check.setChecked(checkMap.get(position));
			}
			else {
				holder.check.setVisibility(View.GONE);
			}
			if (enableDrag) {
				holder.drag.setVisibility(View.VISIBLE);
			}
			else {
				holder.drag.setVisibility(View.GONE);
			}
			return convertView;
		}

		@Override
		public SparseBooleanArray getCheckedMap() {
			return checkMap;
		}
		@Override
		public void checkItem(int position, View view) {
			if (orderList.get(position).getId() == Constants.ORDER_DEFAULT_ID) {
				return;
			}
			if (checkMap.get(position)) {
				checkMap.put(position, false);
			}
			else {
				checkMap.put(position, true);
			}
			ViewHolder holder = getViewHolder(view);
			holder.check.setChecked(checkMap.get(position));
		}

		@Override
		public int getCheckedPosition() {
			for (int i = 0; i < checkMap.size(); i ++) {
				if (checkMap.valueAt(i)) {
					return checkMap.keyAt(i);
				}
			}
			return 0;
		}

		@Override
		public int getCheckedItemCount() {
			int count = 0;
			for (int i = 0; i < checkMap.size(); i ++) {
				if (checkMap.valueAt(i)) {
					count ++;
				}
			}
			return count;
		}
		@Override
		public void selectAll() {
			if (orderList != null) {
				for (int i = 0; i < orderList.size(); i ++) {
					if (orderList.get(i).getId() != Constants.ORDER_DEFAULT_ID) {
						checkMap.put(i, true);
					}
				}
			}
		}
		@Override
		public void unSelectAll() {
			if (orderList != null) {
				for (int i = 0; i < orderList.size(); i ++) {
					checkMap.put(i, false);
				}
			}
		}
		@Override
		public boolean gonnaCheckAll(int position, View view) {
			if (orderList.get(position).getId() == Constants.ORDER_DEFAULT_ID) {
				return false;
			}

			ViewHolder holder = getViewHolder(view);
			boolean check = holder.check.isChecked();
			if (!check) {
				//if all position already have put value, then must judge this by all checked size
				//if (checkMap.size() == list.size() - 1) {
				if (getCheckedItemCount() == orderList.size() - 2) {//must filt default order, so it's - 2
					return true;
				}
			}
			return false;
		}
		@Override
		public boolean gonnaUnCheckAll(int position, View view) {
			if (orderList.get(position).getId() == Constants.ORDER_DEFAULT_ID) {
				return false;
			}

			ViewHolder holder = getViewHolder(view);
			boolean check = holder.check.isChecked();
			if (check) {
				if (checkMap.size() == orderList.size() - 1) {//all checked, gonna uncheck this one //must filt default order, so it's -1
					return true;
				}
			}
			return false;
		}
	}

	private ViewHolder getViewHolder(View view) {
		ViewHolder holder = null;
		if (view instanceof DragSortItemView) {
			holder = (ViewHolder) ((DragSortItemView) view).getChildAt(0).getTag();
		}
		else {
			holder = (ViewHolder) view.getTag();
		}
		return holder;
	}

	private class ViewHolder {
		CheckBox check;
		TextView textView;
		ImageView drag;
	}

	public void hideDefaultOrder() {
		if (orderList != null) {
			for (int i = 0; i < orderList.size(); i ++) {
				if (orderList.get(i).getId() == Constants.ORDER_DEFAULT_ID) {
					orderList.remove(i);
					break;
				}
			}
		}
	}

}
