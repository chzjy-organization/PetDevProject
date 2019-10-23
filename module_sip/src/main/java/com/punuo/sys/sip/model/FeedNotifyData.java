package com.punuo.sys.sip.model;

import com.google.gson.annotations.SerializedName;

public class FeedNotifyData {

    @SerializedName("from")
    public String userName;

    @SerializedName("to")
    public String devId;

    @SerializedName("feed_count")
    public String feedCount;
}