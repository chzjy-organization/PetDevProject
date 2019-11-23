package com.punuo.sys.sip.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by han.chen.
 * Date on 2019-11-23.
 **/
public class FeedPlan {
    @SerializedName("userName")
    public String userName;
    //设定的时间
    @SerializedName("time")
    public long time;
    //餐名：早餐/午餐
    @SerializedName("name")
    public String name;
    //份数
    @SerializedName("count")
    public int count;
}
