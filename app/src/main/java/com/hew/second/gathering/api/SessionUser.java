package com.hew.second.gathering.api;

import org.parceler.Parcel;

/**
 * セッションに参加しているメンバーに特有の属性とかはここに持たせる
 * テーブルでいうと、session_userテーブルに特有の属性やな
 */
@Parcel
public class SessionUser extends User{
    public String join_status; // 参加状況
    public int paid; // 支払い済みか
    public int plus_minus; // 支払い金額のplus_minus
    public String attribute_name; // 支払い金額のplus_minus
}