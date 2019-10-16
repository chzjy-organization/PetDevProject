package com.punuo.sys.app.weighing.tool;

import com.google.gson.annotations.SerializedName;
import com.punuo.sys.sdk.model.BaseModel;

import java.util.List;

public class GroupMemberModel extends BaseModel {
    @SerializedName("member")
    public Member member;

    public static class Member{
        @SerializedName("phone")
        public List<String> phone;
    }
}
