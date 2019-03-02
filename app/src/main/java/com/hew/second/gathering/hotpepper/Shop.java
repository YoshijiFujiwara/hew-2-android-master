package com.hew.second.gathering.hotpepper;

import org.parceler.Parcel;

import java.io.Serializable;


@Parcel
public class Shop implements Serializable{
    public Shop(){}
    public String id;
    public String name;
    public String name_kana;
    public String lat;
    public String lng;
    public String logo_image;
    public String address;

    public Genre genre;
    public Url urls;
    public Photo photo;
    public Budget budget;

    public String capacity;
    public String mobile_access;
    public String open;
    public String close;
    public String party_capacity;
    public String course;
    public String free_drink;
    public String free_food;
    public String lunch;
}
