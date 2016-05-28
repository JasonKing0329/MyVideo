package com.king.app.video.setting;

import com.king.app.video.Application;
import com.king.app.video.R;

import android.app.Activity;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.TextView;

public class SettingActivity extends Activity implements OnClickListener {

	private TextView backAction;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.video_setting);
		backAction = (TextView) findViewById(R.id.video_action_back);
		backAction.setOnClickListener(this);
		if (Application.isLollipop()) {
			backAction.setBackgroundResource(R.drawable.item_background_borderless_material);
		}

		// getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingFragment()).commit();
		//通过观察源码看出android.R.id.content代表的是LinearLayout(属于ViewGroup)
		getFragmentManager().beginTransaction().replace(R.id.video_setting_container, new SettingFragment()).commit();
	}
	@Override
	public void onClick(View view) {
		if (view == backAction) {
			finish();
		}
	}

	public static class SettingFragment extends PreferenceFragment implements OnPreferenceChangeListener
			, OnPreferenceClickListener{

//		private CheckBoxPreference accessModeBox, enablePageBox, enableSlidingBox, enableFingerBox;
//		private EditTextPreference pageNumberPreference, casualLookPreference, minItemPreference;
//		private ListPreference speedList, autoplayModeList, orderShowList
//			, colNumPref, horColNumPref, slidingMenuPref, cascadeNumberPref;
//		private static Toast fpNotSupportToast;

		private ListPreference dispPlayPref, sortPref;
		//private Preference clearHistoryPref, backupPref, importPref;

		@Override
		public void onCreate(Bundle savedInstanceState) {

			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.video_setting);

			dispPlayPref = (ListPreference) findPreference("video_setting_display_mode");
			sortPref = (ListPreference) findPreference("video_setting_default_sort");
			dispPlayPref.setOnPreferenceChangeListener(this);
			sortPref.setOnPreferenceChangeListener(this);
//			backupPref.setOnPreferenceClickListener(this);
//			importPref.setOnPreferenceClickListener(this);

			initPreferenceData();
		}

		private void initPreferenceData() {
			dispPlayPref.setSummary(SettingProperties.getDisplayMode(getActivity()));
			sortPref.setSummary(SettingProperties.getSortType(getActivity()));
		}

		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			if (preference == dispPlayPref) {
				dispPlayPref.setSummary(newValue.toString());
			}
			else if (preference == sortPref) {
				sortPref.setSummary(newValue.toString());
			}
			return true;
		}

		@Override
		public boolean onPreferenceClick(Preference preference) {
			return true;
		}
	}

}
