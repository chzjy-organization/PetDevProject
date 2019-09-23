package com.punuo.sys.sip.service;

import android.util.Log;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.punuo.sys.sdk.util.HandlerExceptionUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.zoolu.sip.message.Message;

import fr.arnaudguyon.xmltojsonlib.JsonToXml;

/**
 * Created by han.chen.
 * Date on 2019-08-20.
 **/
@Route(path = ServicePath.PATH_QUERY)
public class QueryService extends NormalRequestService<String> {

    @Override
    protected String getBody() {
        JSONObject body = new JSONObject();
        JSONObject value = new JSONObject();
        try {
            value.put("variable", "MediaInfo_Video");
            value.put("result", 0);
            value.put("video", "H.264");
            value.put("resolution", "Feed_Device");
            value.put("framerate", 25);
            value.put("bitrate", 256);
            value.put("bright", 51);
            value.put("contrast", 49);
            value.put("saturation", 50);
            body.put("query_response", value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonToXml jsonToXml = new JsonToXml.Builder(body).build();
        return jsonToXml.toFormattedString();
    }

    @Override
    protected void onSuccess(Message msg, String result) {
        Log.d("han.chen", "Query 收到视频请求");
        onResponse(msg);
    }

    @Override
    protected void onError(Exception e) {
        HandlerExceptionUtils.handleException(e);
    }
}
