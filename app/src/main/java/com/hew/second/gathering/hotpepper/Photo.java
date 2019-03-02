package com.hew.second.gathering.hotpepper;


import org.parceler.Parcel;

import java.io.Serializable;

@Parcel
public class Photo implements Serializable {
    public Photo(){}
    public PhotoDetail pc;
    public PhotoDetail mobile;

}