package com.hew.second.gathering.api;

import org.parceler.Parcel;

import java.io.Serializable;

/**
 * グループのメンバーに特有の属性とかはここに持たせる
 * テーブルでいうと、group_userテーブルに特有の属性やな
 */
@Parcel
public class GroupUser extends User implements Serializable {
    public GroupUser() {}
}
