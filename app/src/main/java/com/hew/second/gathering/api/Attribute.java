package com.hew.second.gathering.api;

import org.parceler.Parcel;

@Parcel
public class Attribute {
    public int id;
    public User manager;
    public String name;
    public int plus_minus;
    public Date created_at;
    public Date updated_at;
}
