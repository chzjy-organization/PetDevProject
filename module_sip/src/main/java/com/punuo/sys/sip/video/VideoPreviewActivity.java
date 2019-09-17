package com.punuo.sys.sip.video;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.orangelabs.rcs.core.ims.protocol.rtp.codec.video.h264.encoder.NativeH264Encoder;
import com.punuo.sys.sdk.activity.BaseActivity;
import com.punuo.sys.sdk.util.CommonUtil;
import com.punuo.sys.sip.R;
import com.punuo.sys.sip.R2;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by han.chen.
 * Date on 2019-09-16.
 **/
@Route(path = "/sip/video_preview")
public class VideoPreviewActivity extends BaseActivity {

    @BindView(R2.id.surface_view)
    SurfaceView mSurfaceView;

    private SurfaceHolder mSurfaceHolder;
    private Camera mCamera;
    private long time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sip_activity_video_preview);
        //防止锁屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ButterKnife.bind(this);
        initSurfaceViewSize();
        init();
    }

    private void init() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(mSurfaceHolderCallback);
        NativeH264Encoder.InitEncoder(VideoInfoManager.width, VideoInfoManager.height, VideoInfoManager.frameRate);

    }

    private void initSurfaceViewSize() {
        int width = CommonUtil.getWidth();
        int height = VideoInfoManager.width * width / VideoInfoManager.height; //旋转90度宽高旋转了
        mSurfaceView.getLayoutParams().height = height;
    }

    private SurfaceHolder.Callback mSurfaceHolderCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {

        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//            int numCamera = Camera.getNumberOfCameras();
//            Camera.CameraInfo info = new Camera.CameraInfo();
//            for (int i = 0; i < numCamera; i++) {
//                Camera.getCameraInfo(i, info);
//            }
            resetCamera();
            initCamera();
            time = System.currentTimeMillis();
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
        parameters.setPreviewFpsRange(VideoInfoManager.frameRate, VideoInfoManager.frameRate);
        parameters.setPictureFormat(ImageFormat.YV12);
        parameters.setPreviewSize(VideoInfoManager.width, VideoInfoManager.height);
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        camera.setParameters(parameters);
    }

    private Camera.PreviewCallback mPreviewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            //此处是在主线线程
            byte[] encodeResult = NativeH264Encoder.EncodeFrame(data, time);
            time += 1000 / VideoInfoManager.frameRate;
            int encodeState = NativeH264Encoder.getLastEncodeStatus();
            if (encodeState == 0 && encodeResult.length > 0) {
                //编码成功分包发送
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        resetCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initCamera();
        time = System.currentTimeMillis();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NativeH264Encoder.DeinitEncoder();
    }
}
