package com.hew.second.gathering.hotpepper;

public class Shop {
    public String id;
    public String name;
    public float lat;
    public float lng;
    public String logo_image;
    public String address;
    public Genre genre;
    public Url urls;
    public Photo photo;

    public class Url{
        public String pc;
    }
    public class Photo{
        public PhotoDetail pc;
        public PhotoDetail mobile;
        public class PhotoDetail{
            public String l;
            public String s;
        }
    }

    public String open;
    public String close;
    public int party_capacity;
    public String course;
    public String free_drink;
    public String free_food;
    public String lunch;
}
