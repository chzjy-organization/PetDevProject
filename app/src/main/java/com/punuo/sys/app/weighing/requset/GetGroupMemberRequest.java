package com.punuo.sys.app.weighing.requset;

import com.punuo.sys.app.weighing.tool.GroupMemberModel;
import com.punuo.sys.sdk.httplib.BaseRequest;

public class GetGroupMemberRequest extends BaseRequest<GroupMemberModel> {

    public GetGroupMemberRequest(){
        setRequestPath("/groupmembers/getGroupMembersForSip");
        setRequestType(RequestType.GET);
    }
}
