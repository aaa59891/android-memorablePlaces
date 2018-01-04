package com.example.chong.memorableplaces;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String TAG = "MapsActivity";

    private GoogleMap mMap;

    private LocationManager manager;
    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            centerMapOnLocation(latLng, "Your place");
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng latlng = new LatLng(-34, 151);
        String title = "Default";
        Intent intent = getIntent();
        int notExist = -1;
        int index = intent.getIntExtra("index", notExist);
        if (index != notExist) {
            latlng = new LatLng(MainActivity.lats.get(index),MainActivity.lngs.get(index));
            title = MainActivity.address.get(index);
        }

        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }

        centerMapOnLocation(latlng, title);
        mMap.setOnMapLongClickListener(longClickListener);
    }

    private GoogleMap.OnMapLongClickListener longClickListener = latLng -> {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> data = null;
        String title = "";
        try {
            data = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if(data != null && data.size() > 0){
                title = getAddress(data.get(0));
            }
        } catch (Exception e) {
            Log.e(TAG, "OnMapLongClickListener: ", e);
        }
        if(TextUtils.isEmpty(title)){
            Date d = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            title = sdf.format(d);
        }
        MainActivity.address.add(title);
        MainActivity.lats.add(latLng.latitude);
        MainActivity.lngs.add(latLng.longitude);
        SharedPreferences sharedPreferences = this.getSharedPreferences(MainActivity.SP_TAG, Context.MODE_PRIVATE);

        try{
            sharedPreferences.edit().putString(MainActivity.SP_ADDRESS, ObjectSerializer.serialize(MainActivity.address)).apply();
            sharedPreferences.edit().putString(MainActivity.SP_LAT, ObjectSerializer.serialize(MainActivity.lats)).apply();
            sharedPreferences.edit().putString(MainActivity.SP_LNG, ObjectSerializer.serialize(MainActivity.lngs)).apply();
        }catch (Exception e){
            Log.e(TAG, "save to SharedPreferences had an error: ", e);
        }
        mMap.addMarker(new MarkerOptions().position(latLng).title(title));
        Toast.makeText(this, "Location Saved", Toast.LENGTH_SHORT).show();
    };

    private void centerMapOnLocation(LatLng latLng, String title){
        mMap.clear();

        mMap.addMarker(new MarkerOptions().position(latLng).title(title));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
    }

    private String getAddress(Address address){
        if(address == null){
            return "";
        }
        return address.getAddressLine(0);
    }
}
