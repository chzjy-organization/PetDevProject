package com.punuo.sys.app.weighing.requset;

import com.punuo.sys.sdk.httplib.BaseRequest;

public class GetGroupMemberRequest extends BaseRequest {

    public GetGroupMemberRequest(){
        setRequestPath("/pets/");
        setRequestType(RequestType.GET);
    }
}
