package com.punuo.sys.sip.service;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.google.gson.JsonElement;
import com.punuo.sys.sdk.httplib.JsonUtil;
import com.punuo.sys.sip.model.ControlData;

import org.zoolu.sip.message.Message;

import android_serialport_api.SerialPortManager;

/**
 * Created by han.chen.
 * Date on 2019-08-20.
 * 云台控制
 **/
@Route(path = ServicePath.PATH_DIRECTION_CONTROL)
public class DirectionControlService extends NormalRequestService {

    @Override
    public void handleRequest(Message msg, JsonElement jsonElement) {
        super.handleRequest(msg, jsonElement);
        ControlData controlData = JsonUtil.fromJson(jsonElement, ControlData.class);
        if ("left".equals(controlData.operate)) {
            SerialPortManager.getInstance().writeData(SerialPortManager.TURN_LEFT);
        } else if ("right".equals(controlData.operate)) {
            SerialPortManager.getInstance().writeData(SerialPortManager.TURN_RIGHT);
        } else if ("stop".equals(controlData.operate)) {
            SerialPortManager.getInstance().writeData(SerialPortManager.STOP);
        }
    }

    @Override
    protected String getBody() {
        return "";
    }

}
