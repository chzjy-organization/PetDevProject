package com.punuo.sys.sip.request;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.punuo.sys.sdk.httplib.JsonUtil;
import com.punuo.sys.sip.config.SipConfig;
import com.punuo.sys.sip.SipMessageProcessor;
import com.punuo.sys.sip.message.SipMessageFactory;

import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.message.Message;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import fr.arnaudguyon.xmltojsonlib.XmlToJson;

/**
 * Created by han.chen.
 * Date on 2019-08-12.
 **/
public class BaseSipRequest<T> implements SipMessageProcessor<T> {
    private SipRequestType mSipRequestType;
    private SipRequestListener<T> mSipRequestListener;
    private Message mMessage;
    private int mCode;
    private String mReason;

    public BaseSipRequest() {

    }

    public void setSipRequestListener(SipRequestListener<T> sipRequestListener) {
        mSipRequestListener = sipRequestListener;
    }

    public void setSipRequestType(SipRequestType sipRequestType) {
        mSipRequestType = sipRequestType;
    }


    public void setResponse(Message message, int code, String reason) {
        mMessage = message;
        mCode = code;
        mReason = reason;
    }

    /**
     * 获取远程目标地址
     *
     * @return NameAddress
     */
    public NameAddress getDestNameAddress() {
        return SipConfig.getServerAddress();
    }

    /**
     * 获取本地地址
     *
     * @return NameAddress
     */
    public NameAddress getLocalNameAddress() {
        return SipConfig.getDevNormalAddress();
    }

    /**
     * 获取消息体
     *
     * @return String
     */
    public String getBody() {
        return null;
    }


    @Override
    public void deliverResponse(Message message) {
        String body = message.getBody();
        if (!TextUtils.isEmpty(body)) {
            XmlToJson xmlToJson = new XmlToJson.Builder(body).build();
            String parse = xmlToJson.toString();
            Log.d("SipRequest", "deliverResponse: \n" + parse);
            JsonElement data = null;
            try {
                data = new JsonParser().parse(parse);
                deliverResponse(jsonParse(data, parse), message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void deliverResponse(T data, Message message) {
        try {
            if (mSipRequestListener != null) {
                mSipRequestListener.onSuccess(data);
                mSipRequestListener.onComplete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deliverError(Exception error) {
        try {
            if (mSipRequestListener != null) {
                mSipRequestListener.onError(error);
                mSipRequestListener.onComplete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Message build() {
        switch (mSipRequestType) {
            case Register:
                return SipMessageFactory.createRegisterRequest(getDestNameAddress(), getLocalNameAddress(), getBody());
            case Subscribe:
                return SipMessageFactory.createSubscribeRequest(getDestNameAddress(), getLocalNameAddress(), getBody());
            case Notify:
                return SipMessageFactory.createNotifyRequest(getDestNameAddress(), getLocalNameAddress(), getBody());
            case Invite:
                return SipMessageFactory.createInviteRequest(getDestNameAddress(), getLocalNameAddress(), getBody());
            case Options:
                return SipMessageFactory.createOptionsRequest(getDestNameAddress(), getLocalNameAddress(), getBody());
            case Bye:
                return SipMessageFactory.createByeRequest(getDestNameAddress(), getLocalNameAddress());
            case Response:
                return SipMessageFactory.createResponse(mMessage, mCode, mReason, getBody());
            default:
                return null;
        }
    }

    protected T jsonParse(JsonElement result, String response) {
        if (result == null) {
            return jsonParse(response);
        }
        Type type = getType();
        if (type == String.class) {
            return (T) response;
        }
        return JsonUtil.fromJsonNoCatch(result, getType());
    }

    protected T jsonParse(String result) {
        Type type = getType();
        if (type == String.class) {
            return (T) result;
        }
        return JsonUtil.fromJson(result, getType());
    }

    public Type getType() {
        Type superclass = getClass().getGenericSuperclass();
        if (superclass instanceof Class) {
            throw new RuntimeException("Missing type parameter.");
        }
        ParameterizedType parameterized = (ParameterizedType) superclass;
        return parameterized.getActualTypeArguments()[0];
    }
}
