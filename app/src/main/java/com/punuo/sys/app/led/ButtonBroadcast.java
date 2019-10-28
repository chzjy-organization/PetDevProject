package com.punuo.sys.app.led;


import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Method;


public class ButtonBroadcast extends BroadcastReceiver {
LedData ledData=new LedData();
    @Override
    public void onReceive(Context context, Intent intent) {
        EventBus.getDefault().post(ledData);
        Toast.makeText(context,"bluetooth",Toast.LENGTH_SHORT).show();
    }
}
