package com.hew.second.gathering.hotpepper;

import org.parceler.Parcel;

@Parcel
public class Shop {
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

    @Parcel
    public static class Budget{
        public Budget(){}
        public String code;
        public String name;
        public String average;
    }

    @Parcel
    public static class Url{
        public Url(){}
        public String pc;
    }

    @Parcel
    public static class Photo{
        public Photo(){}
        public PhotoDetail pc;
        public PhotoDetail mobile;
        @Parcel
        public static class PhotoDetail{
            public PhotoDetail(){}
            public String l;
            public String s;
        }
    }

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
