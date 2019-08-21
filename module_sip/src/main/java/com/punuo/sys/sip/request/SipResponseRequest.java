package com.punuo.sys.sip.request;

/**
 * Created by han.chen.
 * Date on 2019-08-21.
 **/
public class SipResponseRequest extends BaseSipRequest<String> {
    private String body;

    public SipResponseRequest() {
        setSipRequestType(SipRequestType.Response);
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String getBody() {
        return body;
    }
}
