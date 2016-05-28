package com.king.app.video;

import com.king.app.video.controller.DisplayHelper;
import com.king.app.video.controller.OnVideoDialogListener;
import com.king.app.video.open.image.CropHelper;
import com.king.app.video.open.image.CropInforView;
import com.king.app.video.open.image.CropView;
import com.king.app.video.open.image.CropView.OnCropAreaChangeListener;
import com.king.app.video.open.image.ZoomListener;
import com.king.app.video.setting.Configuration;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.TextView;
import android.widget.Toast;

public class ScreenshotDialog extends Dialog implements android.view.View.OnClickListener
		, OnCropAreaChangeListener{

	private final String TAG = "ShowImageDialog";
	/**
	 * Gif 模式下，seizeButton将被zoomButton替代
	 */
	private ImageView closeButton, seizeButton, saveButton, undoButton
			, cropFullScreenButton, cropAreaSizeButton;
	private ImageView showImageView;
	private Bitmap bitmap, cropBitmap;
	private boolean isOrienChanged;
	private ListPopupWindow cropAreaSizePopup, zoomPopup;
	private CropView cropView;
	private CropInforView cropInforView;
	private LinearLayout cropActionLayout;
	private LinearLayout actionbar, cropActionbar;
	private TextView doneButton, cancelButton;
	private String[] cropAreaSizeArray;

	/**
	 *
	 * @param context
	 * @param imagePath
	 * @param listener if null, execute default action(dialog自定义“添加至列表”、“设置封面”、“查看详情”实现功能，也提供listener可由引用处定义)
	 * @param actionbarHeight define if window height should consider outside actionbarHeight
	 */
	public ScreenshotDialog(Context context) {
		super(context, R.style.TransparentDialog);
		setContentView(R.layout.dialog_showimage);
		closeButton = (ImageView) findViewById(R.id.actionbar_close);
		seizeButton = (ImageView) findViewById(R.id.actionbar_seize);
		undoButton = (ImageView) findViewById(R.id.actionbar_undo);
		cropFullScreenButton = (ImageView) findViewById(R.id.actionbar_crop_fullscreen);
		saveButton = (ImageView) findViewById(R.id.actionbar_save);
		cropAreaSizeButton = (ImageView) findViewById(R.id.actionbar_crop_areasize);
		showImageView = (ImageView) findViewById(R.id.showimage_imageview);
		cropView = (CropView) findViewById(R.id.showimage_cropview);
		cropView.setOnCropAreaChangeListener(this);
		cropInforView = (CropInforView) findViewById(R.id.showimage_cropvinfor);
		actionbar = (LinearLayout) findViewById(R.id.showimage_actionbar);
		cropActionbar = (LinearLayout) findViewById(R.id.showimage_crop_actionbar);
		cropActionLayout = (LinearLayout) findViewById(R.id.showimage_crop_actionview);
		doneButton = (TextView) findViewById(R.id.showimage_crop_action_done);
		cancelButton = (TextView) findViewById(R.id.showimage_crop_action_cancel);

		closeButton.setOnClickListener(this);
		saveButton.setOnClickListener(this);
		undoButton.setOnClickListener(this);
		seizeButton.setOnClickListener(this);
		cropAreaSizeButton.setOnClickListener(this);
		cropFullScreenButton.setOnClickListener(this);
		doneButton.setOnClickListener(this);
		cancelButton.setOnClickListener(this);
		showImageView.setOnTouchListener(new ZoomListener());

		if (Application.isLollipop()) {
			closeButton.setBackgroundResource(R.drawable.ripple_borderless_white);
			saveButton.setBackgroundResource(R.drawable.ripple_borderless_white);
			seizeButton.setBackgroundResource(R.drawable.ripple_borderless_white);
			undoButton.setBackgroundResource(R.drawable.ripple_borderless_white);
			cropAreaSizeButton.setBackgroundResource(R.drawable.ripple_borderless_white);
			cropFullScreenButton.setBackgroundResource(R.drawable.ripple_borderless_white);
			doneButton.setBackgroundResource(R.drawable.ripple_white);
			cancelButton.setBackgroundResource(R.drawable.ripple_white);
		}

		initWindowParams();
	}

	public void setOrientationChanged() {
		isOrienChanged = true;
	}

	private void initWindowParams() {
		WindowManager.LayoutParams params = getWindow().getAttributes();

		Point point = DisplayHelper.getScreenSize(getContext());
		params.width = point.x;
		params.height = point.y;
		getWindow().setAttributes(params);
	}

	public void onConfigChange() {
		initWindowParams();

		//很奇怪如果不重新用代码设置，imageview将充不满父控件
		FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) showImageView.getLayoutParams();
		params.width = FrameLayout.LayoutParams.MATCH_PARENT;
		params.height = FrameLayout.LayoutParams.MATCH_PARENT;

		//cropView和cropInforView都是根据屏幕宽高计算的move区域，
		//orientation改变之后一定要重新初始化参数（主要是屏幕宽高）
		cropView.initParams();
		cropInforView.init();

		fitImageView();
	}

	public void fitImageView() {
		if (bitmap != null) {
			Matrix matrix = new Matrix();
			WindowManager.LayoutParams attr = getWindow().getAttributes();
			int maxWidth = attr.width;
			int maxHeight = attr.height;//initWindowParams has already calculated dialog height
			int imageWidth = bitmap.getWidth();
			int imageHeight = bitmap.getHeight();

			float factor = -1;
			//1. check image scale > dialog scale
			/*********
			 * if configuration is portrait(width < height), first check height then width
			 * if configuration is landscape(width > height), first check width then height
			 * in this condition, it can make sure that image will not over dialog screen
			 */
			if (maxWidth < maxHeight) {
				if (imageHeight > maxHeight) {
					factor = (float)maxHeight/(float)imageHeight;
					imageHeight = (int) (((float) imageHeight) * factor);
					imageWidth = (int) (((float) imageWidth) * factor);
					matrix.postScale(factor, factor);
				}
				if (imageWidth > maxWidth) {
					factor = (float)maxWidth/(float)imageWidth;
					imageHeight = (int) (((float) imageHeight) * factor);
					imageWidth = (int) (((float) imageWidth) * factor);
					matrix.postScale(factor, factor);
				}
			}
			else {
				if (imageWidth > maxWidth) {
					factor = (float)maxWidth/(float)imageWidth;
					imageHeight = (int) (((float) imageHeight) * factor);
					imageWidth = (int) (((float) imageWidth) * factor);
					matrix.postScale(factor, factor);
				}
				if (imageHeight > maxHeight) {
					factor = (float)maxHeight/(float)imageHeight;
					imageHeight = (int) (((float) imageHeight) * factor);
					imageWidth = (int) (((float) imageWidth) * factor);
					matrix.postScale(factor, factor);
				}
			}

			//2. check image scale < dialog scale
			if (factor == -1) {
				int spaceY = getContext().getResources().getDimensionPixelOffset(R.dimen.show_image_dlg_space_y);
				int spaceX = getContext().getResources().getDimensionPixelOffset(R.dimen.show_image_dlg_space_x);
				if (maxWidth < maxHeight) {
					if (imageHeight + 2*spaceY < maxHeight) {
						factor = (float)(maxHeight - 2 * spaceY) / (float)imageHeight;
						imageWidth = (int) (((float) imageWidth) * factor);
						imageHeight = maxHeight - 2 * spaceY;
						matrix.postScale(factor, factor);
					}
					//经y方向放大后，检查x方向是否超过屏幕，超过则再缩小
					if (imageWidth > maxWidth){
						factor = (float)(maxWidth - 2 * spaceX) / (float)imageWidth;
						imageHeight = (int) (((float) imageHeight) * factor);
						imageWidth = maxWidth - 2 * spaceX;
						matrix.postScale(factor, factor);
					}
				}
				else {
					if (imageWidth + 2*spaceX < maxWidth) {
						factor = (float)(maxWidth - 2 * spaceX) / (float)imageWidth;
						imageHeight = (int) (((float) imageHeight) * factor);
						imageWidth = maxWidth - 2 * spaceX;
						matrix.postScale(factor, factor);
					}
					//经x方向放大后，检查y方向是否超过屏幕，超过则再缩小
					if (imageHeight > maxHeight) {
						factor = (float)(maxHeight - 2 * spaceY) / (float)imageHeight;
						imageWidth = (int) (((float) imageWidth) * factor);
						imageHeight = maxHeight - 2 * spaceY;
						matrix.postScale(factor, factor);
					}
				}
			}

			//3. set image center
			matrix.postTranslate(maxWidth/2 - imageWidth/2, maxHeight/2 - imageHeight/2);
			showImageView.setImageMatrix(matrix);
		}
	}

	/**
	 * 当imagePath=null, actionListener不为null时，提供给外部设置图片的接口
	 * @param bitmap
	 */
	public void setImage(Bitmap bitmap) {

		this.bitmap = bitmap;
		showImageView.setVisibility(View.VISIBLE);
		if (bitmap != null) {
			showImageView.setImageBitmap(bitmap);
			fitImageView();
		}
		else {
			showImageView.setImageResource(R.drawable.ic_launcher);
		}
	}

	@Override
	public void show() {
		if (isOrienChanged) {
			onConfigChange();
			isOrienChanged = false;
		}
		super.show();
	}

	@Override
	public void onClick(View v) {
		if (v == closeButton) {
			dismiss();
		}
		else if (v == seizeButton) {
			cropActionLayout.setVisibility(View.VISIBLE);
			cropActionbar.setVisibility(View.VISIBLE);
			actionbar.setVisibility(View.GONE);
			saveButton.setVisibility(View.GONE);

			WindowManager.LayoutParams params = getWindow().getAttributes();
			cropView.setCropArea(params.width/2 - 200, params.height/2 - 200, params.width/2 + 200, params.height/2 + 200);
			cropView.setVisibility(View.VISIBLE);
			cropInforView.setInfor(params.width/2 - 200, params.height/2 - 200, params.width/2 + 200, params.height/2 + 200);
			cropInforView.setArea(20, 20, 500, 260);
			cropInforView.setVisibility(View.VISIBLE);
			showImageView.setImageBitmap(bitmap);
		}
		else if (v == saveButton) {
			saveCropBitmap();
		}
		else if (v == doneButton) {
			cropBitmap();
			saveButton.setVisibility(View.VISIBLE);
		}
		else if (v == cancelButton) {
			closeCropMode();
			if (cropBitmap != null) {
				showImageView.setImageBitmap(cropBitmap);
			}
			else {
				showImageView.setImageBitmap(bitmap);
			}
			saveButton.setVisibility(View.VISIBLE);
		}
		else if (v == cropFullScreenButton) {

			WindowManager.LayoutParams params = getWindow().getAttributes();
			setCropArea(0 - cropView.getOffset(), 0 - cropView.getOffset()
					, params.width + cropView.getOffset(), params.height + cropView.getOffset());

		}
		else if (v == cropAreaSizeButton) {
			showCropAreaSizePopup();
		}
		else if (v == undoButton) {
			if (cropBitmap != null) {
				showImageView.setImageBitmap(bitmap);
				cropBitmap.recycle();
				cropBitmap = null;
			}
		}
	}

	private void setCropArea(int left, int top, int right, int bottom) {
		//先gone再visible才起作用
		cropView.setVisibility(View.GONE);
		cropView.setCropArea(left, top, right, bottom);
		onChange(left, top, right, bottom);
		cropView.setVisibility(View.VISIBLE);
	}
	private void setCropArea(int width, int height) {
		//先gone再visible才起作用
		cropView.setVisibility(View.GONE);
		cropView.setCropArea(width, height);
		onChange(width, height);
		cropView.setVisibility(View.VISIBLE);
	}

	private void setCropAreaCenter(int width, int height) {
		//先gone再visible才起作用
		WindowManager.LayoutParams params = getWindow().getAttributes();
		cropView.setVisibility(View.GONE);
		cropView.setCropArea(params.width/2 - width/2, params.height/2 - height/2, params.width/2 + width/2, params.height/2 + height/2);
		onChange(params.width/2 - width/2, params.height/2 - height/2, params.width/2 + width/2, params.height/2 + height/2);
		cropView.setVisibility(View.VISIBLE);
	}


	private void showCropAreaSizePopup() {
		if (cropAreaSizePopup == null) {
			cropAreaSizePopup = new ListPopupWindow(getContext());
			cropAreaSizePopup.setAnchorView(cropAreaSizeButton);
			cropAreaSizePopup.setWidth(600);
			//cropAreaSizePopup.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.shape_slidingmenuitem_bk_pressed));
			cropAreaSizeArray = getContext().getResources().getStringArray(R.array.crop_area_size);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext()
					, android.R.layout.simple_dropdown_item_1line, cropAreaSizeArray);
			cropAreaSizePopup.setAdapter(adapter);
			cropAreaSizePopup.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
										int position, long arg3) {
					String area = cropAreaSizeArray[position];
					String[] array = area.split("\\*");
					int width = Integer.parseInt(array[0]);
					int height = Integer.parseInt(array[1]);
					if (getContext().getResources().getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
						int temp = width;
						width = height;
						height = temp;
					}
					//setCropArea(width, height);
					setCropAreaCenter(width, height);

					//Toast.makeText(getContext(), R.string.success, Toast.LENGTH_LONG).show();
					cropAreaSizePopup.dismiss();
				}
			});
		}
		cropAreaSizePopup.show();
		cropAreaSizePopup.getListView().setDivider(null);//getListView只有在show之后才不为null
	}

	private void closeCropMode() {
		actionbar.setVisibility(View.VISIBLE);
		cropView.setVisibility(View.GONE);
		cropActionLayout.setVisibility(View.GONE);
		cropInforView.setVisibility(View.GONE);
		cropActionbar.setVisibility(View.GONE);
	}

	private void saveCropBitmap() {
		new SaveAsDialog(getContext(), "" + System.currentTimeMillis(),
				new OnVideoDialogListener() {

					@Override
					public void onOk(Object object) {
						String path = (String) object;
						Bitmap saveImage = bitmap;
						if (cropBitmap != null) {
							saveImage = cropBitmap;
						}
						new SaveImageTask(saveImage, path).execute();
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

	private class SaveImageTask extends AsyncTask<Void, Boolean, Void> {

		private String path;
		private Bitmap saveImage;
		public SaveImageTask(Bitmap saveImage, String path) {
			this.path = path;
			this.saveImage = saveImage;
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			boolean result = CropHelper.saveBitmap(saveImage, path + Configuration.DEFAULT_IMAGE_EXTRA);
			publishProgress(result);
			return null;
		}

		@Override
		protected void onProgressUpdate(Boolean... values) {
			if (values[0]) {
				Toast.makeText(getContext(), R.string.video_detail_thumb_save_success, Toast.LENGTH_LONG).show();
			}
			super.onProgressUpdate(values);
		}

	}

	private void cropBitmap() {
		float[] dValues = new float[9];
		showImageView.getImageMatrix().getValues(dValues);
		Rect targetRect = CropHelper.computeRectArea(dValues, cropView.getCutPosition());

		cropBitmap = CropHelper.crop(bitmap, targetRect);

		//v5.9.1 fix v5.9 bug, crop area over image, FC occur
		if (cropBitmap != null) {
			closeCropMode();

			showImageView.setImageBitmap(cropBitmap);
		}
		else {
			//Toast.makeText(getContext(), R.string.crop_area_error, Toast.LENGTH_LONG).show();
		}
	}

	//To fix: showImageDialog>click setasslidingmenubk icon>popup listwindow
	//>back>show image dialog again>click setasslidingmenubk, there is no action
	@Override
	public void dismiss() {

		if (zoomPopup != null && zoomPopup.isShowing()) {
			zoomPopup.dismiss();
		}

		if (bitmap != null) {
			bitmap.recycle();
			bitmap = null;
		}
		//v5.9.1 fix v5.9 bug, 1.should close crop mode. 2.cropBitmap must be null when open dialog
		closeCropMode();
		if (cropBitmap != null) {
			cropBitmap.recycle();
			cropBitmap = null;
		}
		super.dismiss();
	}

	public interface ActionListener {
		public void onAddToOrder();
		public void onMoveToFolder();
		public void onDetails();
		public void onSetCover();
		public void onSetAsMenuBk();
	}

	@Override
	public void onChange(int left, int top, int right, int bottom) {
		cropInforView.setInfor(left, top, right - left, bottom - top);
		cropInforView.invalidate();
	}

	@Override
	public void onChange(int width, int height) {
		cropInforView.setInfor(width, height);
		cropInforView.invalidate();
	}

}
