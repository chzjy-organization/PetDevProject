package com.punuo.sys.sip.service;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.punuo.sys.sdk.util.HandlerExceptionUtils;
import com.punuo.sys.sip.model.ControlData;

import org.zoolu.sip.message.Message;

import android_serialport_api.SerialPortManager;

/**
 * Created by han.chen.
 * Date on 2019-08-20.
 * 云台控制
 **/
@Route(path = ServicePath.PATH_DIRECTION_CONTROL)
public class DirectionControlService extends NormalRequestService<ControlData> {

    @Override
    protected String getBody() {
        return "";
    }

    @Override
    protected void onSuccess(Message msg, ControlData controlData) {
        if ("left".equals(controlData.operate)) {
            SerialPortManager.getInstance().writeData(SerialPortManager.TURN_LEFT);
        } else if ("right".equals(controlData.operate)) {
            SerialPortManager.getInstance().writeData(SerialPortManager.TURN_RIGHT);
        } else if ("stop".equals(controlData.operate)) {
            SerialPortManager.getInstance().writeData(SerialPortManager.STOP);
        }
        onResponse(msg);
    }

    @Override
    protected void onError(Exception e) {
        HandlerExceptionUtils.handleException(e);
    }

}
