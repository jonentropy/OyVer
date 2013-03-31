package org.canthack.tris.oyver;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayDeque;
import java.util.Deque;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

public class Voter implements Runnable {
	private static final String TAG = "OyVer Voter";
	private static final String SERIAL_FILENAME = "votes";
	public static final int MAX_ATTEMPTS = 10;

	private Context ctx = null;

	private Deque<Vote> votes;
	private boolean running = true;

	public Voter(Context c){
		this.ctx = c;		
		synchronized(Voter.class){
			votes = new ArrayDeque<Vote>();
		}
		
		deserialiseVotes();
	}

	public void queueVote(Vote v) {
		synchronized(Voter.class){
			votes.add(v);
		}
	}

	public void stop(){
		Log.v(TAG, "Stopping");
		running = false;
	}

	public void setContext(Context c){
		this.ctx = c;
	}

	private void deserialiseVotes(){
		synchronized(Voter.class){
			boolean found = false;
			for(String s: ctx.fileList()){
				if(s.equals(SERIAL_FILENAME)){
					found = true;
					break;
				}
			}
			if(found){
				Log.v(TAG, "DeSerialising votes");

				FileInputStream fis;
				try {
					fis = ctx.openFileInput(SERIAL_FILENAME);
				} catch (FileNotFoundException e1) {
					Log.v(TAG, "Couldnt find deserialise file.");
					return;
				}

				try {
					ObjectInputStream os = new ObjectInputStream(fis);
					try {
						votes = (Deque<Vote>) os.readObject();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
						Log.v(TAG, "Couldnt deserialise file.");
					}

					os.close();
					ctx.deleteFile(SERIAL_FILENAME);

				} catch (IOException e) {
					e.printStackTrace();
					Log.v(TAG, "Couldnt deserialise file.");
				}
			}
		}
	}

	@Override
	public void run() {
		Log.v(TAG, "Starting");

		while(running){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			synchronized(Voter.class){
				if(!votes.isEmpty()){
					ConnectivityManager cm = (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
					NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

					boolean isConnected = !(activeNetwork == null) && activeNetwork.isConnectedOrConnecting();

					if(isConnected){
						if(sendVote(votes.peek())){
							Log.v(TAG, "SENDING " + votes.peek().getUrl());
							votes.pop();
						}
						else{
							Log.v(TAG, "Incrementing attempt counter");
							votes.peek().incrementAttempts();
						}			
					}

					if(!votes.isEmpty() && votes.peek().getNumberOfAttempts() > Voter.MAX_ATTEMPTS){
						Log.v(TAG, "MAX ATTEMPTS REACHED");
						((Activity)ctx).runOnUiThread(new Runnable() {
							public void run() {
								Toast.makeText(((Activity)ctx), ctx.getString(R.string.voting_problem), Toast.LENGTH_SHORT).show();
							}
						});

						votes.pop();
					}
				}
			}
		}
		//stopped, serialise what we haven't sent yet
		serialiseVotes();
	}

	private void serialiseVotes(){
		synchronized(Voter.class){
			if(!votes.isEmpty()){
				Log.v(TAG, "Serialising votes");

				FileOutputStream fos = null;
				try { 
					ctx.deleteFile(SERIAL_FILENAME);
					fos = ctx.openFileOutput(SERIAL_FILENAME, Context.MODE_PRIVATE);
				} catch (FileNotFoundException e) {
					Log.v(TAG, "Could not serialise votes");
					e.printStackTrace();
					return;
				}

				try {
					ObjectOutputStream os = new ObjectOutputStream(fos);
					os.writeObject(votes);
					os.flush();
					os.close();

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else{
				ctx.deleteFile(SERIAL_FILENAME);
			}
		}
	}

	private boolean sendVote(Vote v) {
		InputStream s = CustomHTTPClient.retrieveStream(v.getUrl());	
		if(s == null) return false;

		try {
			s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;	
	}
}
