package com.hew.second.gathering.api;

import java.util.List;

public class GroupInfo {
    public int id;
    public MemberInfo manager;
    public String name;
    public List<MemberInfo> users;
    public DateInfo created_at;
    public DateInfo updated_at;
}
