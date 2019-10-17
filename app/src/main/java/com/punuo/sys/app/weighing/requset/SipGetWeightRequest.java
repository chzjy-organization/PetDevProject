package com.punuo.sys.app.weighing.requset;

import com.punuo.sys.app.weighing.WeighingActivity;
import com.punuo.sys.app.weighing.tool.GroupMemberModel;
import com.punuo.sys.sip.config.SipConfig;
import com.punuo.sys.sip.request.BaseSipRequest;
import com.punuo.sys.sip.request.SipRequestType;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import fr.arnaudguyon.xmltojsonlib.JsonToXml;

public class SipGetWeightRequest extends BaseSipRequest {

//    private WeightData mWeightData;
    private String mQuality;
    private StringBuilder mUserTo;
    private WeighingActivity weighingActivity;

    public SipGetWeightRequest(String quality, List<GroupMemberModel.Member> members){
        setSipRequestType(SipRequestType.Notify);
        setTargetResponse("weight_response");
        mQuality = quality;
        mUserTo = new StringBuilder();
        for (int i = 0; i < members.size(); i++) {
            mUserTo.append(members.get(i).userid);
            if (i != members.size() - 1) {
                mUserTo.append(",");
            }
        }
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
            value.put("quality", mQuality);
            value.put("from", devId);
            value.put("to", mUserTo);
            body.put("weight", value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonToXml jsonToXml = new JsonToXml.Builder(body).build();
        return jsonToXml.toFormattedString();
    }

}
