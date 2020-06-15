package com.punuo.sys.sip.service;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.punuo.sys.sdk.util.HandlerExceptionUtils;
import com.punuo.sys.sip.event.StartVideoEvent;
import com.punuo.sys.sip.model.MediaData;
import com.punuo.sys.sip.request.BaseSipRequest;
import com.punuo.sys.sip.video.H264Config;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;
import org.zoolu.sip.message.Message;

import fr.arnaudguyon.xmltojsonlib.JsonToXml;

/**
 * Created by han.chen.
 * Date on 2020/6/14.
 **/
@Route(path = ServicePath.PATH_MEDIA)
public class MediaService extends NormalRequestService<MediaData> {
    @Override
    protected String getBody() {
        JSONObject body = new JSONObject();
        JSONObject value = new JSONObject();
        try {
            value.put("resolution", "CIF");
            value.put("video", "H.264");
            value.put("audio", "G.711");
            value.put("kbps", "800");
            value.put("self", "192.168.1.129 UDP 5200");
            value.put("mode", "active");
            value.put("magic", "01234567890123456789012345678901");
            value.put("dev_type", 2);
            body.put("media", value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonToXml jsonToXml = new JsonToXml.Builder(body).build();
        return jsonToXml.toFormattedString();
    }

    @Override
    protected void onSuccess(Message msg, MediaData result) {
        onResponse(msg);
        //初始化视频通道数据
        H264Config.initMediaData(result);
        H264Config.videoNum++;
        EventBus.getDefault().post(new StartVideoEvent());
    }

    @Override
    protected void onError(Exception e) {
        HandlerExceptionUtils.handleException(e);
    }

    @Override
    public void handleTimeOut(BaseSipRequest baseSipRequest) {

    }
}
