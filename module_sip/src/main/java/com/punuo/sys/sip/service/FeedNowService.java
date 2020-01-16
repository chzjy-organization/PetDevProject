package com.punuo.sys.sip.service;

import android.util.EventLog;
import android.util.Log;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.punuo.sys.sdk.util.ToastUtils;
import com.punuo.sys.sip.model.FeedNotifyData;
import com.punuo.sys.sip.request.BaseSipRequest;

import org.greenrobot.eventbus.EventBus;
import org.zoolu.sip.message.Message;

@Route(path= ServicePath.PATH_FEED_NOW)
public class FeedNowService extends NormalRequestService<FeedNotifyData> {
    @Override
    protected String getBody() {
        return null;
    }

    @Override
    protected void onSuccess(Message msg, FeedNotifyData result) {
        ToastUtils.showToast("收到sip请求");
//        String feedCount = result.feedCount;
        EventBus.getDefault().post(result);
    }

    @Override
    protected void onError(Exception e) {

    }

    @Override
    public void handleTimeOut(BaseSipRequest baseSipRequest) {

    }
}
