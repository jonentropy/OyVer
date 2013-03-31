package org.canthack.tris.oyver;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Deque;

import android.util.Log;

public class Voter implements Runnable {

	public static final int MAX_ATTEMPTS = 10;
	
	private Deque<Vote> votes = new ArrayDeque<Vote>();
	private boolean running = true;

	private static final String TAG = "OyVer Voter";
	
	public void queueVote(Vote v) {
		Log.v(TAG, "Adding vote (" + v.getUrl() + ")");
		votes.add(v);
	}
	
	public void stop(){
		Log.v(TAG, "Stopping");
		running = false;
	}

	@Override
	public void run() {
		Log.v(TAG, "Starting");
		
		while(running){
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if(!votes.isEmpty()){
				if(sendVote(votes.peek())){
					Log.v(TAG, "Sending vote from thread");
					votes.pop();
				}
			}
		}
	}

	private boolean sendVote(Vote v) {
		
		InputStream s = CustomHTTPClient.retrieveStream(v.getUrl());	
		if(s == null) return false;
		
		try {
			s.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;	
	}
}
