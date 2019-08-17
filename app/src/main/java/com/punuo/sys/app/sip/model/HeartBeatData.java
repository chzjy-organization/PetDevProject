package com.punuo.sys.app.sip.model;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

/**
 * Created by han.chen.
 * Date on 2019-08-12.
 **/
public class HeartBeatData {
    @SerializedName("login_response")
    public JsonElement mLoginResponse;
}
