package com.hew.second.gathering.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.R;
import com.hew.second.gathering.activities.LoginActivity;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.JWT;
import com.hew.second.gathering.api.Session;
import com.hew.second.gathering.api.SessionDetail;
import com.hew.second.gathering.api.Util;
import com.hew.second.gathering.views.adapters.EventAdapter;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;


public class MapFragment extends BaseFragment  implements OnMapReadyCallback, GoogleMap.OnCameraIdleListener {
    private static final String MESSAGE = "message";
    private MapView mapView;
    private GoogleMap googleMap;

    public static MapFragment newInstance(String message) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        args.putString(MESSAGE, message);
        fragment.setArguments(args);
        return fragment;
    }

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_map, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        mapView = view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);//when you already implement OnMapReadyCallback in your fragment
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SlidingUpPanelLayout sup = activity.findViewById(R.id.sliding_layout);
        sup.setAnchorPoint(0.5f);

        // activity_main.xmlのlistViewにListViewをセット
        ListView listView = activity.findViewById(R.id.listView_shop_list);
    }
    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        googleMap.setOnCameraIdleListener(this);
    }
    @Override
    public void onCameraIdle() {
        onGetCenter(mapView);
    }

    public void onGetCenter(View view) {
        CameraPosition cameraPos = googleMap.getCameraPosition();
        Toast.makeText(activity, "中心位置\n緯度:" + cameraPos.target.latitude + "\n経度:" + cameraPos.target.longitude, Toast.LENGTH_LONG).show();
    }
}
