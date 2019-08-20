package com.punuo.sys.sip.service;

import android.content.Context;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.google.gson.JsonElement;
import com.punuo.sys.sdk.httplib.JsonUtil;
import com.punuo.sys.sip.model.ControlData;

import android_serialport_api.SerialPortManager;

/**
 * Created by han.chen.
 * Date on 2019-08-20.
 * 云台控制
 **/
@Route(path = ServicePath.PATH_DIRECTION_CONTROL)
public class DirectionControlService implements SipRequestService {
    @Override
    public void init(Context context) {

    }

    @Override
    public void handleRequest(JsonElement jsonElement) {
        ControlData controlData = JsonUtil.fromJson(jsonElement, ControlData.class);
        if ("left".equals(controlData.operate)) {
            SerialPortManager.getInstance().writeData(SerialPortManager.TURN_LEFT);
        } else if ("right".equals(controlData.operate)) {
            SerialPortManager.getInstance().writeData(SerialPortManager.TURN_RIGHT);
        } else if ("stop".equals(controlData.operate)) {
            SerialPortManager.getInstance().writeData(SerialPortManager.STOP);
        }
    }

}
