package com.punuo.sys.app.weighing.tool;

import com.google.gson.annotations.SerializedName;
import com.punuo.sys.sdk.model.BaseModel;

import java.util.List;

public class GroupMemberModel extends BaseModel {
    @SerializedName("groupmember")
    public Member member;

    public static class Member{
        @SerializedName("member")
        public List<String> phone;
    }
}
