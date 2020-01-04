package com.punuo.sys.sip.service;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.punuo.sys.sip.model.WiFiData;
import com.punuo.sys.sip.request.BaseSipRequest;

import org.greenrobot.eventbus.EventBus;
import org.zoolu.sip.message.Message;

@Route(path = ServicePath.PATH_WIFI)
public class WiFiService extends NormalRequestService<WiFiData>{

    @Override
    protected String getBody() {
        return null;
    }

    @Override
    protected void onSuccess(Message msg, WiFiData result) {
        if (result == null) {
            return;
        }
        EventBus.getDefault().post(result);
    }

    @Override
    protected void onError(Exception e) {

    }

    @Override
    public void handleTimeOut(BaseSipRequest baseSipRequest) {

    }
}
