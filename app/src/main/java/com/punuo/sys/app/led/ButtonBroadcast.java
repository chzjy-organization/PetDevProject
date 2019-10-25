package com.punuo.sys.app.led;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import org.greenrobot.eventbus.EventBus;


public class ButtonBroadcast extends BroadcastReceiver {
LedData ledData=new LedData();
private BluetoothAdapter mBluetooth;

    @Override
    public void onReceive(Context context, Intent intent) {
        mBluetooth=BluetoothAdapter.getDefaultAdapter();
        EventBus.getDefault().post(ledData);
        if(!mBluetooth.isEnabled()){
            mBluetooth.enable();
            Toast.makeText(context,"蓝牙打开",Toast.LENGTH_SHORT).show();
        }
        if(mBluetooth.isEnabled()){
            mBluetooth.disable();
            Toast.makeText(context,"蓝牙关闭",Toast.LENGTH_SHORT).show();
        }
    }
}
