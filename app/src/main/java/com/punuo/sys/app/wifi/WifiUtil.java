package com.punuo.sys.app.wifi;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.punuo.sys.sdk.PnApplication;

import static android.content.Context.WIFI_SERVICE;

public class WifiUtil {
    /**
     * 获取当前连接的WIFI信息
     *
     * @return
     */
    public static WifiInfo getConnectWifiInfo() {
        Context context = PnApplication.getInstance().getApplicationContext();
        WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        return wifiManager.getConnectionInfo();
    }

    /**
     * 获取当前连接的WIFI信息
     *
     * @param wifiManager
     * @return
     */
    public static WifiInfo getConnectWifiInfo(WifiManager wifiManager) {
        return wifiManager.getConnectionInfo();
    }

    /**
     * 获取当前连接的WIFI信息的BSSID
     *
     * @return
     */
    public static String getConnectWifiBssid() {
        Context context = PnApplication.getInstance().getApplicationContext();
        WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo.getBSSID();
    }

    /**
     * 获取当前连接的WIFI信息的SSID
     *
     * @param wifiManager
     * @return
     */
    public static String getConnectWifiBssid(WifiManager wifiManager) {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo.getBSSID();
    }

    /**
     * Wifi是否打开
     *
     * @param wifiManager
     * @return
     */
    public static Boolean isWifiEnable(WifiManager wifiManager) {
        return wifiManager.isWifiEnabled();
    }
}
