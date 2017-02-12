package org.canthack.tris.oyver;

import android.app.Application;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class OyVerApp extends Application {
	public Queue<Vote> votes = new LinkedBlockingQueue<>();

	//TODO fix this. Could be leak?
	static Voter voter;

	@Override
	public void onCreate() {
		voter = new Voter(this, this);
		Thread voterThread = new Thread(null, voter, "Voter");
		voterThread.start();	
	}

	@Override
	public void onLowMemory() {
		voter.serialiseNow();
		super.onLowMemory();
	}

	@Override
	public void onTerminate() {
		voter.stop();
		super.onTerminate();
	}
}
