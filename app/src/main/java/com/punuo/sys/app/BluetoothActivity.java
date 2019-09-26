package com.punuo.sys.app;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.alibaba.android.arouter.launcher.ARouter;
import com.alibaba.fastjson.JSON;
import com.leplay.petwight.PetWeight;
import com.punuo.sys.app.Weighing.WeighingActivity;
import com.punuo.sys.app.bluetooth.BluetoothChatService;
import com.punuo.sys.app.bluetooth.Constants;
import com.punuo.sys.app.bluetooth.PTOMessage;
import com.punuo.sys.app.process.ProcessTasks;
import com.punuo.sys.app.wifi.OnServerWifiListener;
import com.punuo.sys.app.wifi.WifiController;
import com.punuo.sys.app.wifi.WifiMessage;
import com.punuo.sys.app.wifi.WifiUtil;
import com.punuo.sys.sdk.PnApplication;
import com.punuo.sys.sdk.activity.BaseActivity;
import com.punuo.sys.sdk.util.HandlerExceptionUtils;
import com.punuo.sys.sip.HeartBeatHelper;
import com.punuo.sys.sip.SipDevManager;
import com.punuo.sys.sip.event.ReRegisterEvent;
import com.punuo.sys.sip.model.RegisterData;
import com.punuo.sys.sip.request.SipDevRegisterRequest;
import com.punuo.sys.sip.request.SipGetDevSeedRequest;
import com.punuo.sys.sip.request.SipRequestListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by han.chen.
 * Date on 2019-08-17.
 **/
public class BluetoothActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "BluetoothActivity";
    public static final int MSG_HEART_BEAR_VALUE = 10086;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothChatService mBluetoothChatService;
    private WifiManager mWifiManager;
    private boolean isFirst = true;
    private Button mWeight;
    PetWeight mPeight;
    Timer mTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ProcessTasks.commonLaunchTasks(PnApplication.getInstance());
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothChatService = BluetoothChatService.getInstance(this, mBaseHandler);
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        Button setting = findViewById(R.id.setting);
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startActivity(new Intent(Settings.ACTION_SETTINGS));
                ARouter.getInstance().build("/sip/video_preview").navigation();
            }
        });
        EventBus.getDefault().register(this);

        mWeight = findViewById(R.id.weight);
        mWeight.setOnClickListener(this);

//        getQuality();
        mTimer = new Timer();
        setTimerTask();
    }

    /**
     * 以下两个方法用来测试称重精度，测试完成后再重新修改
     */
    public void setTimerTask(){
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                getQuality();
            }
        },0,1000*5);
    }

    public void getQuality(){
        mPeight = new PetWeight();
        mPeight.getWeight();
        Log.i(TAG, "getQuality: "+mPeight.getWeight());
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
        if (WifiUtil.getConnectWifiBssid().equals(wifiMessage.BSSID)) {
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
        getDevSeedRequest.setSipRequestListener(new SipRequestListener<RegisterData>() {
            @Override
            public void onComplete() {

            }

            @Override
            public void onSuccess(RegisterData result) {
                if (result == null) {
                    return;
                }
                sipDevRegister(result);
            }

            @Override
            public void onError(Exception e) {
                HandlerExceptionUtils.handleException(e);
            }
        });
        SipDevManager.getInstance().addRequest(getDevSeedRequest);
    }

    private void sipDevRegister(RegisterData data) {
        SipDevRegisterRequest registerRequest = new SipDevRegisterRequest(data);
        registerRequest.setSipRequestListener(new SipRequestListener<Object>() {
            @Override
            public void onComplete() {

            }

            @Override
            public void onSuccess(Object result) {
                //sip登陆注册成功 开启心跳保活
                if (!mBaseHandler.hasMessages(MSG_HEART_BEAR_VALUE)) {
                    mBaseHandler.sendEmptyMessage(MSG_HEART_BEAR_VALUE);
                }
            }

            @Override
            public void onError(Exception e) {
                HandlerExceptionUtils.handleException(e);
            }
        });
        SipDevManager.getInstance().addRequest(registerRequest);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ReRegisterEvent event) {
        mBaseHandler.removeMessages(MSG_HEART_BEAR_VALUE);
        registerDev();
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

        //关闭计时器
        if (mTimer != null){
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.weight:
//                Intent intent = new Intent(BluetoothActivity.this, WeighingActivity.class);
//                startActivity(intent);
        }
    }
}
