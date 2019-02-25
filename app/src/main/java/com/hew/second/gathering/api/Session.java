package com.hew.second.gathering.api;

import org.parceler.Parcel;

import java.io.Serializable;
import java.util.List;

/**
 * セッションの情報
 */
@Parcel
public class Session implements Serializable {
    public int id;
    public String name;
    public String shop_id; // ぐるなび側から持ってきたidかなにかを入れる予定
    public int manager_plus_minus; // このセッションの管理者の予算の増減費
    public int budget; // 予算
    public int actual; // 実際にかかった金額
    public String start_time; // 開始時刻
    public String end_time; // 終了時刻
    public User manager;
    public List<SessionUser> users;
    public Date created_at;
    public Date updated_at;
}