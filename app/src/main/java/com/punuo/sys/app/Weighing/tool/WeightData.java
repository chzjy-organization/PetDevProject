package com.punuo.sys.app.Weighing.tool;

import com.google.gson.annotations.SerializedName;

public class WeightData {
    @SerializedName("weight")
    public WeightResponse mWeightResponse;

    public static class WeightResponse{

        /**
         * salt : 8b1fa
         * seed : 06aaeb48bccd11e9824b00163e1390bd
         */

        @SerializedName("salt")
        public String salt;
        @SerializedName("seed")
        public String seed;
        @SerializedName("quality")
        public int quality;
    }
}
