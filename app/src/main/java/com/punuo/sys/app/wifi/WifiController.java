package com.punuo.sys.app.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * Created by han.chen.
 * Date on 2019-08-14.
 **/
public class WifiController {
    private Context mContext;
    private WifiManager mWifiManager;
    private WifiStateReceiver mWifiStateReceiver;
    private WifiMessage mWifiMessage;
    private OnClientWifiListener mClientWifiListener;
    private OnServerWifiListener mServerWifiListener;

    private boolean isClient;

    public void setClientWifiListener(OnClientWifiListener clientWifiListener) {
        mClientWifiListener = clientWifiListener;
    }

    public void setServerWifiListener(OnServerWifiListener serverWifiListener) {
        mServerWifiListener = serverWifiListener;
    }

    public WifiController(Context context, WifiManager wifiManager, boolean isClient) {
        mContext = context;
        mWifiManager = wifiManager;
        this.isClient = isClient;
    }

    public void connectWifi(WifiMessage wifiMessage) {
        if (wifiMessage == null) {
            return;
        }
        mWifiMessage = wifiMessage;
        if (WifiUtil.getConnectWifiBssid().equals(mWifiMessage.BSSID)) {
            mWifiManager.disconnect();
        }
        mWifiStateReceiver = new WifiStateReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        //暂未处理
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mContext.registerReceiver(mWifiStateReceiver, filter);

        WifiConfiguration wifiConfiguration = createWifiConfiguration(mWifiMessage.SSID,
                mWifiMessage.pwd, mWifiMessage.wifiType);
        int netId = mWifiManager.addNetwork(wifiConfiguration);
        boolean result = mWifiManager.enableNetwork(netId, true);
        Log.i("han.chen", "connectWifi: " + result);
    }

    public void unregisterStateReceiver() {
        if (mWifiStateReceiver != null) {
            mContext.unregisterReceiver(mWifiStateReceiver);
        }
    }

    private WifiConfiguration createWifiConfiguration(String SSID, String password, int wifiType) {
        WifiConfiguration configuration = new WifiConfiguration();
        configuration.allowedAuthAlgorithms.clear();
        configuration.allowedGroupCiphers.clear();
        configuration.allowedKeyManagement.clear();
        configuration.allowedPairwiseCiphers.clear();
        configuration.allowedProtocols.clear();
        configuration.SSID = "\"" + SSID + "\"";
        if (wifiType == 0) {
            configuration.wepKeys[0] = "";
            configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            configuration.wepTxKeyIndex = 0;
        } else if (wifiType == 1) {
            configuration.hiddenSSID = false;
            //密码
            configuration.wepKeys[0] = "\"" + password + "\"";
            configuration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.IEEE8021X);
            configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            configuration.status = WifiConfiguration.Status.ENABLED;
        } else if (wifiType == 2) {
            configuration.hiddenSSID = false;
            configuration.preSharedKey = "\"" + password + "\"";
            configuration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            configuration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            configuration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            configuration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.NONE);
            // For WPA
            configuration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            // For WPA2
            configuration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            configuration.status = WifiConfiguration.Status.ENABLED;
        }
        return configuration;
    }

    public class WifiStateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mWifiMessage == null) {
                return;
            }
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                NetworkInfo info = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                if (info != null) {
                    NetworkInfo.State state = info.getState();
                    if (state == NetworkInfo.State.DISCONNECTED) {
                        Log.v("WifiController", "Wifi断开，尝试连接");
                        if (isClient) {
                            mClientWifiListener.OnClientDisConnect();
                        } else {
                            mServerWifiListener.OnServerDisConnect();
                        }
                    } else if (state == NetworkInfo.State.CONNECTED) {
                        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
                        Log.v("WifiController", "成功连接" + wifiInfo.getSSID());
                        if (isClient) {
                            mClientWifiListener.OnClientConnected(wifiInfo, mWifiMessage);
                        } else {
                            mServerWifiListener.OnServerConnected(wifiInfo, mWifiMessage);
                        }
                    } else if (state == NetworkInfo.State.CONNECTING) {
                        Log.v("WifiController", "正在连接。。。");
                        if (isClient) {
                            mClientWifiListener.OnClientConnecting();
                        } else {
                            mServerWifiListener.OnServerConnecting();
                        }
                    }
                }
            }
        }
    }
}
