package com.punuo.sys.app.weighing.tool;

import com.google.gson.annotations.SerializedName;
import com.punuo.sys.sdk.model.BaseModel;

import java.util.List;

public class GroupMemberModel extends BaseModel {
    @SerializedName("userids")
    public Member member;

    public static class Member{
        @SerializedName("userid")
        public List<String> userid;
    }
}
