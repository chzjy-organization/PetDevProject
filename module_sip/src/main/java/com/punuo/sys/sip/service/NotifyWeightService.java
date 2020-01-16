package com.punuo.sys.sip.service;

import android.util.Log;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.punuo.sys.sdk.util.ToastUtils;
import com.punuo.sys.sip.model.WeightData;
import com.punuo.sys.sip.request.BaseSipRequest;

import org.zoolu.sip.message.Message;


@Route(path = ServicePath.PATH_WEIGHT)
public class NotifyWeightService extends NormalRequestService<WeightData> {


    @Override
    protected String getBody() {
        return null;
    }

    @Override
    protected void onSuccess(Message msg, WeightData result) {
        Log.i("weight ", "重量数据成功发送到Sip服务器");
        if(result != null){
            String quality_response = "quality = "+result.quality;
            ToastUtils.showToast(quality_response);
        }
    }

    @Override
    protected void onError(Exception e) {

    }

    @Override
    public void handleTimeOut(BaseSipRequest baseSipRequest) {

    }
}
