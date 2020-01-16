package com.punuo.sys.app.feed.plan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

public class FeedPlanBroadcast extends BroadcastReceiver {
    private FeedPlanData feedPlanData;
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String sCount = intent.getStringExtra("count");
        Log.i("plan", "收到intent的count "+sCount);
        int count = Integer.parseInt(sCount);
        Log.i("plan", ""+count);
        feedPlanData = new FeedPlanData(count);
        if ("com.punuo.sys.app.FEED".equals(action)){
            EventBus.getDefault().post(feedPlanData);
        }
    }
}
