package com.punuo.sys.app.feed;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.punuo.sys.sdk.util.ToastUtils;

import org.greenrobot.eventbus.EventBus;

public class FeedBroadcast extends BroadcastReceiver {

    private FeedData feedData = new FeedData();
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        ToastUtils.showToast("开始喂食啦！");
        if ("android.petrobot.action.FEED_KEY".equals(action)) {
            if (intent.getExtras() != null) {
                boolean down = intent.getExtras().getBoolean("down");
                if (down) {
                    EventBus.getDefault().post(feedData);
                }
            }
        } else if ("com.punuo.sys.app.FEED".equals(action)) {
            EventBus.getDefault().post(feedData);
        }
    }
}
