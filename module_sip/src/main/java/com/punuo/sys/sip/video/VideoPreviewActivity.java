package com.punuo.sys.sip.video;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.punuo.sys.sdk.util.CommonUtil;
import com.punuo.sys.sip.R;
import com.punuo.sys.sip.R2;
import com.punuo.sys.sip.model.RecvaddrData;
import com.serenegiant.common.BaseActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.opencore.avch264.NativeH264Encoder;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by han.chen.
 * Date on 2019-09-16.
 **/
//@Route(path = "/sip/video_preview")
public class VideoPreviewActivity extends BaseActivity {
    private String TAG = "VideoPreviewActivity";

    @BindView(R2.id.surface_view)
    SurfaceView mSurfaceView;

    private SurfaceHolder mSurfaceHolder;
    private Camera mCamera;
    private CameraBuffer mCameraBuffer;
    private VideoEncodeThread mVideoEncodeThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sip_activity_video_preview);
        //防止锁屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ButterKnife.bind(this);
        initSurfaceViewSize();
        init();
        EventBus.getDefault().register(this);
    }

    private void init() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        mCameraBuffer = new CameraBuffer();
        NativeH264Encoder.InitEncoder(H264Config.VIDEO_WIDTH, H264Config.VIDEO_HEIGHT, H264Config.FRAME_RATE);

        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(mSurfaceHolderCallback);

    }

    private void initSurfaceViewSize() {
        int width = CommonUtil.getWidth();
        int height = H264Config.VIDEO_WIDTH * width / H264Config.VIDEO_HEIGHT; //旋转90度宽高旋转了
        mSurfaceView.getLayoutParams().height = height;
    }

    private SurfaceHolder.Callback mSurfaceHolderCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {

        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            resetCamera();
            initCamera();
            startEncoding();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    };

    private void resetCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    private void initCamera() {
        if (mCamera != null) {
            return;
        }
        try {
            //TODO 打开外接摄像头
            mCamera = Camera.open();
            if (mCamera == null) {
                return;
            }
            mCamera.setPreviewDisplay(mSurfaceHolder);
            mCamera.setPreviewCallback(mPreviewCallback);
            mCamera.setDisplayOrientation(90);
            initParameters(mCamera);
            mCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initParameters(Camera camera) {
        if (camera == null) {
            return;
        }
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewFpsRange(H264Config.FRAME_RATE, H264Config.FRAME_RATE);
        parameters.setPictureFormat(ImageFormat.YV12);
        parameters.setPreviewSize(H264Config.VIDEO_WIDTH, H264Config.VIDEO_HEIGHT);
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        camera.setParameters(parameters);
    }

    private Camera.PreviewCallback mPreviewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            if (mCameraBuffer != null) {
                mCameraBuffer.setFrame(data);
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

    @Override
    protected void onPause() {
        super.onPause();
        resetCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopEncoding();
        NativeH264Encoder.DeinitEncoder();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(RecvaddrData data) {
        finish();
    }

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
                encodeResult = NativeH264Encoder.EncodeFrame(frameData, encoderTs);
                int encodeState = NativeH264Encoder.getLastEncodeStatus();
                if (encodeState == 0 && encodeResult.length > 0) {
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
}
