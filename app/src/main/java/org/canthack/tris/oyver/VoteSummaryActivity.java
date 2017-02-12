package org.canthack.tris.oyver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.ListView;

import java.util.Queue;

public class VoteSummaryActivity extends Activity {
	private ListView lv;
	private Queue<Vote> votes;
	
	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("Votes summary changed", "Received event");
			runOnUiThread(new Runnable(){
				@Override
				public void run() {
					setAdapter();
				}
			});
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_vote_summary);

		votes = ((OyVerApp)getApplication()).votes;
		lv = (ListView) findViewById(R.id.list);

		//for when the queue changes..
		LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
				new IntentFilter("voteQueueUpdated"));
	}


	private void setAdapter() {
		//TODO enumerate and populate list view
//		final Vote[] voteArray = ((OyVerApp)getApplication()).votes.toArray(new Vote[0]);
//        final ArrayAdapter<Vote> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, voteArray);
//		lv.setAdapter(arrayAdapter);
	}

	@Override
	public void onDestroy(){
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
		super.onDestroy();
	}
}
