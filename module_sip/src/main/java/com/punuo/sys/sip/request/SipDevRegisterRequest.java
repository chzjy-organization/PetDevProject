package com.punuo.sys.sip.request;

import com.punuo.sys.sip.model.RegisterData;
import com.punuo.sys.sdk.sercet.SHA1;

import org.json.JSONException;
import org.json.JSONObject;

import fr.arnaudguyon.xmltojsonlib.JsonToXml;

/**
 * Created by han.chen.
 * Date on 2019-08-12.
 * 提交密码
 **/
public class SipDevRegisterRequest extends BaseSipRequest<Object> {
    private RegisterData mRegisterData;
    public SipDevRegisterRequest(RegisterData data) {
        setSipRequestType(SipRequestType.Register);
        mRegisterData = data;
    }

    @Override
    public String getBody() {
        if (mRegisterData == null || mRegisterData.mNegotiateResponse == null) {
            return null;
        }
        RegisterData.NegotiateResponse response = mRegisterData.mNegotiateResponse;
        String password = SHA1.getInstance().hashData(response.salt + "");
        password = SHA1.getInstance().hashData(response.seed + password);
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
