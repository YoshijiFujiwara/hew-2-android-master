package com.hew.second.gathering;

import android.content.SharedPreferences;

public class SelectedSession {
    private static final String KEY_SESSION_ID = "session";

    private static int id = -1;

    public static int getId() {
        return id;
    }

    public static void setId(int id) {
        SelectedSession.id = id;
    }

    public static void setSessionId(SharedPreferences sharedPref, int id) {
        SharedPreferences.Editor editor = sharedPref.edit();
        SelectedSession.id = id;
        editor.putInt(KEY_SESSION_ID, id);
        editor.apply();
    }

    public static void deleteSessionInfo(SharedPreferences sharedPref) {
        SharedPreferences.Editor editor = sharedPref.edit();
        SelectedSession.id = -1;
        editor.apply();
    }
}
