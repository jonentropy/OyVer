package org.canthack.tris.oyver;


import java.util.ArrayList;

import org.canthack.tris.oyver.model.json.ListTalksResponse;
import org.canthack.tris.oyver.model.json.Talk;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.AsyncTask;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class OyVerMain extends Activity {

	enum GuiMode {SELECT_SESSION, VOTE_SESSION};

	private ArrayList<Talk> talks = new ArrayList<Talk>();
	private GuiMode guimode = GuiMode.SELECT_SESSION;

	private TalkDownloadTask talkDLTask;

	private static final String TAG = "OyVer Main";

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_oyver_main);

		if( (talkDLTask = (TalkDownloadTask)getLastNonConfigurationInstance()) != null) {
			talkDLTask.setContext(this); 
			if(talkDLTask.getStatus() == AsyncTask.Status.FINISHED)
				populateTalks(talkDLTask.getTalks());
		}
		else{
			downloadTalks();
		}

		setGuiMode(guimode);
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
			downloadTalks();//TODO fix
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
		setGuiMode(guimode);
	}

	@SuppressLint("NewApi")
	private void setGuiMode(GuiMode mode){
		Log.d(TAG, "Setting GUI Mode: " + mode);
		guimode = mode;

		View main_layout = this.findViewById(android.R.id.content).getRootView();

		switch(mode){
		case SELECT_SESSION: 
			getActionBar().show();
			this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			main_layout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
			break;

		case VOTE_SESSION:
			main_layout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
			getActionBar().hide();
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);  
			break;

		default:
			break;
		}
	}

	public void switchModes(View v){
		//setGuiMode(GuiMode.VOTE_SESSION);
		//TODO
		populateTalks(talkDLTask.getTalks());
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

	public void populateTalks(ListTalksResponse response){
		if(response == null)
			return;

		Log.d(TAG, "Populating");

		talks.clear();
		talks = response.talks;

		ArrayList<String> talkNames = new ArrayList<String>();

		for(Talk t : talks){
			talkNames.add(t.title);
		}

		Spinner s = (Spinner) this.findViewById(R.id.spinner1);
		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, talkNames);	
		spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); 
		s.setAdapter(spinnerArrayAdapter);
	}


	// This gets called before onDestroy(). We want to pass forward a reference
	// to our AsyncTask.
	@Override
	public Object onRetainNonConfigurationInstance() {
		return talkDLTask;
	}

}
