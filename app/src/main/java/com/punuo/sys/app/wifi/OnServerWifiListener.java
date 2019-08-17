package com.punuo.sys.app.wifi;

import android.net.wifi.WifiInfo;

/**
 * Created by han.chen.
 * Date on 2019-08-14.
 **/
public interface OnServerWifiListener {
    void OnServerConnected(WifiInfo wifiInfo, WifiMessage wifiMessage);

    void OnServerConnecting();

    void OnServerDisConnect();
}
