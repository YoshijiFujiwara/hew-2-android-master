package com.hew.second.gathering.api;

import java.io.Serializable;

/**
 * ユーザーの基本情報
 */
public class User implements Serializable {
    public int id;
    public String unique_id;
    public String username;
    public String email;
    public Date created_at;
    public Date updated_at;
}
