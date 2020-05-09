package com.punuo.sys.app.feed.plan;

import com.google.gson.annotations.SerializedName;
import com.punuo.sys.sdk.model.BaseModel;
import com.punuo.sys.sip.model.FeedPlan;

import java.util.List;

public class PlanModel extends BaseModel {
    @SerializedName("feedplans")
    public List<FeedPlan> mPlanList;

}
