package com.punuo.sys.sip.request;

import com.punuo.sys.sip.SipConfig;
import com.punuo.sys.sip.model.RegisterData;

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
