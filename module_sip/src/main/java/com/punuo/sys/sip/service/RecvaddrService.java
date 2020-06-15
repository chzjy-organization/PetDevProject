package com.punuo.sys.sip.service;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.punuo.sys.sdk.util.HandlerExceptionUtils;
import com.punuo.sys.sip.model.RecvaddrData;
import com.punuo.sys.sip.request.BaseSipRequest;
import com.punuo.sys.sip.video.H264Config;

import org.greenrobot.eventbus.EventBus;
import org.zoolu.sip.message.Message;

/**
 * Created by han.chen.
 * Date on 2020/6/14.
 **/
@Route(path = ServicePath.PATH_RECVADDR)
public class RecvaddrService extends NormalRequestService<RecvaddrData> {
    @Override
    protected String getBody() {
        return null;
    }

    @Override
    protected void onSuccess(Message msg, RecvaddrData result) {
        onResponse(msg);
        H264Config.videoNum--;
        if (H264Config.videoNum == 0) {
            EventBus.getDefault().post(result);
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
