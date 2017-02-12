package org.canthack.tris.oyver;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.concurrent.LinkedBlockingQueue;

class Voter implements Runnable {
	private static final String TAG = "OyVer Voter";
	private static final String SERIAL_PREF = "votes";
	private static final String EMPTY_JSON_ARRAY = "[]";

	private Gson gson = new Gson();
	private Context appCtx;
	private boolean running = true;
	private OyVerApp app;
	private volatile long lastSerialisedTime;
	private static final long SERIALISE_PERIOD = 10L * 1000L * 1000L * 1000L; //10 seconds.

	Voter(Context c, OyVerApp app){
		this.appCtx = c.getApplicationContext();		
		this.app = app;
		deserialiseVotes();
	}

	void queueVote(Vote v) {
		app.votes.add(v);
		notifyVoteQueueChanged();
	}

	void stop(){
		Log.v(TAG, "Voter Stopping");
		running = false;
	}

	private void deserialiseVotes(){
		synchronized(Voter.class){
			Log.v(TAG, "DeSerialising votes");
			
			String votes = PreferenceManager.getDefaultSharedPreferences(appCtx).getString(SERIAL_PREF, EMPTY_JSON_ARRAY);	
			Type voteQueueType = new TypeToken<LinkedBlockingQueue<Vote>>(){}.getType();
			app.votes = gson.fromJson(votes, voteQueueType);
		}
	}

	@Override
	public void run() {
		Log.v(TAG, "Voter Run Starting");

		while(running){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if(!app.votes.isEmpty()){
				ConnectivityManager cm = (ConnectivityManager)appCtx.getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

				boolean isConnected = !(activeNetwork == null) && activeNetwork.isConnectedOrConnecting();

				if(isConnected){
					Vote v = app.votes.peek();
					if(sendVote(v)){
						Log.d(TAG, "Vote successful: " + v);
						app.votes.remove(v);
						notifyVoteQueueChanged();
					}		
				}
			}
			
			//periodically serialise votes just in case, even if online...
			if(System.nanoTime() > lastSerialisedTime + SERIALISE_PERIOD){
				serialiseVotes();
				lastSerialisedTime = System.nanoTime();
			}
		}
		//stopped, serialise what we haven't sent yet
		serialiseVotes();
	}

	private void notifyVoteQueueChanged() {
		Log.d("Voter", "Broadcasting changed message");
		Intent intent = new Intent("voteQueueUpdated");
		LocalBroadcastManager.getInstance(appCtx).sendBroadcast(intent);
	}

	private void serialiseVotes(){
		synchronized(Voter.class){
			Log.v(TAG, "Serialising votes");
		
			String votes = gson.toJson(app.votes);
			PreferenceManager.getDefaultSharedPreferences(appCtx).edit().putString(SERIAL_PREF, votes).apply();			
		}
	}

	private boolean sendVote(Vote v) {
		if(v == null) return false;

		InputStream s = CustomHTTPClient.retrieveStream(v.getUrl());	
		if(s == null) return false;

		try {
			s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;	
	}
	
	void serialiseNow() {
		lastSerialisedTime = System.nanoTime() - SERIALISE_PERIOD - 1;
	}
	
}
