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

package com.punuo.sys.sip.video;

import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.punuo.sys.sdk.util.CommonUtil;
import com.punuo.sys.sip.R;
import com.punuo.sys.sip.model.RecvaddrData;
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
import org.opencore.avch264.NativeH264Encoder;

import java.nio.ByteBuffer;
import java.util.List;

@Route(path = "/sip/video_preview")
public class TestActivity extends BaseActivity implements CameraDialog.CameraDialogParent {
    private static final boolean DEBUG = true;    // TODO set false when production
    private static final String TAG = "MainActivity";

    private final Object mSync = new Object();
    // for accessing USB and USB camera
    private USBMonitor mUSBMonitor;
    private UVCCamera mUVCCamera;
    private SurfaceView mUVCCameraView;
    // for open&start / stop&close camera preview
    private ImageButton mCameraButton;
    private Surface mPreviewSurface;
    private boolean isActive, isPreview;
    private CameraBuffer mCameraBuffer;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sip_activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        MediaRtpSender.getInstance().init();
        mCameraBuffer = new CameraBuffer();
        NativeH264Encoder.InitEncoder(H264Config.VIDEO_WIDTH, H264Config.VIDEO_HEIGHT, H264Config.FRAME_RATE);

        mUSBMonitor = new USBMonitor(this, mOnDeviceConnectListener);
        mCameraButton = (ImageButton) findViewById(R.id.camera_button);
        mCameraButton.setOnClickListener(mOnClickListener);

        mUVCCameraView = (SurfaceView) findViewById(R.id.camera_surface_view);
        initSurfaceViewSize();
        mUVCCameraView.getHolder().addCallback(mSurfaceViewCallback);

        startEncoding();
        EventBus.getDefault().register(this);
    }

    private void initSurfaceViewSize() {
        int width = CommonUtil.getWidth();
        int height = H264Config.VIDEO_HEIGHT * width / H264Config.VIDEO_WIDTH;
        mUVCCameraView.getLayoutParams().height = height;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (DEBUG) Log.v(TAG, "onStart:");
        synchronized (mSync) {
            if (mUSBMonitor != null) {
                mUSBMonitor.register();
            }
        }
    }

    @Override
    protected void onStop() {
        if (DEBUG) Log.v(TAG, "onStop:");
        synchronized (mSync) {
            if (mUSBMonitor != null) {
                mUSBMonitor.unregister();
            }
        }
        super.onStop();
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
                mUSBMonitor.destroy();
                mUSBMonitor = null;
            }
        }
        mUVCCameraView = null;
        mCameraButton = null;
        stopEncoding();
        EventBus.getDefault().unregister(this);
        //延迟销毁NativeH264Encoder
        getWindow().getDecorView().postDelayed(new Runnable() {
            @Override
            public void run() {
                NativeH264Encoder.DeinitEncoder();
            }
        }, 500);
        super.onDestroy();
    }

    private final OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(final View view) {
            if (mUVCCamera == null) {
                // XXX calling CameraDialog.showDialog is necessary at only first time(only when app has no permission).
                CameraDialog.showDialog(TestActivity.this);
            } else {
                synchronized (mSync) {
                    mUVCCamera.destroy();
                    mUVCCamera = null;
                    isActive = isPreview = false;
                }
            }
        }
    };

    private final OnDeviceConnectListener mOnDeviceConnectListener = new OnDeviceConnectListener() {
        @Override
        public void onAttach(final UsbDevice device) {
            if (DEBUG) Log.v(TAG, "onAttach:");
            Toast.makeText(TestActivity.this, "USB_DEVICE_ATTACHED", Toast.LENGTH_SHORT).show();
            List<UsbDevice> deviceList = mUSBMonitor.getDeviceList();
            if (!deviceList.isEmpty()) {
                UsbDevice usbDevice = deviceList.get(0);
                mUSBMonitor.requestPermission(usbDevice);
            }
        }

        @Override
        public void onConnect(final UsbDevice device, final UsbControlBlock ctrlBlock, final boolean createNew) {
            if (DEBUG) Log.v(TAG, "onConnect:");
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
                            camera.setFrameCallback(mIFrameCallback, UVCCamera.PIXEL_FORMAT_NV21);
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
            Toast.makeText(TestActivity.this, "USB_DEVICE_DETACHED", Toast.LENGTH_SHORT).show();
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

    private final IFrameCallback mIFrameCallback = new IFrameCallback() {
        @Override
        public void onFrame(final ByteBuffer frame) {
            if (mCameraBuffer != null) {
                mCameraBuffer.setFrame(frame.array());
            }
        }
    };

    private void startEncoding() {
        started = true;
        mVideoEncodeThread = new VideoEncodeThread();
        mVideoEncodeThread.start();
    }

    private void stopEncoding() {
        started = false;
        try {
            if (mVideoEncodeThread != null) {
                mVideoEncodeThread.interrupt();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mVideoEncodeThread = null;
    }
    private VideoEncodeThread mVideoEncodeThread;
    private boolean started = false;
    public class VideoEncodeThread extends Thread {
        private String TAG = "VideoEncodeThread";

        @Override
        public void run() {
            if (mCameraBuffer == null) {
                return;
            }

            int timeToSleep = 1000 / H264Config.FRAME_RATE;
            byte[] frameData;
            byte[] encodeResult;
            long encoderTs = 0;
            long oldTs = System.currentTimeMillis();
            while (started) {
                long time = System.currentTimeMillis();
                encoderTs = encoderTs + (time - oldTs);
                frameData = mCameraBuffer.getFrame();
                try {
                    encodeResult = NativeH264Encoder.EncodeFrame(frameData, encoderTs);
                } catch (Exception e) {
                    e.printStackTrace();
                    encodeResult = null;
                }
                int encodeState = NativeH264Encoder.getLastEncodeStatus();
                if (encodeState == 0 && encodeResult != null && encodeResult.length > 0) {
                    //TODO 编码成功分包发送
                    Log.d(TAG, "编码成功");
//                MediaRtpSender.getInstance().divideAndSendNal(encodeResult);
                }
                // Sleep between frames if necessary
                long delta = System.currentTimeMillis() - time;
                if (delta < timeToSleep) {
                    try {
                        Thread.sleep((timeToSleep - delta) - (((timeToSleep - delta) * 10) / 100));
                    } catch (InterruptedException e) {
                    }
                }
                oldTs = time;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(RecvaddrData event) {
       finish();
    }
}
