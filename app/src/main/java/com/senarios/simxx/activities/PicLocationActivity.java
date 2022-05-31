package com.senarios.simxx.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.senarios.simxx.R;
import com.senarios.simxx.Utility;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class PicLocationActivity extends AppCompatActivity implements
        OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnCameraMoveStartedListener {

    private Button getLocation;
    private GoogleMap mGoogleMap;
    private ImageView marker_loc;
    private View emptyView;
    LatLng startLatLng;
    private Marker currentMarker;
    FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic_location);
        getLocation = findViewById(R.id.btn_getlocation);
        marker_loc = findViewById(R.id.marker);
        emptyView = findViewById(R.id.emptyView);

//        final Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                marker_loc.performClick();
//                handler.postDelayed(this, 3000);
//            }
//        }, 3000);
//        marker_loc.setOnClickListener(v -> {
//            Toast.makeText(getApplicationContext(), "asd", Toast.LENGTH_SHORT).show();
//        });
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapp);
        Utility.show(this);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        getLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                marker_loc.performClick();
//                if (marker_loc.getDrawable()==null)
                SharedPreferences prefs = getSharedPreferences("myy", MODE_PRIVATE);
                String lat = prefs.getString("latt", "");
                String lng = prefs.getString("lonn", "");
                String city = prefs.getString("city", "");
                if (city != null && !city.isEmpty()) {
                    String offline = getIntent().getStringExtra("itsOffline");
                    String stream = getIntent().getStringExtra("itsStream");
                    if (offline != null) {
                        onBackPressed();
                    } else if (stream != null) {
                        onBackPressed();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please tap on red marker first", Toast.LENGTH_SHORT).show();
                }
//                Toast.makeText(getApplicationContext(), "Center From Point: Long: "
//                        + lng + " Lat"
//                        + lat + "\n" + city, Toast.LENGTH_SHORT).show();


            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setMyLocationEnabled(true);

        moveToCurrentLoc();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                emptyView.setVisibility(View.GONE);
            }
        }, 2000);

        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                VisibleRegion visibleRegion = googleMap.getProjection()
                        .getVisibleRegion();

                Point x = googleMap.getProjection().toScreenLocation(
                        visibleRegion.farRight);

                Point y = googleMap.getProjection().toScreenLocation(
                        visibleRegion.nearLeft);

                Point centerPoint = new Point(x.x / 2, y.y / 2);

                LatLng centerFromPoint = googleMap.getProjection().fromScreenLocation(
                        centerPoint);
                Geocoder gcd = new Geocoder(PicLocationActivity.this, Locale.getDefault());
                List<Address> addresses = null;
                try {
                    addresses = gcd.getFromLocation(centerFromPoint.latitude, centerFromPoint.longitude, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                Toast.makeText(getApplicationContext(), "Center From Point: Long: "
//                        + centerFromPoint.longitude + " Lat"
//                        + centerFromPoint.latitude, Toast.LENGTH_SHORT).show();
                SharedPreferences.Editor editor = getSharedPreferences("myy", MODE_PRIVATE).edit();
                editor.putString("latt", String.valueOf(centerFromPoint.latitude));
                editor.putString("lonn", String.valueOf(centerFromPoint.longitude));
                if (addresses != null && addresses.size() > 0) {
                    editor.putString("city", addresses.get(0).getAddressLine(0));
                    Toast.makeText(getApplicationContext(), "Selected Address is : "
                            + addresses.get(0).getAddressLine(0) ,Toast.LENGTH_SHORT).show();
                }
                editor.apply();
            }
        });
//        mGoogleMap.addMarker(new MarkerOptions().position(latLngArrayList.get(j)).title("Email: " + locationNameArraylist.get(i))
//                .snippet("Date: " + locationReferenceArraylist.get(i)));
        // below line is use to move camera.
//        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
//            @Override
//            public void onMapClick(LatLng latLng) {
//                //showing marker on click
//                MarkerOptions marker = new MarkerOptions().position(new LatLng(latLng.latitude, latLng.longitude)).title("New Marker");
//                googleMap.addMarker(marker);
//                LatLng center = googleMap.getCameraPosition().target;
//
//                //showing marker on click
//
//                //get city on click on map like (addresses.get(0).getLocality())
//                Geocoder gcd = new Geocoder(PicLocationActivity.this, Locale.getDefault());
//                List<Address> addresses = null;
//                try {
//                    addresses = gcd.getFromLocation(latLng.latitude, latLng.longitude, 1);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                //get city on click on map
//
////                Toast.makeText(
////                        PicLocationActivity.this,
////                        "Lat : " + latLng.latitude + " , "
////                                + "Long : " + latLng.longitude+"   "+addresses.get(0).getLocality(),
////                        Toast.LENGTH_LONG).show();
//                Intent intent = new Intent(PicLocationActivity.this,OfflineStreamActivity.class);
//                intent.putExtra("lat", latLng.latitude);
//                intent.putExtra("lon", latLng.longitude);
//                intent.putExtra("city", addresses.get(0).getLocality());
//                startActivity(intent);
//                finish();
//            }
//        });

    }

    @Override
    public void onCameraMoveStarted(int i) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    private void moveToCurrentLoc() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @SuppressLint("ApplySharedPref")
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    Location location = task.getResult();

                    if (location != null) {
                        SharedPreferences prefs = getSharedPreferences("myy", MODE_PRIVATE);
                        String lat = prefs.getString("latt", "");
                        String lng = prefs.getString("lonn", "");
                        if (lat != null && !lat.isEmpty() && lng != null && !lng.isEmpty()) {
                            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(lat), Double.parseDouble(lng)), 12.0f));
                        } else {
                            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(55.943723,-3.189285), 12.0f));
                        }
                        Utility.dismiss();
                    }

                }
            });
        }
    }
}