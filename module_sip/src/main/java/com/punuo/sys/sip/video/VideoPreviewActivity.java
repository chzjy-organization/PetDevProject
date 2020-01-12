package com.punuo.sys.sip.video;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.punuo.stream.NativeStreamer;
import com.punuo.sys.sdk.activity.BaseActivity;
import com.punuo.sys.sdk.util.CommonUtil;
import com.punuo.sys.sip.R;
import com.punuo.sys.sip.R2;
import com.punuo.sys.sip.model.RecvaddrData;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by han.chen.
 * Date on 2019-09-16.
 * 手机上测试用的
 **/
//@Route(path = "/sip/video_preview")
public class VideoPreviewActivity extends BaseActivity {
    private String TAG = "VideoPreviewActivity";

    @BindView(R2.id.surface_view)
    SurfaceView mSurfaceView;

    private SurfaceHolder mSurfaceHolder;
    private Camera mCamera;
    private NativeStreamer mNativeStreamer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sip_activity_video_preview);
        //防止锁屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ButterKnife.bind(this);
        init();
        initSurfaceViewSize();
        EventBus.getDefault().register(this);
    }

    private void init() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        mNativeStreamer = new NativeStreamer();
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
            mSurfaceHolder = holder;
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            resetCamera();
            initCamera();
            encodeStart();
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initParameters(Camera camera) {
        if (camera == null) {
            return;
        }
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewFormat(ImageFormat.NV21);
        parameters.setPreviewSize(H264Config.VIDEO_WIDTH, H264Config.VIDEO_HEIGHT);
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        camera.setParameters(parameters);
        camera.startPreview();
    }

    private Camera.PreviewCallback mPreviewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            if (mNativeStreamer != null && rtmpOpenResult != -1) {
                mNativeStreamer.onPreviewFrame(data, H264Config.VIDEO_WIDTH, H264Config.VIDEO_HEIGHT);
            }
        }
    };

    private int rtmpOpenResult = -1; //推流启动是否成功  -1:失败 0: 成功

    private void encodeStart() {
        if (mNativeStreamer != null) {
            rtmpOpenResult = mNativeStreamer.startPublish(H264Config.RTMP_STREAM,
                    H264Config.VIDEO_WIDTH, H264Config.VIDEO_HEIGHT);
        }
    }

    private void encodeStop() {
        if (rtmpOpenResult != -1) {
            mNativeStreamer.stopPublish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        resetCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        encodeStop();
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(RecvaddrData data) {
        finish();
    }
}
