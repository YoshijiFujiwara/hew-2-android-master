package com.hew.second.gathering.activities;


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
import com.hew.second.gathering.activities.BaseActivity;
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
public class DefaultMapActivity extends BaseActivity implements OnMapReadyCallback, GoogleMap.OnCameraIdleListener,
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
    private SlidingUpPanelLayout sup = null;
    private Circle circle = null;

    private Boolean requestingLocationUpdates;
    private static final int REQUEST_CHECK_SETTINGS = 0x1;

    private static final LatLng defaultLatLng = new LatLng(35.39291572, 139.44288869);
    private boolean started = true;

    // リクエストを受け取る
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        DefaultMapActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DefaultMapActivityPermissionsDispatcher.startLocationUpdatesWithPermissionCheck(this);
        setContentView(R.layout.activity_default_map);

        // Backボタンを有効にする
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        mapView = findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);

        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this);
        settingsClient = LocationServices.getSettingsClient(this);

        createLocationCallback();
        buildLocationSettingsRequest();

        Button ok = findViewById(R.id.button_ok);
        ok.setOnClickListener((l) -> {
            Intent intent = new Intent();
            intent.putExtra("lat", String.valueOf(location.getLatitude()));
            intent.putExtra("lng", String.valueOf(location.getLongitude()));
            intent.putExtra(SNACK_MESSAGE, "店検索の中心地を設定しました。");
            setResult(RESULT_OK, intent);
            finish();

        });
        Button cancel = findViewById(R.id.button_cancel);
        cancel.setOnClickListener((l) -> {
            onBackPressed();
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        googleMap.setOnCameraIdleListener(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d("debug", "permission granted");

            // default の LocationSource から自前のsourceに変更する
            googleMap.setMyLocationEnabled(true);
            googleMap.setOnMyLocationButtonClickListener(this);
        } else {
            Log.d("debug", "permission error");
            return;
        }


        Intent beforeIntent = getIntent();
        String lat = beforeIntent.getStringExtra("lat");//設定したkeyで取り出す
        String lng = beforeIntent.getStringExtra("lng");

        // 初回のマップ中心
        CameraPosition cameraPos = new CameraPosition.Builder().target(defaultLatLng).bearing(0).build();
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPos));

        if (lat != null && lng != null) {
            location = new Location("dummy");
            location.setLatitude(Double.valueOf(lat));
            location.setLongitude(Double.valueOf(lng));
            onSetCenter(false);
            started = true;
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
                .addOnSuccessListener(this,
                        new OnSuccessListener<LocationSettingsResponse>() {
                            @Override
                            public void onSuccess(
                                    LocationSettingsResponse locationSettingsResponse) {
                                Log.i("debug", "All location settings are satisfied.");

                                // パーミッションの確認
                                if (ActivityCompat.checkSelfPermission(
                                        getApplicationContext(),
                                        Manifest.permission.ACCESS_FINE_LOCATION) !=
                                        PackageManager.PERMISSION_GRANTED
                                        && ActivityCompat.checkSelfPermission(
                                        getApplicationContext(),
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
                .addOnFailureListener(this, new OnFailureListener() {
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
                                            DefaultMapActivity.this,
                                            REQUEST_CHECK_SETTINGS);

                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i("debug", "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e("debug", errorMessage);
                                Toast.makeText(getApplication(),
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

    public void onSetCenter(boolean animate) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraPosition cameraPos = new CameraPosition.Builder()
                .target(latLng).zoom(15.0f)
                .bearing(0).tilt(60).build();
        if (animate) {
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPos));
        } else {
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPos));
        }

    }

    public void onGetCenter(View view) {
        CameraPosition cameraPos = googleMap.getCameraPosition();
        // マーカー設定
        LatLng latLng = new LatLng(cameraPos.target.latitude, cameraPos.target.longitude);
        location = new Location("dummy");
        location.setLatitude(cameraPos.target.latitude);
        location.setLongitude(cameraPos.target.longitude);
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


    @OnPermissionDenied(Manifest.permission.ACCESS_FINE_LOCATION)
    void showDeniedForCamera() {
        Toast.makeText(this, "現在地が取得できません", Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain(Manifest.permission.ACCESS_FINE_LOCATION)
    void showNeverAskForCamera() {
        Toast.makeText(this, "現在地が取得できません", Toast.LENGTH_SHORT).show();
    }
}
