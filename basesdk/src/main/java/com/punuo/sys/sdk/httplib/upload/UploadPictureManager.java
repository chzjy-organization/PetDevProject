package com.punuo.sys.sdk.httplib.upload;

import android.util.Log;

import com.punuo.sys.sdk.httplib.HttpManager;
import com.punuo.sys.sdk.httplib.RequestListener;

import java.io.File;

/**
 * Created by han.chen.
 * Date on 2020-01-04.
 **/
public class UploadPictureManager {

    private static UploadPictureManager instance;

    public static UploadPictureManager getInstance() {
        if (instance == null) {
            synchronized (UploadPictureManager.class) {
                if (instance == null) {
                    instance = new UploadPictureManager();
                }
            }
        }
        return instance;
    }

    private UploadPictureRequest mUploadPictureRequest;

    public void uploadPicture(String path, String devId) {
        uploadPicture(path, devId, null);
    }

    public void uploadPicture(String path, String devId, RequestListener<UploadResult> listener) {
//        if (mUploadPictureRequest != null && !mUploadPictureRequest.isFinish()) {
//            return;
//        }
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        mUploadPictureRequest = new UploadPictureRequest();
        mUploadPictureRequest.addEntityParam("photo", file);
        mUploadPictureRequest.addEntityParam("devid", devId);
        mUploadPictureRequest.setRequestListener(listener);
        HttpManager.addRequest(mUploadPictureRequest);
    }

    private UploadVideoRequest mUploadVideoRequest;
    public void uploadVideo(String path, String devId) {
//        if (mUploadVideoRequest != null && !mUploadVideoRequest.isFinish()) {
//            return;
//        }
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        mUploadVideoRequest = new UploadVideoRequest();
        mUploadVideoRequest.addEntityParam("video", file);
        mUploadVideoRequest.addEntityParam("devid", devId);
        mUploadVideoRequest.setRequestListener(new RequestListener<UploadResult>() {
            @Override
            public void onComplete() {

            }

            @Override
            public void onSuccess(UploadResult result) {
                Log.i("upLoadvideo", result.success+result.message+result.database);
            }

            @Override
            public void onError(Exception e) {

            }
        });
        HttpManager.addRequest(mUploadVideoRequest);
    }
}
