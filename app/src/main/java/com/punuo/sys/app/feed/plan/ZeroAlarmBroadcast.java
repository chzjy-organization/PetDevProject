package com.punuo.sys.app.feed.plan;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.punuo.sys.app.HomeActivity;
import com.punuo.sys.sdk.account.AccountManager;
import com.punuo.sys.sdk.httplib.HttpManager;
import com.punuo.sys.sdk.httplib.RequestListener;
import com.punuo.sys.sip.config.SipConfig;
import com.punuo.sys.sip.model.FeedPlan;

public class ZeroAlarmBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if ("com.punuo.sys.app.SETZEROFEED".equals(action)){
            getPlan(context);

        }
    }

    private DevGetPlanRequest mGetPlanRequest;
    public void getPlan(Context context){
        if (mGetPlanRequest != null && mGetPlanRequest.isFinish()) {
            return;
        }
        mGetPlanRequest = new DevGetPlanRequest();
        mGetPlanRequest.addUrlParam("devid", SipConfig.getDevId());
        mGetPlanRequest.setRequestListener(new RequestListener<PlanModel>() {
            @Override
            public void onComplete() {
            }
            @Override
            public void onSuccess(PlanModel result) {
                if (result == null || result.mPlanList == null) {
                    return;
                }
                for (int i = 0;i<result.mPlanList.size();i+=1) {
                    FeedPlan plan = result.mPlanList.get(i);
                    FeedAlarmManager.getInstance().addAlarmTask(context,plan);
                    Log.i("plan", "成功获取到喂食计划并创建alarm");
                }
            }
            @Override
            public void onError(Exception e) {

            }
        });
        HttpManager.addRequest(mGetPlanRequest);
    }
}
