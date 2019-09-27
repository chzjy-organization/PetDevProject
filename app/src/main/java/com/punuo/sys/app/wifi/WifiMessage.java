package com.punuo.sys.app.wifi;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

/**
 * Created by han.chen.
 * Date on 2019-08-14.
 **/
public class WifiMessage implements Parcelable {
    public int wifiType;
    public String pwd;
    public String BSSID;
    public String SSID;

    public WifiMessage() {

    }

    protected WifiMessage(Parcel in) {
        wifiType = in.readInt();
        pwd = in.readString();
        BSSID = in.readString();
        SSID = in.readString();
    }

    public static final Creator<WifiMessage> CREATOR = new Creator<WifiMessage>() {
        @Override
        public WifiMessage createFromParcel(Parcel in) {
            return new WifiMessage(in);
        }

        @Override
        public WifiMessage[] newArray(int size) {
            return new WifiMessage[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(wifiType);
        dest.writeString(pwd);
        dest.writeString(BSSID);
        dest.writeString(SSID);
    }

    @NonNull
    @Override
    public String toString() {
        return "WifiMessage{" +
                "wifiType=" + wifiType +
                ", pwd='" + pwd + '\'' +
                ", BSSID='" + BSSID + '\'' +
                ", SSID='" + SSID + '\'' +
                '}';
    }
}
