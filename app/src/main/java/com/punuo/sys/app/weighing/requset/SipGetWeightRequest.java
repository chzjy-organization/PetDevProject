package com.punuo.sys.app.weighing.requset;

import com.punuo.sys.sip.request.BaseSipRequest;
import com.punuo.sys.sip.request.SipRequestType;

import org.json.JSONException;
import org.json.JSONObject;

import fr.arnaudguyon.xmltojsonlib.JsonToXml;

public class SipGetWeightRequest extends BaseSipRequest {

//    private WeightData mWeightData;
    private int mQuality;


    public SipGetWeightRequest(int quality){
        setSipRequestType(SipRequestType.Notify);
        setTargetResponse("weight_response");
        mQuality = quality;
    }

    @Override
    public String getBody() {//SHA1类用来加密
//        if (mWeightData == null || mWeightData.mWeightInfo == null) {
//            return null;
//        }

        JSONObject body = new JSONObject();
        JSONObject value = new JSONObject();

        try {
            value.put("quality",mQuality);
            body.put("weight",value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonToXml jsonToXml = new JsonToXml.Builder(body).build();
        return jsonToXml.toFormattedString();
    }

}
