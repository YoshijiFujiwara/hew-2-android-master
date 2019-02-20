package com.hew.second.gathering;

import android.content.SharedPreferences;
import android.support.annotation.Nullable;

public class SearchArgs {
    public static String keyword = "";
    public static String genre = "";
    public static boolean nomi = false;
    public static boolean tabe = false;
    public static boolean course = false;
    public static boolean room = false;
    public static boolean parking = false;
    public static boolean lunch = false;

    public static void clear(){
        keyword = "";
        genre = "";
        nomi = false;
        tabe = false;
        course = false;
        room = false;
        parking = false;
        lunch = false;
    }
}
