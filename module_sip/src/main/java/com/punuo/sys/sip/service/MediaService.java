package com.punuo.sys.sip.service;

import android.util.Log;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.punuo.sys.sdk.util.HandlerExceptionUtils;
import com.punuo.sys.sip.model.MediaData;
import com.punuo.sys.sip.request.BaseSipRequest;

import org.zoolu.sip.message.Message;

/**
 * Created by han.chen.
 * Date on 2019-08-21.
 **/
@Route(path = ServicePath.PATH_MEDIA)
public class MediaService extends NormalRequestService<MediaData> {

    @Override
    protected String getBody() {
        return null;
    }

    @Override
    protected void onSuccess(Message msg, MediaData result) {
        Log.d("han.chen", "Media 收到视频请求");
        if (result != null) {
            onResponse(msg);
            ARouter.getInstance().build("/sip/video_preview")
                    .navigation();
        }
    }

    @Override
    protected void onError(Exception e) {
        HandlerExceptionUtils.handleException(e);
    }

    @Override
    public void handleTimeOut(BaseSipRequest baseSipRequest) {

    }
}
