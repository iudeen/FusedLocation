package com.irfan.iudeen.fusedlocation;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import android.view.View;
import android.view.WindowManager;

import android.widget.EditText;
import android.widget.ImageView;

import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, SensorEventListener {

    private GoogleMap mMap;
    private EditText mSearchText;
    private static final float DEFAULT_ZOOM = 15f;
    private ImageView mSearch;
    private SensorManager mSensorManager;
    private Sensor mLight;
    public float lux;
    private TextView sensorTv;
    public LatLng myLocation;
    Marker marker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mSearchText = findViewById(R.id.input_search);
        mSearch = findViewById(R.id.ic_search);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        init();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Toast.makeText(this, "Welcome to Map", Toast.LENGTH_SHORT ).show();
        Intent i = getIntent();
        Location location = i.getParcelableExtra("Location");
        // Add a marker in Sydney and move the camera
        LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
        marker = mMap.addMarker(new MarkerOptions()
                .position(myLocation)
                .title("My Location")
                .snippet(String.valueOf(lux)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
    }

    private void init(){

        mSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                geoLocate();
                hideSoftKeyboard();
            }
    });
    }

    private void geoLocate(){
        Toast.makeText(this, "Searching", Toast.LENGTH_SHORT ).show();
        hideSoftKeyboard();

        String searchString = mSearchText.getText().toString();

        Geocoder geocoder = new Geocoder(MapsActivity.this);
        List<Address> list = new ArrayList<>();
        try{
            list = geocoder.getFromLocationName(searchString, 1);
        }catch (IOException e){
            Toast.makeText(this, "Exception", Toast.LENGTH_SHORT).show();
        }

        if(list.size() > 0){
            Address address = list.get(0);

            String title = address.getAddressLine(0);
            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()),DEFAULT_ZOOM, title);

        }
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        lux = event.values[0];
       // mMap.addMarker(new MarkerOptions().position(myLocation).title("Light values:" + String.valueOf(lux) ));
        marker.setSnippet("Atmospheric Pressure: " + String.valueOf(lux));



    }
    private void moveCamera(LatLng latLng, float zoom, String title){

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        if(latLng != myLocation){
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(title);
            mMap.addMarker(options);
        }
        hideSoftKeyboard();
    }
    private void hideSoftKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }
}
