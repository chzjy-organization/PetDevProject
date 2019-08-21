package com.punuo.sys.sip.service;

import com.alibaba.android.arouter.facade.template.IProvider;
import com.google.gson.JsonElement;

import org.zoolu.sip.message.Message;

/**
 * Created by han.chen.
 * Date on 2019-08-20.
 **/
public interface SipRequestService extends IProvider {

    void handleRequest(Message msg, JsonElement jsonElement);
}
