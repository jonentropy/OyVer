package org.canthack.tris.oyver;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

public class VoteSummaryActivity extends ListActivity {
	private ListView lv;
	private Queue<Vote> votes;
    private final Map<String, VoteSummary> voteSummary = new HashMap<>();
	
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
		lv = getListView();

		//for when the queue changes..
		LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
				new IntentFilter("voteQueueUpdated"));

        setAdapter();
	}


	private void setAdapter() {
		//Visualise the voting summary for the list view.

        voteSummary.clear();

        for (final Vote v: votes) {
            final String name = v.getTalkName();
            VoteSummary summary = voteSummary.get(name);
            if (summary == null) {
                summary = new VoteSummary();
                voteSummary.put(name, summary);
            }

            //Increment the appropriate vote.
            switch (v.getVoteType()) {
                case Vote.YAY:
                    summary.yay.incrementAndGet();
                    break;
                case Vote.NAY:
                    summary.nay.incrementAndGet();
                    break;
                case Vote.MEH:
                    summary.meh.incrementAndGet();
                    break;
            }
        }

        final List<String> toDisplay = new ArrayList<>(voteSummary.size());
        for (String talk: voteSummary.keySet()) {
            final VoteSummary sum = voteSummary.get(talk);
            if (sum == null) continue;
            toDisplay.add(talk + "\n :) = " + sum.yay.get() + "   :| = " + sum.meh.get() + "   :( = " + sum.nay.get());
        }

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, toDisplay);
		lv.setAdapter(arrayAdapter);
	}

	@Override
	public void onDestroy(){
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
		super.onDestroy();
	}

    private class VoteSummary {
        AtomicInteger yay = new AtomicInteger(0);
        AtomicInteger meh = new AtomicInteger(0);
        AtomicInteger nay = new AtomicInteger(0);
    }
}
