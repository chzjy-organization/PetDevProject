package com.punuo.sys.sip.config;

import org.zoolu.sip.address.NameAddress;

/**
 * Created by han.chen.
 * Date on 2019/4/23.
 **/
public interface ISipConfig {

    String getServerIp();

    int getPort();

    String getDevId();

    NameAddress getServerAddress();

    NameAddress getDevNormalAddress();
}
