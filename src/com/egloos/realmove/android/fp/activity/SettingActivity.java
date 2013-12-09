
package com.egloos.realmove.android.fp.activity;

import com.egloos.realmove.android.fp.R;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class SettingActivity extends PreferenceActivity {

	private static final String TAG = SettingActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Display the fragment as the main content.
		getFragmentManager().beginTransaction().replace(android.R.id.content,
				new PrefsFragment()).commit();

		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
	}

	public static class PrefsFragment extends PreferenceFragment {

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.preferences);
		}
	}
}
