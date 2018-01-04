package com.example.chong.memorableplaces;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public static final String SP_TAG = "com.example.chong.memorableplaces";
    public static final String SP_ADDRESS = "address";
    public static final String SP_LAT = "lat";
    public static final String SP_LNG = "lng";
    public static ArrayList<String> address = new ArrayList<>(Arrays.asList("Add a new place"));
    public static ArrayList<Double> lats = new ArrayList<>();
    public static ArrayList<Double> lngs = new ArrayList<>();
    private LatLng lastLatLng = null;

    private ArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences sharedPreferences = this.getSharedPreferences(SP_TAG, Context.MODE_PRIVATE);

        String addressData = sharedPreferences.getString(SP_ADDRESS, "");
        String latData = sharedPreferences.getString(SP_LAT, "");
        String lngData = sharedPreferences.getString(SP_LNG, "");
        try {
            if (!TextUtils.isEmpty(addressData)) {
                address = (ArrayList<String>) ObjectSerializer.deserialize(addressData);
            }
            if(!TextUtils.isEmpty(latData)){
                lats = (ArrayList<Double>) ObjectSerializer.deserialize(latData);
            }
            if(!TextUtils.isEmpty(lngData)){
                lngs = (ArrayList<Double>) ObjectSerializer.deserialize(lngData);
            }
        } catch (Exception e) {
            Log.e(TAG, "onCreate: ", e);
        }
        ListView listView = findViewById(R.id.listView);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, MainActivity.address);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(onItemClickListener);

        if (!setLastLatLng()) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            if(MainActivity.lats.size() == 0 && MainActivity.lngs.size() == 0){
                MainActivity.lats.add(this.lastLatLng.latitude);
                MainActivity.lngs.add(this.lastLatLng.longitude);
            }else{
                MainActivity.lats.set(0, this.lastLatLng.latitude);
                MainActivity.lats.set(0, this.lastLatLng.longitude);
            }
        }

        Log.d(TAG, "onCreate: lat: " + lats.size());
        Log.d(TAG, "onCreate: lng: " + lngs.size());
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setLastLatLng();
                MainActivity.lats.add(this.lastLatLng.latitude);
                MainActivity.lngs.add(this.lastLatLng.longitude);
            }
        }
    }

    private AdapterView.OnItemClickListener onItemClickListener = (adapter, v, i, l) -> {
        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        intent.putExtra("index", i);
        startActivity(intent);
    };

    private boolean setLastLatLng() {
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        Location lastLocation = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        this.lastLatLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
        return true;
    }
}
