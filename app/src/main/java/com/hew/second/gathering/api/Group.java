package com.hew.second.gathering.api;

import java.util.List;

/**
 * 一つ一つのグループの情報
 */
public class Group {
    public int id;
    public User manager;
    public String name;
    public List<GroupUser> users;
    public Date created_at;
    public Date updated_at;
}
