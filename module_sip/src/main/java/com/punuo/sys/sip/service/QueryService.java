package com.punuo.sys.sip.service;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.punuo.sys.sdk.util.HandlerExceptionUtils;
import com.punuo.sys.sip.model.QueryData;
import com.punuo.sys.sip.request.BaseSipRequest;

import org.json.JSONException;
import org.json.JSONObject;
import org.zoolu.sip.message.Message;

import fr.arnaudguyon.xmltojsonlib.JsonToXml;

/**
 * Created by han.chen.
 * Date on 2020/6/14.
 **/
@Route(path = ServicePath.PATH_QUERY)
public class QueryService extends NormalRequestService<QueryData> {
    @Override
    protected String getBody() {
        JSONObject body = new JSONObject();
        JSONObject value = new JSONObject();
        try {
            value.put("variable", "MediaInfo_Video");
            value.put("result", "0");
            value.put("video", "H.264");
            value.put("resolution", "CIF");
            value.put("framerate", "25");
            value.put("bitrate", "256");
            value.put("bright", "51");
            value.put("contrast", "49");
            value.put("saturation", "50");
            body.put("query_response", value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonToXml jsonToXml = new JsonToXml.Builder(body).build();
        return jsonToXml.toFormattedString();
    }

    @Override
    protected void onSuccess(Message msg, QueryData result) {
        onResponse(msg);
    }

    @Override
    protected void onError(Exception e) {
        HandlerExceptionUtils.handleException(e);
    }

    @Override
    public void handleTimeOut(BaseSipRequest baseSipRequest) {

    }
}
