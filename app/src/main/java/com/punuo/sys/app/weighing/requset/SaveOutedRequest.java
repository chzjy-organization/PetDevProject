package com.punuo.sys.app.weighing.requset;

import com.punuo.sys.sdk.httplib.BaseRequest;
import com.punuo.sys.sdk.model.BaseModel;

public class SaveOutedRequest extends BaseRequest<BaseModel>{
    public SaveOutedRequest(){
        setRequestPath("/totalfeedamount/addTotalFeedAmount");
        setRequestType(RequestType.POST);
    }
}
