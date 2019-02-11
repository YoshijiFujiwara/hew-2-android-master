package com.example.eventer.member.api;

import java.util.List;

public class GroupDetail {
    public GroupDetailInfo data;

    public class GroupDetailInfo{
        public int id;
        public MemberInfo manager;
        public String name;
        public List<MemberInfo> users;
        public DateInfo created_at;
        public DateInfo updated_at;
    }
}
