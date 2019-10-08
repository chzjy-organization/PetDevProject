package com.punuo.sys.sip.service;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.punuo.sys.sdk.util.HandlerExceptionUtils;
import com.punuo.sys.sip.SipDevManager;
import com.punuo.sys.sip.model.NegotiateResponse;
import com.punuo.sys.sip.request.BaseSipRequest;
import com.punuo.sys.sip.request.SipDevRegisterRequest;

import org.zoolu.sip.message.Message;

/**
 * Created by han.chen.
 * Date on 2019-09-23.
 * 注册第一步Response
 **/
@Route(path = ServicePath.PATH_REGISTER)
public class RegisterService extends NormalRequestService<NegotiateResponse> {
    @Override
    protected String getBody() {
        return null;
    }

    @Override
    protected void onSuccess(Message msg, NegotiateResponse result) {
        SipDevRegisterRequest sipDevRegisterRequest = new SipDevRegisterRequest(result);
        SipDevManager.getInstance().addRequest(sipDevRegisterRequest);
    }

    @Override
    protected void onError(Exception e) {
        HandlerExceptionUtils.handleException(e);
    }

    @Override
    public void handleTimeOut(BaseSipRequest baseSipRequest) {

    }
}
