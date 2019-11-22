package com.punuo.sys.app;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.alibaba.android.arouter.launcher.ARouter;
import com.alibaba.fastjson.JSON;
import com.leplay.petwight.PetWeight;
import com.punuo.sys.app.RotationControl.TurnAndStop;
import com.punuo.sys.app.bluetooth.BluetoothChatService;
import com.punuo.sys.app.bluetooth.Constants;
import com.punuo.sys.app.bluetooth.PTOMessage;
import com.punuo.sys.app.led.LedControl;
import com.punuo.sys.app.led.LedData;
import com.punuo.sys.app.process.ProcessTasks;
import com.punuo.sys.app.weighing.tool.WeightReset;
import com.punuo.sys.app.wifi.OnServerWifiListener;
import com.punuo.sys.app.wifi.WifiController;
import com.punuo.sys.app.wifi.WifiMessage;
import com.punuo.sys.app.wifi.WifiUtil;
import com.punuo.sys.sdk.PnApplication;
import com.punuo.sys.sdk.activity.BaseActivity;
import com.punuo.sys.sdk.util.ToastUtils;
import com.punuo.sys.sip.HeartBeatHelper;
import com.punuo.sys.sip.SipDevManager;
import com.punuo.sys.sip.event.ReRegisterEvent;
import com.punuo.sys.sip.model.LoginResponse;
import com.punuo.sys.sip.request.SipGetDevSeedRequest;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by han.chen.
 * Date on 2019-08-17.
 **/
public class BluetoothActivity extends BaseActivity {
    private static final String TAG = "BluetoothActivity";
    public static final int MSG_HEART_BEAR_VALUE = 10086;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothChatService mBluetoothChatService;
    private WifiManager mWifiManager;
    private boolean isFirst = true;
    private boolean run = true;
    private Button mWeight;
    private Button mReset;
    TurnAndStop turn = new TurnAndStop();
    private PetWeight petWeight;
    private LedControl ledControl;
    private IntentFilter intentFilter;
    private NetworkChangeReceiver networkChangeReceiver;
    private FeedButtonReceiver feedButtonReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ProcessTasks.commonLaunchTasks(PnApplication.getInstance());
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothChatService = BluetoothChatService.getInstance(this, mBaseHandler);
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        ledControl=new LedControl();
        intentFilter=new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        intentFilter.addAction("android.petrobot.action.FEED_KEY");
        networkChangeReceiver=new NetworkChangeReceiver();
        registerReceiver(networkChangeReceiver,intentFilter);
        feedButtonReceiver = new FeedButtonReceiver();
        registerReceiver(feedButtonReceiver,intentFilter);

        Button setting = findViewById(R.id.setting);
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startActivity(new Intent(Settings.ACTION_SETTINGS));
                ARouter.getInstance().build("/sip/video_preview").navigation();
            }
        });
        findViewById(R.id.turn_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                run = true;
                new Thread() {
                    @Override
                    public void run() {
//                        while (run) {
                           // PetControl.petRight();


                        turn.turnRight();
//                        }
                    }
                }.start();
//                mBaseHandler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        run = false;
//                    }
//                },10 * 1000);
//                PetControl.petRight();
            }
        });
        findViewById(R.id.stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    @Override
                    public void run() {
//                        PetControl.petStop();
                        turn.turnStop();
                    }

                }.start();
//                PetControl.petStop();
            }
        });
        EventBus.getDefault().register(this);

        mWeight = findViewById(R.id.weight);
        mWeight.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(BluetoothActivity.this,WeighingActivity.class);
//                startActivity(intent);
            }
        });

