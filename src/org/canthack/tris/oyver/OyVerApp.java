package org.canthack.tris.oyver;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import android.app.Application;

public class OyVerApp extends Application {
	public Queue<Vote> votes = new LinkedBlockingQueue<Vote>();
}
