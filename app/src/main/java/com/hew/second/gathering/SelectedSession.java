package com.hew.second.gathering;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.hew.second.gathering.api.Session;

public class SelectedSession {
    private static final String KEY_SESSION_ID = "session";
    private static final String KEY_SESSION_DETAIL = "session_detail";

    public static void setSessionId(SharedPreferences sharedPref, int id) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(KEY_SESSION_ID, id);
        editor.apply();
    }

    /**
     * セッション詳細情報を、json形式で保存する
     * 予算フラグメント間の値の受け渡しが、非常に億劫だったため
     * @param sharedPref
     * @param session
     */
    public static void setSessionDetail(SharedPreferences sharedPref, Session session) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(KEY_SESSION_DETAIL);
        Gson gson = new Gson();
        String sessionJson = gson.toJson(session);
        editor.putString(KEY_SESSION_DETAIL, sessionJson);
        editor.apply();
    }

    public static int getSharedSessionId(SharedPreferences sharedPref) {
        return sharedPref.getInt(KEY_SESSION_ID, -1);
    }

    public static Session getSessionDetail(SharedPreferences sharedPref) {

        Gson gson = new Gson();
        String json = sharedPref.getString(KEY_SESSION_DETAIL, "");
        Log.v("SESSIONJSON", json);
        Session obj = gson.fromJson(json, Session.class);
        return obj;
    }

    public void deleteSessionInfo(SharedPreferences sharedPref) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(KEY_SESSION_ID);
        editor.commit();
    }
}
