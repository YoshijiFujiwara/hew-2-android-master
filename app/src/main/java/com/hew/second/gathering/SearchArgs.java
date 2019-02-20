package com.hew.second.gathering;

import android.content.SharedPreferences;
import android.location.Location;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

public class SearchArgs {
    // hotPepper
    public static String keyword = "";
    public static String genre = null;
    public static boolean nomi = false;
    public static boolean tabe = false;
    public static boolean course = false;
    public static boolean room = false;
    public static boolean parking = false;
    public static boolean lunch = false;
    public static Integer range = 3;

    public static ArrayList<Integer> rangeList = new ArrayList<>(Arrays.asList(0, 300, 500, 1000, 2000, 3000));

    //map
    public static float lat = 0.0f;
    public static float lng = 0.0f;

    public static void clear() {
        keyword = "";
        genre = "";
        nomi = false;
        tabe = false;
        course = false;
        room = false;
        parking = false;
        lunch = false;
        range = 3;
    }

    /*
     * 2点間の距離（メートル）、方位角（始点、終点）を取得
     * ※配列で返す[距離、始点から見た方位角、終点から見た方位角]
     */
    public static float[] getDistance(double latitude1, double longitude1, double latitude2, double longitude2) {
        // 結果を格納するための配列を生成
        float[] results = new float[3];

        // 距離計算
        Location.distanceBetween(latitude1, longitude1, latitude2, longitude2, results);

        return results;
    }
}
