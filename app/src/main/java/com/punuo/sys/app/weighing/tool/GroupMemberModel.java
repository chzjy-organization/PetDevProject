package com.punuo.sys.app.weighing.tool;

import com.google.gson.annotations.SerializedName;
import com.punuo.sys.sdk.model.BaseModel;

import java.util.List;

public class GroupMemberModel extends BaseModel {
    @SerializedName("userids")
    public List<Member> members;

    public static class Member extends BaseModel {
        @SerializedName("userid")
        public String userid;
    }
}
