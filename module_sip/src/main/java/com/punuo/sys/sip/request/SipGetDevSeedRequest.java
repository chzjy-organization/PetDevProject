package com.punuo.sys.sip.request;

import com.punuo.sys.sip.config.SipConfig;

import org.zoolu.sip.address.NameAddress;

/**
 * Created by han.chen.
 * Date on 2019-08-12.
 * 提交密码
 **/
public class SipGetDevSeedRequest extends BaseSipRequest {
    public SipGetDevSeedRequest() {
        setSipRequestType(SipRequestType.Register);
        setTargetResponse("negotiate_response");
    }

    @Override
    public NameAddress getLocalNameAddress() {
        return SipConfig.getDevNormalAddress();
    }
}
