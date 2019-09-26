package com.punuo.sys.app.Weighing;

import android.os.Bundle;
import android.util.Log;

import com.leplay.petwight.PetWeight;
import com.punuo.sys.app.Weighing.requset.SipGetWeightRequest;
import com.punuo.sys.app.Weighing.tool.WeightData;
import com.punuo.sys.sdk.activity.BaseActivity;
import com.punuo.sys.sdk.util.HandlerExceptionUtils;
import com.punuo.sys.sip.SipDevManager;
import com.punuo.sys.sip.request.SipRequestListener;

import java.util.Timer;
import java.util.TimerTask;

public class WeighingActivity extends BaseActivity {

    PetWeight mPetWeight;
    Timer mTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTimer = new Timer();
        setTimerTask();
    }


    public void setTimerTask(){
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                getQuality();
                Log.i("测得质量", "质量"+getQuality());
//                sipWeightToServer();
            }
        },0,1000*30);
    }

    //将数据发送到服务器
    private void sipWeightToServer(){
        SipGetWeightRequest getWeightRequest = new SipGetWeightRequest();
        getWeightRequest.setSipRequestListener(new SipRequestListener<WeightData>() {
            @Override
            public void onComplete() {

            }

            @Override
            public void onSuccess(WeightData result) {
                if(result == null){
                    return;
                }
                Log.i("11111", "onSuccess: "+"成功");
            }


            @Override
            public void onError(Exception e) {
                HandlerExceptionUtils.handleException(e);
            }
        });
        SipDevManager.getInstance().addRequest(getWeightRequest);
    }


    public int getQuality(){
        mPetWeight = new PetWeight();
        mPetWeight.getWeight();
        return mPetWeight.getWeight();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

