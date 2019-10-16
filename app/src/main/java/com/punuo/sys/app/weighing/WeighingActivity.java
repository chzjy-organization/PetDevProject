package com.punuo.sys.app.weighing;

import android.os.Bundle;
import android.util.Log;

import com.leplay.petwight.PetWeight;
import com.punuo.sys.app.weighing.requset.GetGroupMemberRequest;
import com.punuo.sys.app.weighing.requset.SipGetWeightRequest;
import com.punuo.sys.app.weighing.tool.GroupMemberModel;
import com.punuo.sys.sdk.activity.BaseActivity;
import com.punuo.sys.sdk.httplib.HttpManager;
import com.punuo.sys.sdk.httplib.RequestListener;
import com.punuo.sys.sip.SipDevManager;
import com.punuo.sys.sip.config.SipConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

//@Route(path="/app/WeighingActivty")
public class WeighingActivity extends BaseActivity {

    private com.leplay.petwight.PetWeight mPetWeight;

    Timer mTimer;

    private List<String> mMembers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTimer = new Timer();
//        setTimerTask();
        getQuality();
        Log.i("wankui", "getQuality: "+new WeighingActivity().getQuality());
        weightToSipServer();
        getGroupMember(SipConfig.getDevId());
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
    public  void getGroupMember(String devId){
        if (mGetGroupMemberRequest != null && !mGetGroupMemberRequest.isFinish()) {
            return;
        }
        mGetGroupMemberRequest = new GetGroupMemberRequest();
        mGetGroupMemberRequest.addUrlParam("devid", devId);
        mGetGroupMemberRequest.setRequestListener(new RequestListener<GroupMemberModel>() {
            @Override
            public void onComplete() {
                Log.i("", "getGroupMember: " + mMembers);
            }

            @Override
            public void onSuccess(GroupMemberModel result) {
                if (result == null) {
                    return;
                }
                mMembers = result.members;
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



    public String getQuality(){
        mPetWeight = new PetWeight();
        String quality = mPetWeight.getWeight()+"";

        return quality;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

