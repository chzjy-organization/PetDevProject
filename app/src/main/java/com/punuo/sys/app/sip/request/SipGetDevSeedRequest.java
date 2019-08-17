package com.punuo.sys.app.sip.request;

import com.punuo.sip.SipConfig;
import com.punuo.sip.request.BaseSipRequest;
import com.punuo.sip.request.SipRequestType;
import com.punuo.sys.app.sip.model.RegisterData;

import org.zoolu.sip.address.NameAddress;

/**
 * Created by han.chen.
 * Date on 2019-08-12.
 * 提交密码
 **/
public class SipGetDevSeedRequest extends BaseSipRequest<RegisterData> {
    public SipGetDevSeedRequest() {
        setSipRequestType(SipRequestType.Register);
    }

    @Override
    public NameAddress getLocalNameAddress() {
        return SipConfig.getDevNormalAddress();
    }
}
