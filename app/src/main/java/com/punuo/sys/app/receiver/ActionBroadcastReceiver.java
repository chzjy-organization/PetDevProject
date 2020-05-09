package com.punuo.sys.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.punuo.sys.app.event.LedDataEvent;
import com.punuo.sys.app.event.VolumeChangedEvent;
import com.punuo.sys.app.event.FeedEvent;

import org.greenrobot.eventbus.EventBus;

public class ActionBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (TextUtils.equals(action, "android.petrobot.action.WIFIBT_KEY")) {
            EventBus.getDefault().post(new LedDataEvent());
        } else if (TextUtils.equals(action, "android.media.VOLUME_CHANGED_ACTION")){
            EventBus.getDefault().post(new VolumeChangedEvent());
        } else if ("android.petrobot.action.FEED_KEY".equals(action)) {
            if (intent.getExtras() != null) {
                boolean down = intent.getExtras().getBoolean("down");
                if (down) {
                    EventBus.getDefault().post(new FeedEvent());
                }
            }
        }
    }
}
