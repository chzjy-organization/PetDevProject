/*
 *  UVCCamera
 *  library and sample to access to UVC web camera on non-rooted Android device
 *
 * Copyright (c) 2014-2017 saki t_saki@serenegiant.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 *  All files in the folder are under this Apache License, Version 2.0.
 *  Files in the libjpeg-turbo, libusb, libuvc, rapidjson folder
 *  may have a different license, see the respective files.
 */

package com.punuo.sys.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Message;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;

import com.alibaba.fastjson.JSON;
import com.leplay.petwight.PetWeight;
import com.punuo.stream.NativeStreamer;
import com.punuo.sys.app.RotationControl.TurnAndStop;
import com.punuo.sys.app.bluetooth.BluetoothChatService;
import com.punuo.sys.app.bluetooth.Constants;
import com.punuo.sys.app.bluetooth.PTOMessage;
import com.punuo.sys.app.camera.UVCCameraHelper;
import com.punuo.sys.app.detection.MotionDetector;
import com.punuo.sys.app.detection.MotionDetectorCallback;
import com.punuo.sys.app.feed.FeedData;
import com.punuo.sys.app.feed.plan.FeedAlarmManager;
import com.punuo.sys.app.feed.plan.FeedPlanData;
import com.punuo.sys.app.led.LedControl;
import com.punuo.sys.app.led.LedData;
import com.punuo.sys.app.process.ProcessTasks;
import com.punuo.sys.app.weighing.requset.GetGroupMemberRequest;
import com.punuo.sys.app.weighing.requset.SaveOutedRequest;
import com.punuo.sys.app.weighing.requset.SipGetWeightRequest;
import com.punuo.sys.app.weighing.requset.WeightDataToServerRequest;
import com.punuo.sys.app.weighing.tool.GroupMemberModel;
import com.punuo.sys.app.wifi.OnServerWifiListener;
import com.punuo.sys.app.wifi.WifiAdmin;
import com.punuo.sys.app.wifi.WifiController;
import com.punuo.sys.app.wifi.WifiMessage;
import com.punuo.sys.app.wifi.WifiUtil;
import com.punuo.sys.sdk.PnApplication;
import com.punuo.sys.sdk.activity.BaseActivity;
import com.punuo.sys.sdk.httplib.HttpManager;
import com.punuo.sys.sdk.httplib.RequestListener;
import com.punuo.sys.sdk.httplib.upload.UploadPictureManager;
import com.punuo.sys.sdk.model.BaseModel;
import com.punuo.sys.sdk.util.BaseHandler;
import com.punuo.sys.sdk.util.CommonUtil;
import com.punuo.sys.sdk.util.ToastUtils;
import com.punuo.sys.sip.HeartBeatHelper;
import com.punuo.sys.sip.SipDevManager;
import com.punuo.sys.sip.config.SipConfig;
import com.punuo.sys.sip.event.ReRegisterEvent;
import com.punuo.sys.sip.model.FeedNotifyData;
import com.punuo.sys.sip.model.FeedPlan;
import com.punuo.sys.sip.model.LoginResponse;
import com.punuo.sys.sip.model.MusicData;
import com.punuo.sys.sip.model.RecvaddrData;
import com.punuo.sys.sip.model.ResetData;
import com.punuo.sys.sip.model.VideoData;
import com.punuo.sys.sip.model.VolumeData;
import com.punuo.sys.sip.model.WiFiData;
import com.punuo.sys.sip.request.SipGetDevSeedRequest;
import com.punuo.sys.sip.video.H264Config;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.common.AbstractUVCCameraHandler;
import com.serenegiant.usb.encoder.RecordParams;
import com.serenegiant.usb.widget.CameraViewInterface;
import com.serenegiant.usb.widget.UVCCameraTextureView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class HomeActivity extends BaseActivity implements CameraDialog.CameraDialogParent,
        BaseHandler.MessageHandler, CameraViewInterface.Callback {
    private static final boolean DEBUG = true;
    private static final String TAG = "HomeActivity";
    public static final int MSG_HEART_BEAR_VALUE = 10086;
    private NativeStreamer mNativeStreamer;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothChatService mBluetoothChatService;
    private WifiManager mWifiManager;
    private TurnAndStop turn = new TurnAndStop();
    private LedControl ledControl;
    private NetworkChangeReceiver networkChangeReceiver;
    private BaseHandler mBaseHandler;
    private PetWeight mPetWeight;
    private MotionDetector mMotionDetector; //移动侦测
    private MediaPlayer mMediaPlayer;
    private static String quality;

    private UVCCameraHelper mCameraHelper;
    private UVCCameraTextureView mUVCCameraView;

    private boolean isRequest;
    private boolean isPreview;
    private boolean started = false;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        ProcessTasks.commonLaunchTasks(PnApplication.getInstance());

        mBaseHandler = new BaseHandler(this);
        mUVCCameraView = findViewById(R.id.camera_surface_view);
        initSurfaceViewSize();
        mUVCCameraView.setCallback(this);

        mCameraHelper = UVCCameraHelper.getInstance();
        mCameraHelper.setDefaultFrameFormat(UVCCameraHelper.FRAME_FORMAT_MJPEG);
        mCameraHelper.initUSBMonitor(this, mUVCCameraView, mConnectListener);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothChatService = BluetoothChatService.getInstance(this, mBaseHandler);
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        ledControl = new LedControl();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        networkChangeReceiver = new NetworkChangeReceiver();
        registerReceiver(networkChangeReceiver, intentFilter);
        mNativeStreamer = new NativeStreamer();
        initDetection();
        mCameraHelper.setOnPreviewFrameListener(data -> {
            if (started && isPreview && mNativeStreamer != null && rtmpOpenResult != -1) {
                mNativeStreamer.onPreviewFrame(data, H264Config.VIDEO_WIDTH, H264Config.VIDEO_HEIGHT);
            }
            if (mMotionDetector != null) {
                mMotionDetector.consume(data, H264Config.VIDEO_WIDTH, H264Config.VIDEO_HEIGHT);
            }
        });

        findViewById(R.id.turn_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    @Override
                    public void run() {
                        turn.turnRight();
                    }
                }.start();
            }
        });
        findViewById(R.id.stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    @Override
                    public void run() {
                        turn.turnStop();
                    }
                }.start();
            }
        });

        findViewById(R.id.weight).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /**
                 * 测试设备初始重量
                 */
                int sum = 0;
                int one_quality;//单次称重
                ArrayList<Integer> arrayList = new ArrayList<>();
                mPetWeight = new PetWeight();
                for (int i = 0;i<100;i++){
                    one_quality = mPetWeight.getWeight();
                    arrayList.add(one_quality);
                }
                Collections.sort(arrayList);
                arrayList.subList(0,10).clear();
                arrayList.subList(arrayList.size()-10,arrayList.size()).clear();
                Log.i("weight", ""+arrayList.size());
                for (int i =0;i<arrayList.size();i++){
                    sum += arrayList.get(i);
                }
                Log.i("weight", "平均值为"+sum/80);

            }
        });
        findViewById(R.id.reset).setOnClickListener(view -> recordMovie());
        //截屏操作
        findViewById(R.id.capture).setOnClickListener(view -> capturePicture());

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                //设置一个第二天零点的闹钟
                setZeroClock();
                Log.i("plan", "开始设置0点闹钟");

            }
        }, 0, 24 * 60 * 60 * 1000);

        EventBus.getDefault().register(this);
    }

    private UVCCameraHelper.OnMyDevConnectListener mConnectListener = new UVCCameraHelper.OnMyDevConnectListener() {
        @Override
        public void onAttachDev(UsbDevice device) {
            // request open permission
            if (!isRequest) {
                isRequest = true;
                if (mCameraHelper != null) {
                    mCameraHelper.requestPermission(0);
                }
            }
        }

        @Override
        public void onDettachDev(UsbDevice device) {
            // close camera
            if (isRequest) {
                isRequest = false;
                mCameraHelper.closeCamera();
                ToastUtils.showToast(device.getDeviceName() + " is out");
            }
        }

        @Override
        public void onConnectDev(UsbDevice device, boolean isConnected) {
            if (!isConnected) {
                isPreview = false;
            } else {
                isPreview = true;
                ToastUtils.showToast("connecting");
            }
        }

        @Override
        public void onDisConnectDev(UsbDevice device) {

        }
    };

    private long lastDetectorTime = 0; // 上一次检测到移动的时间

    /**
     * 初始化移动侦测
     */
    private void initDetection() {
        mMotionDetector = new MotionDetector(this);
        mMotionDetector.setMotionDetectorCallback(new MotionDetectorCallback() {
            @Override
            public void onMotionDetected(byte[] bytes) {
                Log.i(TAG, "onMotionDetected: 监测到移动");
                long nowTime = System.currentTimeMillis();
                if (nowTime - lastDetectorTime > 5 * 60 * 1000) {
                    lastDetectorTime = nowTime;
                    //视频不行的话就用图像吧。
//                    capturePicture();//捕捉图像
                    recordMovie(); //捕捉视频
                }
            }

            @Override
            public void onTooDark() {
                Log.i(TAG, "onMotionDetected: 光线太暗无法检测");
            }
        });
    }

    /**
     * 截屏
     */
    private void capturePicture() {
        if(mCameraHelper != null) {
            String picPath = UVCCameraHelper.ROOT_PATH +"DCIM/"+ System.currentTimeMillis() + UVCCameraHelper.SUFFIX_JPEG;
            mCameraHelper.capturePicture(picPath, path -> {
                UploadPictureManager.getInstance().uploadPicture(picPath, SipConfig.getDevId());
            });
        }
    }

    private void recordMovie() {
        String videoPath = UVCCameraHelper.ROOT_PATH +"Movies/"+ System.currentTimeMillis() + UVCCameraHelper.SUFFIX_MP4;
        if (!mCameraHelper.isPushing()) {
            RecordParams params = new RecordParams();
            params.setRecordPath(videoPath);
            params.setRecordDuration(0);
            params.setVoiceClose(false);
            mCameraHelper.startPusher(params, new AbstractUVCCameraHandler.OnEncodeResultListener() {
                @Override
                public void onEncodeResult(byte[] data, int offset, int length, long timestamp, int type) {
                }

                @Override
                public void onRecordResult(String videoPath) {
                    UploadPictureManager.getInstance().uploadVideo(videoPath, SipConfig.getDevId());
                }
            });
            mBaseHandler.postDelayed(() -> {
                mCameraHelper.stopPusher();
            }, 10 * 1000);
        }
    }

    private AlarmManager alarmManager;

    public void setZeroClock() {
        alarmManager = (AlarmManager) PnApplication.getInstance().getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long time = calendar.getTimeInMillis();

        Intent intent = new Intent();
        intent.setAction("com.punuo.sys.app.SETZEROFEED");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
        Log.i("plan", "0点闹钟设置完成 ");
    }

    public String getQuality() {
        int sum = 0;
        int one_quality;//单次称重
        float fQuality;
        int quality;
        ArrayList<Integer> arrayList = new ArrayList<>();
        mPetWeight = new PetWeight();
        for (int i = 0; i < 100; i++) {
            one_quality = mPetWeight.getWeight();
            arrayList.add(one_quality);
        }
        Collections.sort(arrayList);
        arrayList.subList(0,10).clear();
        arrayList.subList(arrayList.size()-10,arrayList.size()).clear();
        Log.i("weight", ""+arrayList.size());
        for (int i =0;i<arrayList.size();i++){
            sum += arrayList.get(i);
        }
        Log.i("weight", "平均值为"+sum/80);
        if(((sum/80)-1149)>0){
            fQuality = ((sum/80)-1149)*(100/17);
        }else{
            fQuality = -((sum/80)-1149)*(100/17);
        }
        quality = Math.round(fQuality);
        Log.i("weight", "重量为"+quality);
        return String.valueOf(quality);
    }

    /**
     * 根据设备id获取到群组所有的user
     */
    private GetGroupMemberRequest mGetGroupMemberRequest;
    private List<GroupMemberModel.Member> mMembers = new ArrayList<>();

    public void getGroupMember(String quality, String devId) {
        if (mGetGroupMemberRequest != null && !mGetGroupMemberRequest.isFinish()) {
            return;
        }
        mGetGroupMemberRequest = new GetGroupMemberRequest();
        mGetGroupMemberRequest.addUrlParam("devid", devId);
        mGetGroupMemberRequest.setRequestListener(new RequestListener<GroupMemberModel>() {
            @Override
            public void onComplete() {
                Log.i("weight", "getGroupMember: " + mMembers);
            }

            @Override
            public void onSuccess(GroupMemberModel result) {
                if (result == null) {
                    return;
                }
                if (result.members != null && !result.members.isEmpty()) {
                    weightToSipServer(quality, result.members);
                }
            }

            @Override
            public void onError(Exception e) {

            }
        });
        HttpManager.addRequest(mGetGroupMemberRequest);
    }

    //将数据发送到Sip服务器
    public void weightToSipServer(String quality, List<GroupMemberModel.Member> members) {
        SipGetWeightRequest getWeightRequest = new SipGetWeightRequest(quality, members);
        SipDevManager.getInstance().addRequest(getWeightRequest);
        Log.i("weight", "称重信息发送中...... ");
    }

    private void initSurfaceViewSize() {
        int width = CommonUtil.getWidth();
        int height = H264Config.VIDEO_HEIGHT * width / H264Config.VIDEO_WIDTH;
        mUVCCameraView.getLayoutParams().height = height;
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
        ledControl.turnOnCustom2Light();
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null) {
            WifiAdmin wifiAdmin = new WifiAdmin(HomeActivity.this);
            wifiAdmin.openWifi();
            WifiConfiguration wcg = wifiAdmin.CreateWifiInfo("admin", "12345678", 3);
            wifiAdmin.addNetwork(wcg);
        }
    }

    private int rtmpOpenResult = -1; //推流启动是否成功  -1:失败 0: 成功
    private int retryTimes = 0;

    private void encodeStart() {
        if (mNativeStreamer != null) {
            rtmpOpenResult = mNativeStreamer.startPublish(H264Config.RTMP_STREAM,
                    H264Config.VIDEO_WIDTH, H264Config.VIDEO_HEIGHT);
        }
        if (rtmpOpenResult != -1) {
            Log.i(TAG, "encodeStart: 开始推流");
            ToastUtils.showToast("开始推流");
        } else {
            retryTimes++;
            startPushError();
        }
    }

    private void startPushError() {
        if (retryTimes < 5) {
            Log.i(TAG, "encodeStart: 开启推流失败,正在重试,次数: " + retryTimes + " 次");
            mBaseHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    encodeStart();
                }
            }, 1000);
        } else {
            Log.i(TAG, "encodeStart: 失败次数过多");
            //TODO 通知服务器推流失败,让用户尝试
        }
    }

    private void encodeStop() {
        mNativeStreamer.stopPublish();
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) Log.v(TAG, "onDestroy:");
        if (mCameraHelper != null) {
            mCameraHelper.release();
        }
        mUVCCameraView = null;
        EventBus.getDefault().unregister(this);
        unregisterReceiver(networkChangeReceiver);
        super.onDestroy();
    }

    /**
     * to access from CameraDialog
     *
     * @return
     */
    @Override
    public USBMonitor getUSBMonitor() {
        return mCameraHelper.getUSBMonitor();
    }

    @Override
    public void onDialogResult(boolean canceled) {

    }



    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(RecvaddrData event) {
        started = false;
        ToastUtils.showToast("停止推流");
        mBaseHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                encodeStop();
            }
        }, 1000);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(VideoData videoData) {
        if (started) {
            return;
        }
        encodeStart();
        started = true;
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
        if (ledData.k == 1) {
            //setDiscoverableTimeout(10);
            //spark();
            Back();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ResetData result) {
        Back();
    }

    private void Back() {
        super.onBackPressed();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(VolumeData result){
        AudioManager am = (AudioManager) getSystemService(Service.AUDIO_SERVICE);
        if(TextUtils.equals(result.volume,"raise")){
            am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
            am.adjustStreamVolume(AudioManager.STREAM_DTMF, AudioManager.ADJUST_RAISE, 0);
        }
        if(TextUtils.equals(result.volume,"lower")){
            am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
            am.adjustStreamVolume(AudioManager.STREAM_DTMF, AudioManager.ADJUST_LOWER, 0);
        }
    }
    //接收WiFi账号密码连接WiFi
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(WiFiData result) {
        WifiAdmin wifiAdmin = new WifiAdmin(HomeActivity.this);
        wifiAdmin.openWifi();
        WifiConfiguration wcg = wifiAdmin.CreateWifiInfo(result.admin, result.password, 3);
        wifiAdmin.addNetwork(wcg);
        Log.d("HomeActivity", "wifi set success");
    }

    public void setDiscoverableTimeout(int timeout) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        try {
            Method setDiscoverableTimeout = BluetoothAdapter.class.getMethod("setDiscoverableTimeout", int.class);
            setDiscoverableTimeout.setAccessible(true);
            Method setScanMode = BluetoothAdapter.class.getMethod("setScanMode", int.class, int.class);
            setScanMode.setAccessible(true);
            setDiscoverableTimeout.invoke(adapter, timeout);
            setScanMode.invoke(adapter, BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE, timeout);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private int clo = 0;

    public void spark() {
        Timer timer = new Timer();

        TimerTask taskcc = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                        if (clo == 0) {
                            clo = 1;
                            ledControl.turnOnCustom1Light();
                        } else {
                            if (networkInfo != null && networkInfo.isAvailable()) {
                                clo = 0;
                            }
                            if (clo == 1) {
                                clo = 0;
                                ledControl.turnOffCustom1Light();
                            }
                        }
                    }
                });
            }
        };
        timer.schedule(taskcc, 1, 300);
    }

    @Override
    public void onSurfaceCreated(CameraViewInterface view, Surface surface) {
        if (!isPreview && mCameraHelper.isCameraOpened()) {
            mCameraHelper.startPreview(mUVCCameraView);
            isPreview = true;
        }
    }

    @Override
    public void onSurfaceChanged(CameraViewInterface view, Surface surface, int width, int height) {

    }

    @Override
    public void onSurfaceDestroy(CameraViewInterface view, Surface surface) {
        if (isPreview && mCameraHelper.isCameraOpened()) {
            mCameraHelper.stopPreview();
            isPreview = false;
        }
    }

    public class NetworkChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isAvailable()) {
                ledControl.turnOnCustom1Light();
            } else {
                ledControl.turnOffCustom1Light();
            }
            if (!mBaseHandler.hasMessages(MSG_HEART_BEAR_VALUE)) {
                registerDev();
            }
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void feedNow(FeedNotifyData result) {
        ToastUtils.showToast("收到喂食请求");
        Log.i("feed", "feedcount" + result.feedCount);

//        取出收到字符串中的第一个数字(正则)
//        Pattern p = Pattern.compile("\\d+");
//        Matcher m = p.matcher(count);
//        m.find();
//        int currentCount = Integer.parseInt(m.group());
//        String count1 = count.substring(0,1);//取出收到字符串的第一个字符
        int count = Integer.parseInt(result.feedCount);
        Log.i("feed", "currentCount: "+count);
        if(count>0){
            turn.turnRight();
            mBaseHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    turn.turnStop();
                    quality = getQuality();
                    getGroupMember(quality,SipConfig.getDevId());
                    float fOutQuality = Math.round(count*7.5);
                    int outQuality = (int) fOutQuality;
                    weightDataToWeb(SipConfig.getDevId(),String.valueOf(outQuality),quality);
                }
            },count*12*1000);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void feedNowButton(FeedData feedData){
        //默认喂食36s,然后在延时一定时间后进行称重操作。
        turn.turnRight();
        mBaseHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                turn.turnStop();
                quality = getQuality();
                getGroupMember(quality,SipConfig.getDevId());
                int outQuality = (int)(3*7.5)+1;
                weightDataToWeb(SipConfig.getDevId(),String.valueOf(outQuality),quality);
            }
        }, 36* 1000);
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                turn.turnRight();
//                try {
//                    Thread.sleep(5 * 1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                turn.turnStop();
//                quality = getQuality();
//                getGroupMember(quality,SipConfig.getDevId());
//                weightDataToWeb(SipConfig.getDevId(),String.valueOf(3*7.5),quality);
//            }
//        }).start();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(FeedPlan feedPlan) {
        FeedAlarmManager.getInstance().addAlarmTask(this, feedPlan);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(FeedPlanData feedPlanData) {
        turn.turnRight();
        mBaseHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                turn.turnStop();
                quality = getQuality();
                getGroupMember(quality, SipConfig.getDevId());
                saveOutedCount(feedPlanData.mCount);
                int outQuality = (int) Math.round(feedPlanData.mCount*7.5);
                weightDataToWeb(SipConfig.getDevId(),String.valueOf(outQuality),quality);
            }
        },feedPlanData.mCount*12*1000);
    }

    /**
     * 将计划中喂食过后的份数反馈到服务器
     */
    private SaveOutedRequest mSaveOutedRequest;

    public void saveOutedCount(int count) {
        if (mSaveOutedRequest != null && !mSaveOutedRequest.isFinish()) {
            return;
        }
        mSaveOutedRequest = new SaveOutedRequest();
        mSaveOutedRequest.addUrlParam("devid", SipConfig.getDevId());
        mSaveOutedRequest.addUrlParam("amount", count);
        mSaveOutedRequest.setRequestListener(new RequestListener<BaseModel>() {
            @Override
            public void onComplete() {

            }

            @Override
            public void onSuccess(BaseModel result) {
                Log.i(TAG, result.success + result.message);
            }

            @Override
            public void onError(Exception e) {

            }
        });
        HttpManager.addRequest(mSaveOutedRequest);
    }

    /**
     * 接受音乐
     *
     * @param musicData
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MusicData musicData) {
        if (!TextUtils.isEmpty(musicData.musicUrl)) {
            if (mMediaPlayer == null) {
                mMediaPlayer = new MediaPlayer();
            }
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            mMediaPlayer.reset();
            if (TextUtils.equals(musicData.musicUrl, "stop")) {
                return;
            }
            Uri uri = Uri.parse(musicData.musicUrl);
            try {
                mMediaPlayer.setDataSource(this, uri);
                mMediaPlayer.prepare();
                mMediaPlayer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private WeightDataToServerRequest mWeightDataToServerRequest;

    public void weightDataToWeb(String devid, String eatWeight, String leftedWeight) {
        mWeightDataToServerRequest = new WeightDataToServerRequest();
        mWeightDataToServerRequest.addUrlParam("devid", devid);
        mWeightDataToServerRequest.addUrlParam("eatWeight", eatWeight);
        mWeightDataToServerRequest.addUrlParam("leftedWeight", leftedWeight);
        mWeightDataToServerRequest.setRequestListener(new RequestListener<BaseModel>() {
            @Override
            public void onComplete() {

            }

            @Override
            public void onSuccess(BaseModel result) {
                Log.i("weight", "吃粮克数、剩余克数成功发送");
            }

            @Override
            public void onError(Exception e) {

            }
        });
        HttpManager.addRequest(mWeightDataToServerRequest);
    }


    private boolean isFirst = true;

    @Override
    protected void onResume() {
        super.onResume();
        if (isFirst) {
            init();
            registerDev();
            mBaseHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mCameraHelper != null) {
                        mCameraHelper.registerUSB();
                    }
                }
            }, 1000); //延迟1s去初始化摄像头
            isFirst = false;
        } else {
            if (mCameraHelper != null) {
                mCameraHelper.registerUSB();
            }
        }
        if (mMotionDetector != null) {
            mMotionDetector.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCameraHelper != null) {
            mCameraHelper.unregisterUSB();
        }
        if (mMotionDetector != null) {
            mMotionDetector.onPause();
        }
    }
}
