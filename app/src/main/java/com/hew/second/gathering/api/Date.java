package com.hew.second.gathering.api;

import org.parceler.Parcel;

import java.io.Serializable;

/**
 * 日時
 */
@Parcel
public class Date implements Serializable {
    public String date;
    public int timezone_type;
    public String timezone;
}
