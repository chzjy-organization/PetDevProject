package com.punuo.sys.sip.service;

import android.os.Bundle;
import android.os.Message;

import com.alibaba.android.arouter.launcher.ARouter;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.punuo.sys.sdk.util.BaseHandler;

/**
 * Created by han.chen.
 * Date on 2019-08-20.
 **/
public class SipServiceManager implements BaseHandler.MessageHandler {
    private static SipServiceManager sSipServiceManager;
    private static final int MSG_HANDLER = 1;

    public static SipServiceManager getInstance() {
        if (sSipServiceManager == null) {
            synchronized (SipServiceManager.class) {
                if (sSipServiceManager == null) {
                    sSipServiceManager = new SipServiceManager();
                }
            }
        }
        return sSipServiceManager;
    }
    private BaseHandler mBaseHandler;
    private SipServiceManager() {
        mBaseHandler = new BaseHandler(this);
    }

    public void handleRequest(String key, String jsonStr, org.zoolu.sip.message.Message msg) {
        //回调到主线程
        Message message = new Message();
        message.what = MSG_HANDLER;
        Bundle bundle = new Bundle();
        bundle.putString("key", key);
        bundle.getString("jsonStr", jsonStr);
        message.setData(bundle);
        message.obj = msg;
        mBaseHandler.sendMessage(message);

    }

    @Override
    public void handleMessage(Message msg) {
        try {
            Bundle bundle = msg.getData();
            String key = bundle.getString("key");
            String jsonStr = bundle.getString("jsonStr","{}");
            JsonElement jsonElement = new JsonParser().parse(jsonStr);
            SipRequestService service = (SipRequestService) ARouter.getInstance()
                    .build("/sip/" + key).navigation();
            if (service != null) {
                service.handleRequest((org.zoolu.sip.message.Message) msg.obj, jsonElement);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
