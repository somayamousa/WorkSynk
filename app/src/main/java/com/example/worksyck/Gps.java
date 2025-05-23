package com.example.worksyck;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class Gps extends AppCompatActivity {

    private GoogleMap mMap;

    private final int FINE_PERMISSION_CODE=1;

    //   عشان اخزن الموقع جواها
    private  Location currentLocation;
    //المسؤول عن الحصول على الموقع الحالي للمستخدم
    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gps);
//        inizlatinize the   fusedLocationProviderClient
        fusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == FINE_PERMISSION_CODE){
            if (requestCode == FINE_PERMISSION_CODE) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLastLocation();
                } else {
                    Toast.makeText(this, "تم رفض صلاحية الموقع", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION},FINE_PERMISSION_CODE);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null){
                    currentLocation = location;
                    // الفرجمنت الي بتحوي ع الخريطه يعني بنربطه بالجافا
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
//       تحميل الخريطة
                    mapFragment.getMapAsync(Gps.this::onMapReady);

                }}

        });
    }


    public void onMapReady(@NonNull GoogleMap googleMap) {
//        لما تكون الخريطه جاهزه بستدعي هاد الميثود من خلاله بحدد نوع الخريطه والماركر والخ
        mMap = googleMap;


        // موقع معين باستخدام خطوط العرض والطول
        LatLng myLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

        mMap.addMarker(new MarkerOptions().position(myLocation).title("موقعي الحالي"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15));
        mMap.getUiSettings().setZoomControlsEnabled(true); // لتفعيل أزرار الزوم

        Log.d("LOCATION", "Latitude: " + currentLocation.getLatitude() +
                ", Longitude: " + currentLocation.getLongitude());

    }
}