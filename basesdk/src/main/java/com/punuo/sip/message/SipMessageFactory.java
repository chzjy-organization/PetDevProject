package com.punuo.sip.message;

import android.text.TextUtils;

import com.punuo.sip.SipUserManager;

import org.zoolu.sip.address.NameAddress;
import org.zoolu.sip.address.SipURL;
import org.zoolu.sip.message.Message;
import org.zoolu.sip.message.MessageFactory;
import org.zoolu.sip.message.SipMethods;
import org.zoolu.sip.provider.SipProvider;

/**
 * Author chzjy
 * Date 2016/12/19.
 */

public class SipMessageFactory extends MessageFactory {
    private static SipURL sipURL;
    private static final String CONTENT_TYPE = "application/xml";
    private static String viaAddress;
    private static int hostPort;
    private static NameAddress contact;

    public static void init() {
        viaAddress = SipUserManager.getInstance().getViaAddress();
        hostPort = SipUserManager.getInstance().getPort();
        sipURL = new SipURL(viaAddress, hostPort);
        contact = new NameAddress(new SipURL(viaAddress, hostPort));
    }

    public static Message createRegisterRequest(NameAddress to, NameAddress from, String body) {
        Message message = createRegisterRequest(SipUserManager.getInstance(), sipURL, to, from, contact);
        if (!TextUtils.isEmpty(body)) {
            message.setBody(CONTENT_TYPE, body);
        }
        return message;
    }

    public static Message createNotifyRequest(NameAddress to, NameAddress from, String body) {
        Message msg = createRequest(SipUserManager.getInstance(), SipMethods.NOTIFY, sipURL,
                to, from, contact, null);
        msg.setBody(CONTENT_TYPE, body);
        return msg;
    }

    private static Message createSubscribeRequest(SipProvider sip_provider,  //设备列表
                                                  NameAddress to, NameAddress from) {
        String via_addr = SipUserManager.getInstance().getViaAddress();
        int host_port = SipUserManager.getInstance().getPort();
        SipURL sipURL = new SipURL(via_addr, host_port);
        NameAddress contact = new NameAddress(sipURL);
        return createRequest(sip_provider, SipMethods.SUBSCRIBE, sipURL, to, from, contact, null);
    }

    public static Message createSubscribeRequest(SipProvider sip_provider,  //设备列表
                                                 NameAddress to, NameAddress from, String body) {
        Message msg = createSubscribeRequest(sip_provider, to, from);
        msg.setBody(CONTENT_TYPE, body);
        return msg;
    }

    public static Message createOptionsRequest(SipProvider sip_provider,  //视频信息查询
                                               NameAddress to, NameAddress from, String body) {
        String via_addr = sip_provider.getViaAddress();
        int host_port = sip_provider.getPort();
        SipURL sipURL = new SipURL(via_addr, host_port);
        NameAddress contact = new NameAddress(sipURL);
        Message msg = createRequest(sip_provider, SipMethods.OPTIONS, sipURL, to, from, contact, null);
        msg.setBody(CONTENT_TYPE, body);
        return msg;
    }

    public static Message createInviteRequest(SipProvider sip_provider,  //实时视频
                                              NameAddress to, NameAddress from, String body) {
        String via_addr = sip_provider.getViaAddress();
        int host_port = sip_provider.getPort();
        SipURL sipURL = new SipURL(via_addr, host_port);
        NameAddress contact = new NameAddress(sipURL);
        Message msg = createInviteRequest(sip_provider, sipURL, to, from, contact, null);
        msg.setBody(CONTENT_TYPE, body);
        return msg;
    }

    public static Message createByeRequest(SipProvider sip_provider,  //结束实时视频
                                           NameAddress to, NameAddress from) {
        String via_addr = sip_provider.getViaAddress();
        int host_port = sip_provider.getPort();
        SipURL sipURL = new SipURL(via_addr, host_port);
        NameAddress contact = new NameAddress(sipURL);
        return createRequest(sip_provider, SipMethods.BYE, sipURL, to, from, contact, null);
    }

    public static Message createResponse(Message req, int code, String reason, String body) {
        return createResponse(req, code, reason, null, null, CONTENT_TYPE, body);
    }
}
