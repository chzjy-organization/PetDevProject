package com.punuo.sys.sip.request;

import com.punuo.sys.sdk.sercet.SHA1;
import com.punuo.sys.sip.model.NegotiateResponse;

import org.json.JSONException;
import org.json.JSONObject;

import fr.arnaudguyon.xmltojsonlib.JsonToXml;

/**
 * Created by han.chen.
 * Date on 2019-08-12.
 * 提交密码
 **/
public class SipDevRegisterRequest extends BaseSipRequest {
    private NegotiateResponse mNegotiateResponse;
    public SipDevRegisterRequest(NegotiateResponse data) {
        setSipRequestType(SipRequestType.Register);
        setTargetResponse("login_response");
        mNegotiateResponse = data;
    }

    @Override
    public String getBody() {
        if (mNegotiateResponse == null) {
            return null;
        }
        String password = SHA1.getInstance().hashData(mNegotiateResponse.salt + "");
        password = SHA1.getInstance().hashData(mNegotiateResponse.seed + password);
        JSONObject body = new JSONObject();
        JSONObject value = new JSONObject();
        try {
            value.put("password", password);
            body.put("login_request", value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonToXml jsonToXml = new JsonToXml.Builder(body).build();
        return jsonToXml.toFormattedString();
    }
}
