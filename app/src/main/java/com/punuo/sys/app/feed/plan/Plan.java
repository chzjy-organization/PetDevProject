package com.punuo.sys.app.feed.plan;

import com.google.gson.annotations.SerializedName;

public class Plan {
    @SerializedName("time")
    private long planTime;
    @SerializedName("name")
    private String planName;
    @SerializedName("count")
    private String planCount;

    public Plan(long planTime,String planName,String planCount){
        this.planTime = planTime;
        this.planName = planName;
        this.planCount = planCount;
    }

    public long getPlanTime() {
        return planTime;
    }

    public String getPlanName() {
        return planName;
    }

    public String getPlanCount() {
        return planCount;
    }
}
