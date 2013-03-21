package org.canthack.tris.oyver;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;

import org.canthack.tris.oyver.OyVerMain.GuiMode;
import org.canthack.tris.oyver.model.json.ListTalksResponse;
import org.canthack.tris.oyver.model.json.Talk;

import com.google.gson.Gson;

import android.os.Bundle;
import android.os.StrictMode;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Resources.Theme;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class OyVerMain extends Activity {

	enum GuiMode {SELECT_SESSION, VOTE_SESSION};
	
	private ArrayList<Talk> talks = new ArrayList<Talk>();
	private GuiMode guimode = GuiMode.SELECT_SESSION;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		//Don't include this in real code! Background it instead.
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy); 
		//******************
	
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_oyver_main);
		setGuiMode(guimode);
		populateTalks();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.oy_ver_main, menu);
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		setGuiMode(guimode);
	}

	@SuppressLint("NewApi")
	private void setGuiMode(GuiMode mode){
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
		setGuiMode(GuiMode.VOTE_SESSION);
	}
	
	private void populateTalks(){
		final InputStream source = CustomHTTPClient.retrieveStream(ListTalksResponse.getUrl());

		if(source == null){
			return;
		}

		final Reader reader = new InputStreamReader(source);
		final BufferedReader bir = new BufferedReader(reader);

		final Gson gson = new Gson();
		final ListTalksResponse response = gson.fromJson(bir, ListTalksResponse.class);

		if(response == null){
			return;
		}
		else{
			
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
	}

}
