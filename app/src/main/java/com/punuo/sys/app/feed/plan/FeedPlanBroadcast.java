package com.punuo.sys.app.feed.plan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.greenrobot.eventbus.EventBus;

public class FeedPlanBroadcast extends BroadcastReceiver {
    private FeedPlanData feedPlanData;
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        //TODO 需要获取到intent中的count
        feedPlanData = new FeedPlanData(6);
        if ("com.punuo.sys.app.FEED".equals(action)){
            EventBus.getDefault().post(feedPlanData);
        }
    }
}
