package org.canthack.tris.oyver;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class Settings extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	public static final String OYVER_SETTING_SERVER = "server";
	public static final String OYVER_SETTING_VOTING = "voting";
	
	private EditTextPreference mServerPref;
	private EditTextPreference mVotingPref;

	//Settings
	public static String getServerAddress(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getString(OYVER_SETTING_SERVER, context.getResources().getString(R.string.default_hostname));
	}
	
	public static String getVotingServerAddress(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getString(OYVER_SETTING_VOTING, context.getResources().getString(R.string.default_hostname));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		mServerPref = (EditTextPreference) getPreferenceScreen().findPreference(OYVER_SETTING_SERVER);
		mVotingPref = (EditTextPreference) getPreferenceScreen().findPreference(OYVER_SETTING_VOTING);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs,
			String key) {
		if (key.equals(OYVER_SETTING_SERVER)) {
			mServerPref.setSummary(prefs.getString(key, "")); 
		}
		else if (key.equals(OYVER_SETTING_VOTING)) {
			mVotingPref.setSummary(prefs.getString(key, "")); 
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		mServerPref.setSummary(mServerPref.getText());
		mVotingPref.setSummary(mVotingPref.getText());
		
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