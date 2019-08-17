package com.punuo.sys.sip;

import org.zoolu.sip.message.Message;

/**
 * Created by han.chen.
 * Date on 2019-08-12.
 **/
public interface SipMessageProcessor<T> {
    void deliverResponse(Message msg);

    void deliverError(Exception e);
}
