package com.punuo.sys.sip.service;

import android.content.Context;

import com.google.gson.JsonElement;
import com.punuo.sys.sip.SipDevManager;
import com.punuo.sys.sip.request.SipResponseRequest;

import org.zoolu.sip.message.Message;

/**
 * Created by han.chen.
 * Date on 2019-08-21.
 * 已经统一做了response
 **/
public abstract class NormalRequestService implements SipRequestService {
    protected SipResponseRequest mSipResponseRequest;

    @Override
    public void handleRequest(Message msg, JsonElement jsonElement) {
        mSipResponseRequest.setResponse(msg, 200, "OK");
        mSipResponseRequest.setBody(getBody());
        SipDevManager.getInstance().addRequest(mSipResponseRequest);
    }

    @Override
    public void init(Context context) {
        mSipResponseRequest = new SipResponseRequest();
    }

    protected abstract String getBody();
}
