package com.hew.second.gathering;

import android.content.SharedPreferences;

public class SelectedSession {
    private static final String KEY_SESSION_ID = "session";

    public static void setSessionId(SharedPreferences sharedPref, int id) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(KEY_SESSION_ID, id);
        editor.apply();
    }

    public static int getSharedSessionId(SharedPreferences sharedPref) {
        return sharedPref.getInt(KEY_SESSION_ID, -1);
    }

    public void deleteSessionInfo(SharedPreferences sharedPref) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(KEY_SESSION_ID);
        editor.commit();
    }
}
