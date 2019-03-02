package com.hew.second.gathering.hotpepper;


import org.parceler.Parcel;

import java.io.Serializable;

@Parcel
public class Budget implements Serializable {
    public Budget(){}
    public String code;
    public String name;
    public String average;
}