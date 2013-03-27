package org.canthack.tris.oyver;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class Settings extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	private static final String OYVER_SETTING_SERVER = "server";
	private EditTextPreference mServerPref;

	//Settings
	public static String getServerAddress(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getString(OYVER_SETTING_SERVER, context.getResources().getString(R.string.default_hostname));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		mServerPref = (EditTextPreference) getPreferenceScreen().findPreference(OYVER_SETTING_SERVER);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals(OYVER_SETTING_SERVER)) {
			mServerPref.setSummary(sharedPreferences.getString(key, "")); 
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		mServerPref.setSummary(mServerPref.getText());
		
		// Set up a listener whenever a setting changes            
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();

		// Unregister the listener whenever a setting changes            
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);    
	}


}