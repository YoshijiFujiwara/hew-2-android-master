package com.hew.second.gathering.api;

import org.parceler.Parcel;

import java.io.Serializable;
import java.sql.Time;

/**
 * デフォルト設定情報
 */
@Parcel
public class DefaultSetting implements Serializable{
    public DefaultSetting(){}
    public int id;
    public User manager; // このデフォルト設定を管理するひと
    public String name; // このデフォルト設定の名前
    public String timer; // Time型かなにかを入れたいから、型を間違えている気がします（'01:00;00'などをいれれたら良い）
    public String current_location_flag;
    public String latitude;
    public String longitude;
    public Group group;
    public Date created_at;
    public Date updated_at;
}
