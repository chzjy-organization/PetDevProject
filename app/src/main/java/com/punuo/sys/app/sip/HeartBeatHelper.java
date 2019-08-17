package com.punuo.sys.app.sip;

import com.punuo.sip.SipDevManager;
import com.punuo.sip.request.SipRequestListener;
import com.punuo.sys.app.sip.event.ReRegisterEvent;
import com.punuo.sys.app.sip.model.HeartBeatData;
import com.punuo.sys.app.sip.request.SipHeartBeatRequest;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by han.chen.
 * Date on 2019-08-12.
 * 心跳包活工具
 **/
public class HeartBeatHelper {
    public static final int DELAY = 20 * 1000;

    public static void heartBeat() {
        SipHeartBeatRequest heartBeatRequest = new SipHeartBeatRequest();
        heartBeatRequest.setSipRequestListener(new SipRequestListener<HeartBeatData>() {
            @Override
            public void onComplete() {

            }

            @Override
            public void onSuccess(HeartBeatData result) {
                if (result == null) {
                    return;
                }
                if (result.mLoginResponse == null) {
                    //掉线了， 重新注册
                    EventBus.getDefault().post(new ReRegisterEvent());
                }
            }

            @Override
            public void onError(Exception e) {

            }
        });
        SipDevManager.getInstance().addRequest(heartBeatRequest);
    }
}
