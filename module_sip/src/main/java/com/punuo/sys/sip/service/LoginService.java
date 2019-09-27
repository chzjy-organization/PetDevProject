package com.punuo.sys.sip.service;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.punuo.sys.sdk.util.HandlerExceptionUtils;
import com.punuo.sys.sip.event.ReRegisterEvent;
import com.punuo.sys.sip.model.LoginResponse;
import com.punuo.sys.sip.request.BaseSipRequest;

import org.greenrobot.eventbus.EventBus;
import org.zoolu.sip.message.Message;

/**
 * Created by han.chen.
 * Date on 2019-09-23.
 * 注册第二步Response / 心跳包Response
 **/
@Route(path = ServicePath.PATH_LOGIN)
public class LoginService extends NormalRequestService<LoginResponse> {
    @Override
    protected String getBody() {
        return null;
    }

    @Override
    protected void onSuccess(Message msg, LoginResponse result) {
        EventBus.getDefault().post(result);
    }

    @Override
    protected void onError(Exception e) {
        HandlerExceptionUtils.handleException(e);
    }

    @Override
    public void handleTimeOut(BaseSipRequest baseSipRequest) {
        EventBus.getDefault().post(new ReRegisterEvent());
    }
}
