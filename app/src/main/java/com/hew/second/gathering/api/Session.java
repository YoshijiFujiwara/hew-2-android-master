package com.hew.second.gathering.api;

import java.util.List;

/**
 * セッションの情報
 */
public class Session {
    public int id;
    public Member manager;
    public List<SessionMember> users;
    public int shop_id; // ぐるなび側から持ってきたidかなにかを入れる予定
    public int budget; // 予算
    public int actual; // 実際にかかった金額
    public Date start_time; // 開始時刻
    public Date end_time; // 終了時刻
    public Date created_at;
    public Date updated_at;
}