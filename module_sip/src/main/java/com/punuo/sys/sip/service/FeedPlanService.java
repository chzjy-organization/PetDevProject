package com.punuo.sys.sip.service;

import android.util.Log;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.punuo.sys.sip.model.FeedPlan;
import com.punuo.sys.sip.request.BaseSipRequest;

import org.greenrobot.eventbus.EventBus;
import org.zoolu.sip.message.Message;

/**
 * Created by han.chen.
 * Date on 2019-11-23.
 **/
@Route(path = ServicePath.PATH_FEED_PLAN)
public class FeedPlanService extends NormalRequestService<FeedPlan> {


    @Override
    protected String getBody() {
        return null;
    }

    @Override
    protected void onSuccess(Message msg, FeedPlan result) {
        EventBus.getDefault().post(result);
    }

    @Override
    protected void onError(Exception e) {

    }

    @Override
    public void handleTimeOut(BaseSipRequest baseSipRequest) {

    }
}
