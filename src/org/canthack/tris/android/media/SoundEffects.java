package org.canthack.tris.android.media;

import android.content.Context;
import android.media.MediaPlayer;

public class SoundEffects {
	private static MediaPlayer mp = null;

	public static void playEffect(Context context, int resource) {
		playSound(context, resource, false);
	}

	public static void playMusic(Context context, int resource) {
		playSound(context, resource, true);
	}

	public static void stop() {
		if (mp != null) {
			mp.stop();
			mp.release();
			mp = null;
		}
	}

	private static void playSound(Context context, int resource, boolean loop) {
		stop();
		mp = MediaPlayer.create(context, resource);	
		mp.setLooping(loop);
		mp.start();	
	}
}