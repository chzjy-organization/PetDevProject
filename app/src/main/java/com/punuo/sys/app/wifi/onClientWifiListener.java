package com.punuo.sys.app.wifi;

import android.net.wifi.WifiInfo;

/**
 * Created by han.chen.
 * Date on 2019-08-14.
 **/
public interface OnClientWifiListener {
    void OnClientConnected(WifiInfo wifiInfo, WifiMessage wifiMessage);

    void OnClientConnecting();

    void OnClientDisConnect();
}
