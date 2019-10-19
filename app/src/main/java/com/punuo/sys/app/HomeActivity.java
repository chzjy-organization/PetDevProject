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

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Message;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.punuo.stream.NativeStreamer;
import com.punuo.sys.app.RotationControl.TurnAndStop;
import com.punuo.sys.app.bluetooth.BluetoothChatService;
import com.punuo.sys.app.bluetooth.Constants;
import com.punuo.sys.app.bluetooth.PTOMessage;
import com.punuo.sys.app.led.LedControl;
import com.punuo.sys.app.led.LedData;
import com.punuo.sys.app.process.ProcessTasks;
import com.punuo.sys.app.weighing.WeighingActivity;
import com.punuo.sys.app.weighing.tool.WeightReset;
import com.punuo.sys.app.wifi.OnServerWifiListener;
import com.punuo.sys.app.wifi.WifiController;
import com.punuo.sys.app.wifi.WifiMessage;
import com.punuo.sys.app.wifi.WifiUtil;
import com.punuo.sys.sdk.PnApplication;
import com.punuo.sys.sdk.util.BaseHandler;
import com.punuo.sys.sdk.util.CommonUtil;
import com.punuo.sys.sdk.util.ToastUtils;
import com.punuo.sys.sip.HeartBeatHelper;
import com.punuo.sys.sip.SipDevManager;
import com.punuo.sys.sip.event.ReRegisterEvent;
import com.punuo.sys.sip.model.LoginResponse;
import com.punuo.sys.sip.model.RecvaddrData;
import com.punuo.sys.sip.request.SipGetDevSeedRequest;
import com.punuo.sys.sip.video.H264Config;
import com.serenegiant.common.BaseActivity;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.IFrameCallback;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.USBMonitor.OnDeviceConnectListener;
import com.serenegiant.usb.USBMonitor.UsbControlBlock;
import com.serenegiant.usb.UVCCamera;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class HomeActivity extends BaseActivity implements CameraDialog.CameraDialogParent, BaseHandler.MessageHandler {
    private static final boolean DEBUG = true;
    private static final String TAG = "HomeActivity";
    public static final int MSG_HEART_BEAR_VALUE = 10086;
    private final Object mSync = new Object();
    // for accessing USB and USB camera
    private USBMonitor mUSBMonitor;
    private UVCCamera mUVCCamera;
    private SurfaceView mUVCCameraView;
    // for open&start / stop&close camera preview
    private Surface mPreviewSurface;
    private boolean isActive, isPreview;
    private NativeStreamer mNativeStreamer;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothChatService mBluetoothChatService;
    private WifiManager mWifiManager;
    private boolean isFirst = true;
    private Button mWeight;
    private Button mReset;
    private TurnAndStop turn = new TurnAndStop();
    private LedControl ledControl;
    private IntentFilter intentFilter;
    private NetworkChangeReceiver networkChangeReceiver;
    private BaseHandler mBaseHandler;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        mBaseHandler = new BaseHandler(this);
        ProcessTasks.commonLaunchTasks(PnApplication.getInstance());
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothChatService = BluetoothChatService.getInstance(this, mBaseHandler);
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        ledControl = new LedControl();
        intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        networkChangeReceiver = new NetworkChangeReceiver();
        registerReceiver(networkChangeReceiver, intentFilter);


        mNativeStreamer = new NativeStreamer();
        mUSBMonitor = new USBMonitor(this, mOnDeviceConnectListener);
        mUVCCameraView = findViewById(R.id.camera_surface_view);
        initSurfaceViewSize();
        mUVCCameraView.getHolder().addCallback(mSurfaceViewCallback);
        EventBus.getDefault().register(this);
        retryTimes = 0;
        //开启推流
        encodeStart();

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
        mWeight = findViewById(R.id.weight);
        mWeight.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, WeighingActivity.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WeightReset weightReset = new WeightReset();
                weightReset.reset();
            }
        });
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
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (DEBUG) Log.v(TAG, "onStart:");
        synchronized (mSync) {
        }
    }

    @Override
    protected void onStop() {
        if (DEBUG) Log.v(TAG, "onStop:");
        super.onStop();
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
            queueEvent(new Runnable() {
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
        synchronized (mSync) {
            isActive = isPreview = false;
            if (mUVCCamera != null) {
                mUVCCamera.destroy();
                mUVCCamera = null;
            }
            if (mUSBMonitor != null) {
                mUSBMonitor.unregister();
                mUSBMonitor.destroy();
                mUSBMonitor = null;
            }
        }
        mUVCCameraView = null;
        EventBus.getDefault().unregister(this);
        unregisterReceiver(networkChangeReceiver);
        super.onDestroy();
    }

    private final OnDeviceConnectListener mOnDeviceConnectListener = new OnDeviceConnectListener() {
        @Override
        public void onAttach(final UsbDevice device) {
            List<UsbDevice> deviceList = mUSBMonitor.getDeviceList();
            if (!deviceList.isEmpty()) {
                UsbDevice usbDevice = deviceList.get(0);
                mUSBMonitor.requestPermission(usbDevice);
            }
        }

        @Override
        public void onConnect(final UsbDevice device, final UsbControlBlock ctrlBlock, final boolean createNew) {
            ToastUtils.showToast("摄像头链接成功");
            synchronized (mSync) {
                if (mUVCCamera != null) {
                    mUVCCamera.destroy();
                }
                isActive = isPreview = false;
            }
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    synchronized (mSync) {
                        final UVCCamera camera = new UVCCamera();
                        camera.open(ctrlBlock);
                        if (DEBUG) Log.i(TAG, "supportedSize:" + camera.getSupportedSize());
                        try {
                            camera.setPreviewSize(H264Config.VIDEO_WIDTH, H264Config.VIDEO_HEIGHT, UVCCamera.FRAME_FORMAT_MJPEG);
                        } catch (final IllegalArgumentException e) {
                            try {
                                // fallback to YUV mode
                                camera.setPreviewSize(H264Config.VIDEO_WIDTH, H264Config.VIDEO_HEIGHT, UVCCamera.DEFAULT_PREVIEW_MODE);
                            } catch (final IllegalArgumentException e1) {
                                camera.destroy();
                                return;
                            }
                        }
                        mPreviewSurface = mUVCCameraView.getHolder().getSurface();
                        if (mPreviewSurface != null) {
                            isActive = true;
                            camera.setPreviewDisplay(mPreviewSurface);
                            camera.setFrameCallback(mIFrameCallback, UVCCamera.PIXEL_FORMAT_YUV420SP);
                            camera.startPreview();
                            isPreview = true;
                        }
                        synchronized (mSync) {
                            mUVCCamera = camera;
                        }
                    }
                }
            }, 0);
        }

        @Override
        public void onDisconnect(final UsbDevice device, final UsbControlBlock ctrlBlock) {
            if (DEBUG) Log.v(TAG, "onDisconnect:");
            // XXX you should check whether the comming device equal to camera device that currently using
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    synchronized (mSync) {
                        if (mUVCCamera != null) {
                            mUVCCamera.close();
                            if (mPreviewSurface != null) {
                                mPreviewSurface.release();
                                mPreviewSurface = null;
                            }
                            isActive = isPreview = false;
                        }
                    }
                }
            }, 0);
        }

        @Override
        public void onDettach(final UsbDevice device) {
            if (DEBUG) Log.v(TAG, "onDettach:");
            Toast.makeText(HomeActivity.this, "USB_DEVICE_DETACHED", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel(final UsbDevice device) {
        }
    };

    /**
     * to access from CameraDialog
     *
     * @return
     */
    @Override
    public USBMonitor getUSBMonitor() {
        return mUSBMonitor;
    }

    @Override
    public void onDialogResult(boolean canceled) {
        if (canceled) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // FIXME
                }
            }, 0);
        }
    }

    private final SurfaceHolder.Callback mSurfaceViewCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(final SurfaceHolder holder) {
            if (DEBUG) Log.v(TAG, "surfaceCreated:");
        }

        @Override
        public void surfaceChanged(final SurfaceHolder holder, final int format, final int width, final int height) {
            if ((width == 0) || (height == 0)) return;
            if (DEBUG) Log.v(TAG, "surfaceChanged:");
            mPreviewSurface = holder.getSurface();
            synchronized (mSync) {
                if (isActive && !isPreview && (mUVCCamera != null)) {
                    mUVCCamera.setPreviewDisplay(mPreviewSurface);
                    mUVCCamera.startPreview();
                    isPreview = true;
                }
            }
        }

        @Override
        public void surfaceDestroyed(final SurfaceHolder holder) {
            if (DEBUG) Log.v(TAG, "surfaceDestroyed:");
            synchronized (mSync) {
                if (mUVCCamera != null) {
                    mUVCCamera.stopPreview();
                }
                isPreview = false;
            }
            mPreviewSurface = null;
        }
    };
    private byte[] mBytes = new byte[H264Config.VIDEO_WIDTH * H264Config.VIDEO_HEIGHT * 3 / 2];
    private final IFrameCallback mIFrameCallback = new IFrameCallback() {
        @Override
        public void onFrame(final ByteBuffer frame) {
            mBytes = new byte[frame.remaining()];
            frame.get(mBytes, 0, mBytes.length);
            if (isPreview && mNativeStreamer != null && rtmpOpenResult != -1) {
                mNativeStreamer.onPreviewFrame(mBytes, H264Config.VIDEO_WIDTH, H264Config.VIDEO_HEIGHT);
            }
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(RecvaddrData event) {
        encodeStop();
        queueEvent(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 100);
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
            spark();
        }
    }

    private int clo = 0;

    public void spark() {
        Timer timer = new Timer();
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        TimerTask taskcc = new TimerTask() {
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
                                if (networkInfo != null && networkInfo.isAvailable()) {
                                    ledControl.turnOnCustom1Light();
                                } else {
                                    ledControl.turnOffCustom1Light();
                                }
                            }
                        }
                    }
                });
            }
        };
        timer.schedule(taskcc, 1, 300);
    }

    class NetworkChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isAvailable()) {
                ledControl.turnOnCustom1Light();
            } else {
                ToastUtils.showToast("networkchange");
                ledControl.turnOffCustom1Light();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mUSBMonitor != null) {
            mUSBMonitor.register();
        }
        if (isFirst) {
            init();
            registerDev();
            isFirst = false;
        }
    }
}
