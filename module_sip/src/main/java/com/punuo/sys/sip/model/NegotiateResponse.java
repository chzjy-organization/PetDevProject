package com.punuo.sys.sip.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by han.chen.
 * Date on 2019-08-12.
 **/
public class NegotiateResponse {

    @SerializedName("salt")
    public String salt;
    @SerializedName("seed")
    public String seed;
}
