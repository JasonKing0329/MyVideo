package com.king.app.video;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.widget.EditText;
import android.widget.LinearLayout;

public class SimpleDialogManager {

	public interface OnDialogActionListener {
		public void onOk(String name);
		public void onDismiss();
	}
	
	public void openTextInputDialog(Context context, String title, String preText
			, final OnDialogActionListener listener) {
		LinearLayout layout = new LinearLayout(context);
		layout.setPadding(40, 10, 40, 10);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		EditText edit = new EditText(context);
		edit.setLayoutParams(params);
		if (preText != null) {
			edit.setText(preText);
		}
		layout.addView(edit);
		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		dialog.setTitle(title);
		dialog.setView(layout);
		
		final EditText folderEdit = edit;
		dialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String filename = folderEdit.getText().toString();
				listener.onOk(filename);
			}
		});
		dialog.setNegativeButton(R.string.cancel, null);
		dialog.show();
	}
	
	public void openWarningDialog(Context context, int stringId, boolean hasCancel
			, final OnDialogActionListener listener) {

		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		dialog.setTitle(R.string.video_dlg_warning);
		dialog.setMessage(context.getResources().getString(stringId));
		dialog.setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss(DialogInterface arg0) {
				if (listener != null) {
					listener.onDismiss();
				}
			}
		});
		dialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (listener != null) {
					listener.onOk(null);
				}
			}
		});
		if (hasCancel) {
			dialog.setNegativeButton(R.string.cancel, null);
		}
		dialog.show();
	}
}
