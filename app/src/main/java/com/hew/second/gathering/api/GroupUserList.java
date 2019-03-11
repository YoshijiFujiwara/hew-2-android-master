package com.hew.second.gathering.api;

import org.parceler.Parcel;

import java.io.Serializable;
import java.util.List;

/**
 * グループのメンバーに特有の属性とかはここに持たせる
 * テーブルでいうと、group_userテーブルに特有の属性やな
 */
@Parcel
public class GroupUserList implements Serializable {
    public List<GroupUser> data;
}
