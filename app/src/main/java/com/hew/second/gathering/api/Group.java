package com.hew.second.gathering.api;

import org.parceler.Parcel;

import java.io.Serializable;
import java.util.List;

/**
 * 一つ一つのグループの情報
 */
@Parcel
public class Group implements Serializable {
    public Group(){}
    public int id;
    public User manager;
    public String name;
    public List<GroupUser> users;
    public Date created_at;
    public Date updated_at;
}
