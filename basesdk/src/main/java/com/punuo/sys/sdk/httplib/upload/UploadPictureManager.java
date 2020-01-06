package com.punuo.sys.sdk.httplib.upload;

import com.punuo.sys.sdk.httplib.HttpManager;

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
        if (mUploadPictureRequest != null && !mUploadPictureRequest.isFinish()) {
            return;
        }
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        mUploadPictureRequest = new UploadPictureRequest();
        mUploadPictureRequest.addEntityParam("photo", file);
        mUploadPictureRequest.addEntityParam("devid", devId);
        HttpManager.addRequest(mUploadPictureRequest);
    }

    private UploadVideoRequest mUploadVideoRequest;
    public void uploadVideo(String path, String devId) {
        if (mUploadVideoRequest != null && !mUploadVideoRequest.isFinish()) {
            return;
        }
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        mUploadVideoRequest = new UploadVideoRequest();
        mUploadVideoRequest.addEntityParam("photo", file);
        mUploadVideoRequest.addEntityParam("userName", devId);
        HttpManager.addRequest(mUploadVideoRequest);
    }
}
