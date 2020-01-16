package com.punuo.sys.app.feed.plan;

import com.punuo.sys.sdk.httplib.BaseRequest;

public class DevGetPlanRequest extends BaseRequest<PlanModel> {

    public DevGetPlanRequest(){
        setRequestPath("/feedplan/getFeedPlanForDev");
        setRequestType(RequestType.GET);
    }
}