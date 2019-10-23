package com.punuo.sys.app.feed;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.punuo.sys.app.RotationControl.TurnAndStop;
import com.punuo.sys.sdk.util.ToastUtils;

import org.greenrobot.eventbus.EventBus;

public class FeedBroadcast extends BroadcastReceiver {

    private FeedData feedData = new FeedData();
    @Override
    public void onReceive(Context context, Intent intent) {
        ToastUtils.showToast("开始喂食啦！");
        EventBus.getDefault().post(feedData);
    }
}
