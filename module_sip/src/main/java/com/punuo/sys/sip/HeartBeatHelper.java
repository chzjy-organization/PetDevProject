package com.punuo.sys.sip;

import com.punuo.sys.sip.request.SipHeartBeatRequest;

/**
 * Created by han.chen.
 * Date on 2019-08-12.
 * 心跳包活工具
 **/
public class HeartBeatHelper {
    public static final int DELAY = 20 * 1000;

    public static void heartBeat() {
        SipHeartBeatRequest heartBeatRequest = new SipHeartBeatRequest();
        SipDevManager.getInstance().addRequest(heartBeatRequest);
    }
}
