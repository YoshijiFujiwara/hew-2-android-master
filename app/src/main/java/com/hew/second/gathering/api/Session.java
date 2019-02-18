package com.hew.second.gathering.api;

import java.io.Serializable;
import java.util.List;

/**
 * セッションの情報
 */
public class Session implements Serializable {
    public int id;
    public String name;
    public int shop_id; // ぐるなび側から持ってきたidかなにかを入れる予定
    public int budget; // 予算
    public int actual; // 実際にかかった金額
    public String start_time; // 開始時刻
    public String end_time; // 終了時刻
    public User manager;
    public List<SessionUser> users;
    public Date created_at;
    public Date updated_at;
}