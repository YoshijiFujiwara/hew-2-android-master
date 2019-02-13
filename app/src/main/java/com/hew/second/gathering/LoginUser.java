package com.hew.second.gathering;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.hew.second.gathering.activities.LoginActivity;
import com.hew.second.gathering.api.MemberInfo;

public class LoginUser {

    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";

    private static int id = -1;
    private static String uniqueId = "";
    private static String email = "";
    private static String password = "";
    private static String token = "";

    public static int getId() {
        return id;
    }

    public static void setId(int id) {
        LoginUser.id = id;
    }

    public static String getPassword(@Nullable SharedPreferences sharedPref) {
        if(sharedPref == null)
            return LoginUser.password;
        LoginUser.password = sharedPref.getString(KEY_PASSWORD, "");
        return LoginUser.password;
    }

    public static String getToken() {
        return "Bearer " + token;
    }

    public static void setToken(String token) {
        LoginUser.token = token;
    }

    public static String getUniqueId() {
        return uniqueId;
    }

    public static void setUniqueId(String uniqueId) {
        LoginUser.uniqueId = uniqueId;
    }

//    public static String getUsername() {
//        return username;
//    }
//
//    public static void setUsername(String username) {
//        LoginUser.username = username;
//    }
//    public static String getUsername(@Nullable SharedPreferences sharedPref) {
//        if(sharedPref == null)
//            return LoginUser.username;
//        LoginUser.username = sharedPref.getString(KEY_USERNAME, "");
//        return LoginUser.username;
//}

    public static String getEmail(@Nullable SharedPreferences sharedPref) {
        if(sharedPref == null)
            return LoginUser.email;
        LoginUser.email = sharedPref.getString(KEY_EMAIL, "");
        return LoginUser.email;
    }

    public static void setUserInfo(SharedPreferences sharedPref, String email, String password, String token) {
        SharedPreferences.Editor editor = sharedPref.edit();
        LoginUser.token = token;
        LoginUser.email = email;
        LoginUser.password = password;
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PASSWORD, password);
        editor.apply();
    }

    public static void deleteUserInfo(SharedPreferences sharedPref) {
        SharedPreferences.Editor editor = sharedPref.edit();
        LoginUser.token = "";
        LoginUser.email = "";
        LoginUser.password = "";
        editor.clear();
        editor.apply();
    }

}
