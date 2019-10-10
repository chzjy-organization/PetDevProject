package com.punuo.sys.sip.model;

import com.google.gson.annotations.SerializedName;

public class WeightData {
    @SerializedName("weight_response")
    public WeightInfo mWeightInfo;

    public static class WeightInfo{

        @SerializedName("quality")
        public int quality;
    }
}
