package com.punuo.sys.app.led;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;



public class ButtonBroadcast extends BroadcastReceiver {
LedData ledData=new LedData();
    @Override
    public void onReceive(Context context, Intent intent) {
        EventBus.getDefault().post(ledData);
    }
}
