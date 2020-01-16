package com.punuo.sys.app.weighing.requset;

import com.punuo.sys.sdk.httplib.BaseRequest;
import com.punuo.sys.sdk.model.BaseModel;

public class WeightDataToServerRequest extends BaseRequest<BaseModel> {
    public WeightDataToServerRequest(){
        setRequestPath("/weightshistogram/addWeightsHistogram");
        setRequestType(RequestType.POST);
    }
}
