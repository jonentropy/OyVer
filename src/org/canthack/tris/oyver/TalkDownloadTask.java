package org.canthack.tris.oyver;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;

import org.canthack.tris.oyver.model.json.ListTalksResponse;
import org.canthack.tris.oyver.model.json.Talk;
import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.Gson;

public class TalkDownloadTask extends AsyncTask<String, Integer, ListTalksResponse>{

	private Context mContext; // reference to the calling Activity
	int progress = -1;
	private ListTalksResponse downloadedTalks = null;
	private ArrayList<Integer> talkIds = new ArrayList<Integer>();
	
	String lastError;
	private String SESSIONS_FILENAME = "sessions.json";
	private static final String TAG = "OyVer DownloadTask";

	TalkDownloadTask(Context context) {
		mContext = context;
	}

	public boolean downloadedOk(){
		return downloadedTalks != null;
	}
	// Called from main thread to re-attach
	protected void setContext(Context context) {
		mContext = context;
		if(progress >= 0) {
			publishProgress(this.progress);
		}
	}

	protected void onPreExecute() {
		progress = 0;
		Spinner talkSpinner = (Spinner) ((Activity) mContext).findViewById(R.id.spinner1);
		talkSpinner.setVisibility(View.INVISIBLE);	
	}

	protected ListTalksResponse doInBackground(String ... location) {
		Log.v(TAG, "doing download of talks...");
		return downloadTalks(location[0]);
	}

	protected void onProgressUpdate(Integer... progress) {	
		ProgressBar pBar = (ProgressBar)
				((Activity) mContext).findViewById(R.id.progressBar1);

		if(progress[0] > 0 && progress[0] < 100){
			pBar.setVisibility(View.VISIBLE);
			pBar.setProgress(progress[0]);
		}
		else{
			pBar.setVisibility(View.INVISIBLE);	
		}
	}

	protected void onPostExecute(ListTalksResponse result) {
		Spinner talkSpinner = (Spinner) ((Activity) mContext).findViewById(R.id.spinner1);

		if(result != null) {
			downloadedTalks = result;
			talkSpinner.setVisibility(View.VISIBLE);
			
			populateTalks(talkSpinner);
		}
		else {
			Toast.makeText(mContext.getApplicationContext(), mContext.getString(R.string.error_downloading_talks), Toast.LENGTH_SHORT).show();
			talkSpinner.setVisibility(View.INVISIBLE);
		}
	}
	
	public void populateTalks(Spinner s){
		if(downloadedTalks == null || downloadedTalks.talks == null)
			return;

		Log.d(TAG, "Populating");
		
		talkIds.clear();

		ArrayList<String> talkNames = new ArrayList<String>();

		for(Talk t : downloadedTalks.talks){
			talkNames.add(t.title);
			talkIds.add(t.id);
		}

		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_dropdown_item, talkNames);	
		spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); 
		spinnerArrayAdapter.insert(mContext.getString(R.string.select_session), 0);
		s.setAdapter(spinnerArrayAdapter);
	}

	public ListTalksResponse downloadTalks(String location)
	{
		setProgress(1);
		ListTalksResponse response = null;
		
		try{
			final InputStream source = CustomHTTPClient.retrieveStream(location);
			final Reader reader = new InputStreamReader(source);
			final BufferedReader bir = new BufferedReader(reader);

			final Gson gson = new Gson();
			response = gson.fromJson(bir, ListTalksResponse.class);

			//Save sessions ready for offline use
			String jsonRepresentation = gson.toJson(response);
					 
			FileOutputStream fos = mContext.openFileOutput(SESSIONS_FILENAME, Context.MODE_PRIVATE);
			fos.write(jsonRepresentation.getBytes());
			fos.flush();
			fos.close();
			
			bir.close();				
		}
		catch (Exception e) {
			e.printStackTrace();
			lastError = e.getMessage() + ".";
		}
		
		if(lastError != null){
			//error occurred, try offline JSON
			final Gson gson = new Gson();
			try {
				response = gson.fromJson(new BufferedReader(new InputStreamReader(mContext.openFileInput(SESSIONS_FILENAME))), ListTalksResponse.class);
				if(response.talks == null) throw new JSONException("No talks available.");
				lastError = null;
			} catch (Exception e){
				setProgress(-1);
				lastError = e.getMessage() + ".";
				return null;
			}
		}

		setProgress(100);
		return response;
	}
	
	public ArrayList<Integer> getTalkIds(){
		return talkIds;
	}

	private void setProgress(int progress) {
		this.progress = progress;
		publishProgress(this.progress);
	}
	
	public ListTalksResponse getTalks(){
		return downloadedTalks;
	}
}