//        getQuality();
//        mTimer = new Timer();
//        setTimerTask();

        findViewById(R.id.reset).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                WeightReset weightReset = new WeightReset();
                weightReset.reset();
            }
        }
        );

    }

    private void init() {
        Log.i(TAG, "蓝牙状态 = " + mBluetoothAdapter.isEnabled());
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
        mBluetoothChatService.start();
        if (!WifiUtil.isWifiEnable(mWifiManager)) {
            mWifiManager.setWifiEnabled(true);
        }
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case Constants.MESSAGE_STATE_CHANGE:
                switch (msg.arg1) {
                    case BluetoothChatService.STATE_CONNECTED:
                        Log.i(TAG, "与该设备已建立连接");
                        break;
                    case BluetoothChatService.STATE_CONNECTING:
                        Log.i(TAG, "正在建立连接....");
                        break;
                    case BluetoothChatService.STATE_LISTEN:
                    case BluetoothChatService.STATE_NONE:
                        break;
                    default:
                        break;
                }
                break;
            case Constants.MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                String readMessage = new String(readBuf, 0, msg.arg1);
                PTOMessage ptoMessage = JSON.parseObject(readMessage, PTOMessage.class);
                if (ptoMessage != null) {
                    if (ptoMessage.data != null) {
                        Log.i(TAG, readMessage);
                        serverConnectWifi(JSON.parseObject(ptoMessage.data, WifiMessage.class));
                    } else {
                        // ToastUtil.showShort(ptoMessage.getMsg());
                    }
                }
                break;
            case Constants.MESSAGE_TOAST:
                break;
            case MSG_HEART_BEAR_VALUE:
                HeartBeatHelper.heartBeat();
                mBaseHandler.sendEmptyMessageDelayed(MSG_HEART_BEAR_VALUE, HeartBeatHelper.DELAY);
                break;
            default:
                break;
        }
    }

    private int dis = 1;
    private int con = 1;

    private void serverConnectWifi(final WifiMessage wifiMessage) {
        dis = 1;
        con = 1;
        if (TextUtils.equals(WifiUtil.getConnectWifiBssid(), wifiMessage.BSSID)) {
            PTOMessage ptoMsg = new PTOMessage();
            ptoMsg.type = Constants.SERVER;
            ptoMsg.msg = "对接设备已连接该网络";
            mBluetoothChatService.write(JSON.toJSONString(ptoMsg).getBytes());
            return;
        }
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiController wifiController = new WifiController(this, wifiManager);
        wifiController.setServerWifiListener(new OnServerWifiListener() {
            @Override
            public void OnServerConnected(WifiInfo wifiInfo, WifiMessage wifiMessage) {
                mBaseHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        String msg = "";
                        PTOMessage ptoMessage = new PTOMessage();
                        if (con == 1) {
                            msg = "开始连接";
                            con--;
                        } else {
                            msg = "对接设备WiFi连接成功";
                            con = 1;
                        }
                        ptoMessage.type = Constants.SUCCESS;
                        ptoMessage.msg = msg;
                        mBluetoothChatService.write(JSON.toJSONString(wifiMessage).getBytes());
                        dis = 1;
                    }
                }, 400);
                //TODO 注册sip
                registerDev();
            }

            @Override
            public void OnServerConnecting() {

            }

            @Override
            public void OnServerDisConnect() {
                mBaseHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (dis == 1) {
                            dis--;
                            PTOMessage ptoMessage = new PTOMessage();
                            ptoMessage.type = Constants.SUCCESS;
                            ptoMessage.msg = "正在断开WiFi";
                            mBluetoothChatService.write(JSON.toJSONString(ptoMessage).getBytes());
                        }
                    }
                }, 100);
            }
        });
        wifiController.connectWifi(wifiMessage);
    }

    private void registerDev() {
        SipGetDevSeedRequest getDevSeedRequest = new SipGetDevSeedRequest();
        SipDevManager.getInstance().addRequest(getDevSeedRequest);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ReRegisterEvent event) {
        mBaseHandler.removeMessages(MSG_HEART_BEAR_VALUE);
        registerDev();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(LoginResponse event) {
        //sip登陆注册成功 开启心跳保活
        if (!mBaseHandler.hasMessages(MSG_HEART_BEAR_VALUE)) {
            mBaseHandler.sendEmptyMessageDelayed(MSG_HEART_BEAR_VALUE, HeartBeatHelper.DELAY);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(LedData ledData) {
        if(ledData.k==1){
            spark();
        }
        }

    class NetworkChangeReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context,Intent intent){
            ConnectivityManager connectivityManager=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo=connectivityManager.getActiveNetworkInfo();
            if(networkInfo!=null&&networkInfo.isAvailable()){
                ledControl.turnOnCustom1Light();
            }else {
                Toast.makeText(BluetoothActivity.this,"networkchange",Toast.LENGTH_SHORT).show();
                ledControl.turnOffCustom1Light();
            }
        }
    }
    private int clo=0;
    public void spark(){
        Timer timer=new Timer();
        ConnectivityManager connectivityManager=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo=connectivityManager.getActiveNetworkInfo();
        TimerTask taskcc=new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        if (clo == 0) {
                            clo = 1;
                            ledControl.turnOnCustom1Light();
                        } else {
                            if (clo == 1) {
                                clo = 0;
                                if(networkInfo!=null&&networkInfo.isAvailable()){
                                    ledControl.turnOnCustom1Light();
                                }
                                else{ ledControl.turnOffCustom1Light();}
                            }
                        }
                    }
                });
            }
        };
        timer.schedule(taskcc, 1, 300);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void feedNow(String feedCount){
        turn.turnRight();
        //TODO 还未完善，需要根据服务器返回的喂食份数来确定旋转多长时间
    }

    class FeedButtonReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            ToastUtils.showToast("开始喂食啦！ ");
            turn.turnRight();
            //TODO 需要对云台旋转进行时间控制，尚未完成
        }
    }



    @Override
    protected void onResume() {
        super.onResume();
        if (isFirst) {
            init();
            registerDev();
            isFirst = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        unregisterReceiver(networkChangeReceiver);
    }
}
