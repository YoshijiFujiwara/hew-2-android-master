package com.hew.second.gathering.api;

import org.parceler.Parcel;

@Parcel
public class Attribute {
    public Integer id;
    public User manager;
    public String name;
    public Integer plus_minus;
    public Date created_at;
    public Date updated_at;
}
