package com.punuo.sys.app.feed.plan;

import com.google.gson.annotations.SerializedName;
import com.punuo.sys.sip.model.FeedPlan;

import java.util.List;

public class PlanModel {
    @SerializedName("feedplans")
    public List<FeedPlan> mPlanList;

}
