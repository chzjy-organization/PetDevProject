package com.punuo.sys.sip;

import org.zoolu.sip.address.NameAddress;

/**
 * Created by han.chen.
 * Date on 2019-08-12.
 **/
public class SipConfig {

    private static ISipConfig sSipConfig;
    //服务器ip
    private static String host = "101.69.255.134";
    //端口号
    private static int port = 6060;
    //服务器Id
    public static String SERVER_ID = "330100000010000090";
    //用户注册获取用户ID使用
    public static String REGISTER_ID = "330100000010000190";
    //服务器名
    public static String SERVER_NAME = "rvsup";

    public static void init(ISipConfig sipConfig) {
        sSipConfig = sipConfig;
    }

    public static String getServerIp() {
        if (sSipConfig != null) {
            return sSipConfig.getServerIp();
        }
        return host;
    }

    public static int getPort() {
        if (sSipConfig != null) {
            return sSipConfig.getPort();
        }
        return port;
    }

    public static NameAddress getDevRegisterAddress() {
        if (sSipConfig != null) {
            return sSipConfig.getDevRegisterAddress();
        } else {
            throw new RuntimeException("RegisterNameAddress is null, please set RegisterNameAddress");
        }
    }

    public static NameAddress getServerAddress() {
        if (sSipConfig != null) {
            return sSipConfig.getServerAddress();
        } else {
            throw new RuntimeException("ServerNameAddress is null, please set ServerNameAddress");
        }
    }

    public static NameAddress getDevNormalAddress() {
        if (sSipConfig != null) {
            return sSipConfig.getDevNormalAddress();
        } else {
            throw new RuntimeException("NormalNameAddress is null, please set NormalNameAddress");
        }
    }
}
