package com.hew.second.gathering.fragments;


import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.R;
import com.hew.second.gathering.SearchArgs;
import com.hew.second.gathering.activities.LoginActivity;
import com.hew.second.gathering.activities.ShopDetailActivity;
import com.hew.second.gathering.api.DefaultSetting;
import com.hew.second.gathering.hotpepper.GourmetResult;
import com.hew.second.gathering.hotpepper.HpHttp;
import com.hew.second.gathering.hotpepper.Shop;
import com.hew.second.gathering.views.adapters.ShopListAdapter;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;
import retrofit2.HttpException;

import static com.hew.second.gathering.activities.BaseActivity.INTENT_SHOP_DETAIL;


@RuntimePermissions
public class MapFragment extends SessionBaseFragment implements OnMapReadyCallback, GoogleMap.OnCameraIdleListener,
        GoogleMap.OnMyLocationButtonClickListener {

    // Fused Location Provider API.
    private FusedLocationProviderClient fusedLocationClient;

    // Location Settings APIs.
    private SettingsClient settingsClient;
    private LocationSettingsRequest locationSettingsRequest;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private Location location;

    private static final String MESSAGE = "message";
    private MapView mapView;
    private GoogleMap googleMap;
    private ArrayList<Shop> shopList;
    private ListView listView;
    private ShopListAdapter adapter;

    private Marker here = null;
    private ArrayList<Marker> shopListMarker = new ArrayList<>();
    private SlidingUpPanelLayout sup = null;
    private Circle circle = null;

    private Boolean requestingLocationUpdates;
    private static final int REQUEST_CHECK_SETTINGS = 0x1;

    private static final LatLng defaultLatLng = new LatLng(35.39291572, 139.44288869);
    private boolean started = true;

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
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView = view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);//when you already implement OnMapReadyCallback in your fragment

    }

    // リクエストを受け取る
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MapFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        MapFragmentPermissionsDispatcher.startLocationUpdatesWithPermissionCheck(this);

        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(activity);
        settingsClient = LocationServices.getSettingsClient(activity);

        createLocationCallback();
        buildLocationSettingsRequest();

        sup = activity.findViewById(R.id.sliding_layout);

        // activity_main.xmlのlistViewにListViewをセット
        listView = activity.findViewById(R.id.listView_shop_list);
        View emptyView = activity.findViewById(R.id.empty_shop_list);
        listView.setEmptyView(emptyView);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(activity, ShopDetailActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable("SHOP_DETAIL", Parcels.wrap(shopList.get(position)));
            if (activity.session != null) {
                bundle.putParcelable("SESSION_DETAIL", Parcels.wrap(activity.session));
            }
            if (activity.defaultSetting != null) {
                bundle.putParcelable("DEFAULT_DETAIL", Parcels.wrap(activity.defaultSetting));
            }
            intent.putExtras(bundle);
            startActivityForResult(intent, INTENT_SHOP_DETAIL);
        });


        Button button = activity.findViewById(R.id.search_refresh);
        button.setOnClickListener((l) -> {
            if (sup.getPanelState() != SlidingUpPanelLayout.PanelState.EXPANDED) {
                sup.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
            }
            for (Marker m : shopListMarker) {
                m.remove();
            }
            shopListMarker.clear();
            fetchShopList();
        });
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        googleMap.setOnCameraIdleListener(this);
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d("debug", "permission granted");

            // default の LocationSource から自前のsourceに変更する
            googleMap.setMyLocationEnabled(true);
            googleMap.setOnMyLocationButtonClickListener(this);
        } else {
            Log.d("debug", "permission error");
            return;
        }

        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker m) {
                // TODO Auto-generated method stub
                int i = shopListMarker.indexOf(m);
                if (i != -1) {
                    Intent intent = new Intent(activity, ShopDetailActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("SHOP_DETAIL", Parcels.wrap(shopList.get(i)));
                    if (activity.session != null) {
                        bundle.putParcelable("SESSION_DETAIL", Parcels.wrap(activity.session));
                    }
                    if (activity.defaultSetting != null) {
                        bundle.putParcelable("DEFAULT_DETAIL", Parcels.wrap(activity.defaultSetting));
                    }
                    intent.putExtras(bundle);
                    startActivityForResult(intent, INTENT_SHOP_DETAIL);
                }

            }
        });

        DefaultSetting d = activity.defaultSetting;

        // 初回のマップ中心
        CameraPosition cameraPos = new CameraPosition.Builder().target(defaultLatLng).bearing(0).build();
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPos));

        if (d != null && d.current_location_flag != null && d.current_location_flag.equals("0")) {
            location = new Location("dummyProvider");
            location.setLatitude(Double.valueOf(d.latitude));
            location.setLongitude(Double.valueOf(d.longitude));
            started = true;
            onSetCenter(false);
        } else {
            started = false;
            startLocationUpdates();
        }
    }

    // locationのコールバックを受け取る
    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                location = locationResult.getLastLocation();
                onSetCenter(started);
                started = true;
            }
        };
    }

    // 端末で測位できる状態か確認する。wifi, GPSなどがOffになっているとエラー情報のダイアログが出る
    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder =
                new LocationSettingsRequest.Builder();

        builder.addLocationRequest(locationRequest);
        locationSettingsRequest = builder.build();
    }

    // FusedLocationApiによるlocation updatesをリクエスト
    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    protected void startLocationUpdates() {
        // Begin by checking if the device has the necessary location settings.
        if (locationSettingsRequest == null) {
            return;
        }
        settingsClient.checkLocationSettings(locationSettingsRequest)
                .addOnSuccessListener(activity,
                        new OnSuccessListener<LocationSettingsResponse>() {
                            @Override
                            public void onSuccess(
                                    LocationSettingsResponse locationSettingsResponse) {
                                Log.i("debug", "All location settings are satisfied.");

                                // パーミッションの確認
                                if (ActivityCompat.checkSelfPermission(
                                        activity,
                                        Manifest.permission.ACCESS_FINE_LOCATION) !=
                                        PackageManager.PERMISSION_GRANTED
                                        && ActivityCompat.checkSelfPermission(
                                        activity,
                                        Manifest.permission.ACCESS_COARSE_LOCATION) !=
                                        PackageManager.PERMISSION_GRANTED) {

                                    // TODO: Consider calling
                                    //    ActivityCompat#requestPermissions
                                    // here to request the missing permissions, and then overriding
                                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                    //                                          int[] grantResults)
                                    // to handle the case where the user grants the permission. See the documentation
                                    // for ActivityCompat#requestPermissions for more details.
                                    Log.d("debug", "permission error");
                                    return;
                                }
                                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

                            }
                        })
                .addOnFailureListener(activity, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i("debug", "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(
                                            activity,
                                            REQUEST_CHECK_SETTINGS);

                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i("debug", "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e("debug", errorMessage);
                                Toast.makeText(activity,
                                        errorMessage, Toast.LENGTH_LONG).show();

                                requestingLocationUpdates = false;
                        }

                    }
                });

        requestingLocationUpdates = true;
    }

    @Override
    public boolean onMyLocationButtonClick() {
        startLocationUpdates();
        return false;
    }

    @Override
    public void onCameraIdle() {
        onGetCenter(mapView);
    }

    public void onSetCenter(boolean afterSecond) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        SearchArgs.lat = (float) latLng.latitude;
        SearchArgs.lng = (float) latLng.longitude;

        CameraPosition cameraPos = new CameraPosition.Builder()
                .target(latLng).zoom(15.0f)
                .bearing(0).tilt(60).build();
        if (afterSecond) {
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPos));
        } else {
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPos));
            fetchShopList();
        }

    }

    public void onGetCenter(View view) {
        CameraPosition cameraPos = googleMap.getCameraPosition();
        SearchArgs.lat = (float) cameraPos.target.latitude;
        SearchArgs.lng = (float) cameraPos.target.longitude;
        // マーカー設定
        LatLng latLng = new LatLng(cameraPos.target.latitude, cameraPos.target.longitude);
        MarkerOptions options = new MarkerOptions();
        options.position(latLng);
        if (here != null) {
            here.remove();
        }
        here = googleMap.addMarker(options);
        if (circle != null) {
            circle.remove();
        }
        circle = googleMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(SearchArgs.rangeList.get(SearchArgs.range))
                .strokeColor(Color.argb(64, 0, 0, 255))
                .fillColor(Color.argb(32, 0, 0, 255)));
    }

    private void fetchShopList() {
        HashMap<String, String> options = new HashMap<>();
        HashMap<String, Float> pos = new HashMap<>();
        //options.put("keyword", SearchArgs.keyword);
        options.put("keyword", SearchArgs.keyword);
        options.put("lat", Float.valueOf(SearchArgs.lat).toString());
        options.put("lng", Float.valueOf(SearchArgs.lng).toString());
        if (SearchArgs.genre != null) {
            options.put("genre", SearchArgs.genre);
        }
        options.put("range", SearchArgs.range.toString());
        Observable<GourmetResult> ShopList = HpHttp.getService().getShopList(options);
        cd.add(ShopList.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            if (list.results.shop != null && activity != null) {
                                listView = activity.findViewById(R.id.listView_shop_list);
                                shopList = new ArrayList<>(list.results.shop);
                                ArrayList<Shop> data = new ArrayList<>(list.results.shop);
                                adapter = new ShopListAdapter(data);
                                if (listView != null) {
                                    listView.setAdapter(adapter);
                                }

                                for (Shop s : shopList) {
                                    // マーカー設定
                                    LatLng latLng = new LatLng(Double.valueOf(s.lat), Double.valueOf(s.lng));
                                    MarkerOptions mo = new MarkerOptions();
                                    mo.position(latLng);
                                    mo.title(s.name);
                                    mo.icon(BitmapDescriptorFactory
                                            .defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                                    shopListMarker.add(googleMap.addMarker(mo));
                                }
                                onGetCenter(mapView);
                            }
                        },  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            if (activity != null && !cd.isDisposed()) {
                                if (throwable instanceof HttpException && (((HttpException) throwable).code() == 401 || ((HttpException) throwable).code() == 500)) {
                                    // ログインアクティビティへ遷移
                                    Intent intent = new Intent(activity.getApplication(), LoginActivity.class);
                                    startActivity(intent);
                                }
                            }
                        }
                ));
    }

    @OnPermissionDenied(Manifest.permission.ACCESS_FINE_LOCATION)
    void showDeniedForCamera() {
        Toast.makeText(activity, "現在地が取得できません", Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain(Manifest.permission.ACCESS_FINE_LOCATION)
    void showNeverAskForCamera() {
        Toast.makeText(activity, "現在地が取得できません", Toast.LENGTH_SHORT).show();
    }
}
