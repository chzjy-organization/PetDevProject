package com.punuo.sys.app.weighing;

import android.os.Bundle;
import android.util.Log;


import com.alibaba.android.arouter.facade.annotation.Route;
import com.leplay.petwight.PetWeight;
import com.punuo.sys.app.process.ProcessTasks;
import com.punuo.sys.app.weighing.requset.GetGroupMemberRequest;
import com.punuo.sys.app.weighing.requset.SipGetWeightRequest;
import com.punuo.sys.app.weighing.tool.GroupMemberModel;
import com.punuo.sys.sdk.activity.BaseActivity;
import com.punuo.sys.sdk.httplib.HttpManager;
import com.punuo.sys.sdk.httplib.RequestListener;
import com.punuo.sys.sip.SipDevManager;

import java.util.Timer;
import java.util.TimerTask;

//@Route(path="/app/WeighingActivty")
public class WeighingActivity extends BaseActivity {

    private PetWeight mPetWeight;

    Timer mTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTimer = new Timer();
//        setTimerTask();
        getQuality();
        Log.i("wankui", "getQuality: "+new WeighingActivity().getQuality());
        weightToSipServer();
    }


//    public void setTimerTask(){
//        mTimer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                getQuality();
//                Log.i("测得质量", "质量"+getQuality());
////                weightToSipServer();//TimerTask运行在子线程，不能更新UI、跳转activity
//            }
//        },0,1000*30);
//    }

    /**
     * 根据设备id获取到群组所有的user
     */
    private GetGroupMemberRequest mGetGroupMemberRequest;
    public void getGroupMember(String devId){
        if(mGetGroupMemberRequest != null&& mGetGroupMemberRequest.isFinished){
            return;
        }
        mGetGroupMemberRequest = new GetGroupMemberRequest();
        mGetGroupMemberRequest.addUrlParam("devId", new ProcessTasks().getDevId());
        mGetGroupMemberRequest.setRequestListener(new RequestListener<GroupMemberModel>() {
            @Override
            public void onComplete() {

            }

            @Override
            public void onSuccess(GroupMemberModel result) {
                if(result == null){
                    return;
                }
                if(result.member != null){
//                    for(int i=0;i<result.member.phone.size();i++){
//
//                    }
                    //TODO 目前想法：把所有绑定的设备取出作为参数传递给sip服务器
                }
            }

            @Override
            public void onError(Exception e) {

            }
        });
        HttpManager.addRequest(mGetGroupMemberRequest);
    }


    //将数据发送到Sip服务器
    public static void weightToSipServer(){
        SipGetWeightRequest getWeightRequest = new SipGetWeightRequest(new WeighingActivity().getQuality());
        SipDevManager.getInstance().addRequest(getWeightRequest);
    }

//    private void sipWeightToServer(){
//        SipGetWeightRequest getWeightRequest = new SipGetWeightRequest();
//        getWeightRequest.setSipRequestListener(new SipRequestListener<WeightData>() {
//            @Override
//            public void onComplete() {
//
//            }
//
//            @Override
//            public void onSuccess(WeightData result) {
//                if(result == null){
//                    return;
//                }
//                Log.i("11111", "onSuccess: "+"成功");
//            }
//
//
//            @Override
//            public void onError(Exception e) {
//                HandlerExceptionUtils.handleException(e);
//            }
//        });
//        SipDevManager.getInstance().addRequest(getWeightRequest);
//    }


    public int getQuality(){
        mPetWeight = new PetWeight();
        int quality = mPetWeight.getWeight();

        return quality;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
