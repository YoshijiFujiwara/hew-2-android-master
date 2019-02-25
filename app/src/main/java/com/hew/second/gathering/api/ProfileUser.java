package com.hew.second.gathering.api;

import org.parceler.Parcel;

/**
 * セッションに参加しているメンバーに特有の属性とかはここに持たせる
 * テーブルでいうと、session_userテーブルに特有の属性やな
 */
@Parcel
public class ProfileUser extends Profile{
    public String unique_id;
    public String username;
    public String password;
}