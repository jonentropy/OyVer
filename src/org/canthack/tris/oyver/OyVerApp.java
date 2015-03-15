package org.canthack.tris.oyver;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import android.app.Application;

public class OyVerApp extends Application {
	public Queue<Vote> votes = new LinkedBlockingQueue<Vote>();
	
	static Voter voter;
	private static Thread voterThread;
	
	@Override
	public void onCreate() {
		voter = new Voter(this, this);
		voterThread = new Thread(null, voter, "Voter");
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
