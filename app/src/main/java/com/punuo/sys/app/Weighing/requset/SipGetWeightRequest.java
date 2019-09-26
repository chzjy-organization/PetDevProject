package com.punuo.sys.app.Weighing.requset;

import com.punuo.sys.app.Weighing.tool.WeightData;
import com.punuo.sys.sdk.sercet.SHA1;
import com.punuo.sys.sip.model.RegisterData;
import com.punuo.sys.sip.request.BaseSipRequest;
import com.punuo.sys.sip.request.SipRequestType;

import org.json.JSONObject;

import fr.arnaudguyon.xmltojsonlib.JsonToXml;

public class SipGetWeightRequest extends BaseSipRequest {

    private WeightData mWeightData;


    public SipGetWeightRequest(){
        setSipRequestType(SipRequestType.Notify);
//        mWeightData = data;
    }

    @Override
    public String getBody() {//SHA1类用来加密
        if(mWeightData == null || mWeightData.mWeightResponse == null){
            return null;
        }
        WeightData.WeightResponse response = mWeightData.mWeightResponse;
        String weight = SHA1.getInstance().hashData(response.salt + "");
        weight = SHA1.getInstance().hashData(response.seed + weight);

        JSONObject body = new JSONObject();
        JSONObject value = new JSONObject();

        try {
            value.put("weight",weight);
            body.put("weight_notify",value);
        } catch (Exception e) {
            e.printStackTrace();
        }

        JsonToXml jsonToXml = new JsonToXml.Builder(body).build();
        return jsonToXml.toFormattedString();
    }

}
