package com.hew.second.gathering.api;

/**
 * セッションに参加しているメンバーに特有の属性とかはここに持たせる
 * テーブルでいうと、session_userテーブルに特有の属性やな
 */
public class SessionUser extends User {
    public String join_status; // 参加状況
    public int paid; // 支払い済みか
    public int plus_minus; // 支払い金額のplus_minus
    public float ratio; // 割合
}