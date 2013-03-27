package org.canthack.tris.oyver;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

public class OyVerMain extends Activity implements OnSharedPreferenceChangeListener {
	private static final String TAG = "OyVer Main";

	private boolean fullscreen = false;

	private TalkDownloadTask talkDLTask;

	private int selectedTalkId;
	private String selectedTalkTitle;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_oyver_main);

		if( (talkDLTask = (TalkDownloadTask)getLastNonConfigurationInstance()) != null) {
			talkDLTask.setContext(this); 
			if(talkDLTask.getStatus() == AsyncTask.Status.FINISHED)
				talkDLTask.populateTalks((Spinner)this.findViewById(R.id.spinner1));
		}
		else{
			downloadTalks();
		}

		Spinner talkSpinner = (Spinner) this.findViewById(R.id.spinner1);
		talkSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int i, long l) {
				if(i > 0){ //0 is for the "select session" line. Also explains -1s below :-)

					selectedTalkId = talkDLTask.getTalkIds().get(i-1);
					selectedTalkTitle = talkDLTask.getTalks().talks.get(i-1).title;

					Log.v(TAG, "SELECTED " + i + "." + l + "." + selectedTalkId);
					Log.v(TAG, "SEL: " + selectedTalkTitle);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});

		if (savedInstanceState != null){
			fullscreen = savedInstanceState.getBoolean("guimode");
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle bund) {
		super.onSaveInstanceState(bund);
		bund.putBoolean("guimode", fullscreen);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.oy_ver_main, menu);
		return true;
	}

	//Handles menu clicks
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.action_refresh:
			downloadTalks();
			return true;
		case R.id.action_settings:
			startActivity(new Intent(this, Settings.class));
			return true;
		default:
			break;

		}
		return false;
	}

	@Override
	protected void onResume() {
		super.onResume();
		setGuiMode();

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		preferences.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause(){
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		preferences.registerOnSharedPreferenceChangeListener(this);
		super.onPause();
	}

	@SuppressLint("NewApi")
	private void setGuiMode(){
		Log.d(TAG, "Setting GUI Mode: " + fullscreen);

		View main_layout = this.findViewById(android.R.id.content).getRootView();

		if(fullscreen){
			main_layout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
			getActionBar().hide();
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);		
		}
		else{
			getActionBar().show();
			this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			main_layout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);	
		}
	}

	public void switchModes(View v){
		fullscreen = !fullscreen;
		setGuiMode();

		//	talkDLTask.populateTalks((Spinner)this.findViewById(R.id.spinner1));
	}

	private void downloadTalks(){
		if(talkDLTask != null) {
			AsyncTask.Status diStatus = talkDLTask.getStatus();

			if(diStatus != AsyncTask.Status.FINISHED) {
				Log.v(TAG, "Talks already downloading.");
				return;
			}
			// Since diStatus must be FINISHED, we can try again.
		}

		ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

		boolean isConnected = !(activeNetwork == null) && activeNetwork.isConnectedOrConnecting();

		if(!isConnected){
			Toast.makeText(getApplicationContext(), this.getString(R.string.no_internet), Toast.LENGTH_LONG).show();
		}
		else{
			talkDLTask = new TalkDownloadTask(this);

			try{
				talkDLTask.execute(Settings.getServerAddress(this));
			}
			catch(Exception e){
				e.printStackTrace();
				//err.setText(e.getMessage());
			}
		}
	}

	// This gets called before onDestroy(). We want to pass forward a reference
	// to our AsyncTask.
	@Override
	public Object onRetainNonConfigurationInstance() {
		return talkDLTask;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		if (key.equals(Settings.OYVER_SETTING_SERVER)) {
			downloadTalks();
		}
	}

}
