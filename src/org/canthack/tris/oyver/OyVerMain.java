package org.canthack.tris.oyver;

import java.util.ArrayList;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.animation.ObjectAnimator;
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
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class OyVerMain extends Activity implements OnSharedPreferenceChangeListener {
	private static final String TAG = "OyVer Main";

	private boolean fullscreen = false;

	private TalkDownloadTask talkDLTask;
	private Voter voter = new Voter();

	private int selectedTalkId = -1;
	private String selectedTalkTitle = "";

	private Thread voterThread;

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
		final Button goButton = (Button) this.findViewById(R.id.go_button);
				
		talkSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int i, long l) {
				
				if(i > 0){ //0 is for the "select session" line. Also explains -1s below :-)

					selectedTalkId = talkDLTask.getTalkIds().get(i-1);
					selectedTalkTitle = talkDLTask.getTalks().talks.get(i-1).title;
					
					goButton.setEnabled(true);

					Log.v(TAG, "SELECTED " + i + "." + l + "." + selectedTalkId);
					Log.v(TAG, "SEL: " + selectedTalkTitle);
				}
				else{
					selectedTalkId = -1;
					selectedTalkTitle = "";
					goButton.setEnabled(false);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});

		if (savedInstanceState != null){
			selectedTalkId = savedInstanceState.getInt("selectedtalk");
			selectedTalkTitle = savedInstanceState.getString("selectedtalkname");
			fullscreen = savedInstanceState.getBoolean("guimode");
		}
		
		voterThread = new Thread(null, voter, "Voter");
		voterThread.start();
	}

	@Override
	protected void onSaveInstanceState(Bundle bund) {
		super.onSaveInstanceState(bund);
		bund.putBoolean("guimode", fullscreen);
		bund.putInt("selectedtalk", selectedTalkId);
		bund.putString("selectedtalkname", selectedTalkTitle);
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

		//views that are only to be displayed in fullscreen mode
		ArrayList<View> fsViews = new ArrayList<View>();

		fsViews.add(this.findViewById(R.id.yay_button));
		fsViews.add(this.findViewById(R.id.meh_button));
		fsViews.add(this.findViewById(R.id.nay_button));

		//views that are only to be displayed in non fullscreen mode
		ArrayList<View> normalViews = new ArrayList<View>();

		normalViews.add(this.findViewById(R.id.textView1));
		normalViews.add(this.findViewById(R.id.spinner1));
		normalViews.add(this.findViewById(R.id.go_button));

		if(fullscreen){
			for(View v: normalViews) v.setVisibility(View.GONE);

			main_layout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
			getActionBar().hide();
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);	

			for(View v: fsViews) v.setVisibility(View.VISIBLE);
		}
		else{
			for(View v: fsViews) v.setVisibility(View.GONE);

			getActionBar().show();
			this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			main_layout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);	

			for(View v: normalViews) v.setVisibility(View.VISIBLE);
		}
	}

	public void startVoting(View v){
		if(selectedTalkId >= 0){
			fullscreen = true;
			setGuiMode();
		}
	}

	public void voteButtonClick(View v){
		Log.v(TAG, "voting on " + selectedTalkId);
		if(selectedTalkId < 0)
			return;
				
		ObjectAnimator animator = ObjectAnimator.ofFloat(v, "alpha", 0.2f, 1f);

		animator.setDuration(300);
		animator.start();	
		
		Vote vote = null; 
		
		switch(v.getId()){
		case R.id.yay_button:
			vote = new Vote(Settings.getVotingServerAddress(this), selectedTalkId, Vote.YAY);
			break;

		case R.id.meh_button:
			vote = new Vote(Settings.getVotingServerAddress(this), selectedTalkId, Vote.MEH);
			break;

		case R.id.nay_button:
			vote = new Vote(Settings.getVotingServerAddress(this), selectedTalkId, Vote.NAY);
			break;
		}
		
		voter.queueVote(vote);
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
			Toast.makeText(getApplicationContext(), this.getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
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

	@Override
	public void onBackPressed() {
		if(fullscreen){
			fullscreen=false;
			setGuiMode();
		}
		else{
			super.onBackPressed();
		}
	}

}
