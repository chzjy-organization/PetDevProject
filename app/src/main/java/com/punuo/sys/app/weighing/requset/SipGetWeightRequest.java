package com.punuo.sys.app.weighing.requset;

import com.punuo.sys.app.weighing.WeighingActivity;
import com.punuo.sys.sip.config.SipConfig;
import com.punuo.sys.sip.request.BaseSipRequest;
import com.punuo.sys.sip.request.SipRequestType;

import org.json.JSONException;
import org.json.JSONObject;

import fr.arnaudguyon.xmltojsonlib.JsonToXml;

public class SipGetWeightRequest extends BaseSipRequest {

//    private WeightData mWeightData;
    private String mQuality;
    private WeighingActivity weighingActivity;
    private int userid = 10003;

    public SipGetWeightRequest(String quality){
        setSipRequestType(SipRequestType.Notify);
        setTargetResponse("weight_response");
        mQuality = quality;

    }

    @Override
    public String getBody() {//SHA1类用来加密
//        if (mWeightData == null || mWeightData.mWeightInfo == null) {
//            return null;
//        }
        String devId = SipConfig.getDevId();

        JSONObject body = new JSONObject();
        JSONObject value = new JSONObject();

        try {
            value.put("quality",mQuality);
            value.put("userid",userid);
//            value.put("from",devId);
//            int length = weighingActivity.getGroupMember(devId).size();
//            for(int i=0;i<length;i++){
//                value.put("to",weighingActivity.getGroupMember(devId).get(i));
//            }
            body.put("weight",value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonToXml jsonToXml = new JsonToXml.Builder(body).build();
        return jsonToXml.toFormattedString();
    }

}
