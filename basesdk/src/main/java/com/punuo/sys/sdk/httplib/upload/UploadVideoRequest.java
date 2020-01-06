package com.punuo.sys.sdk.httplib.upload;

import com.punuo.sys.sdk.httplib.BaseRequest;

import okhttp3.MediaType;

/**
 * Created by han.chen.
 * Date on 2020-01-04.
 **/
public class UploadVideoRequest extends BaseRequest<UploadResult> {
    public UploadVideoRequest() {
        setRequestType(RequestType.UPLOAD);
        setRequestPath("movedetectpic/addPics");
        contentType(MediaType.parse("video/*"));
    }
}
