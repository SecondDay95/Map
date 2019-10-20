package com.example.map;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final String FINE_LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION_PERMISSION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private boolean mLocationPermissionGranted = false;
    private static final float DEFAULT_ZOOM = 15f;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 1;

    private FusedLocationProviderClient fusedLocationProviderClient;
    EditText etSearch;
    ImageView mGps, mStreetView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mGps = (ImageView) findViewById(R.id.ic_gps);
        mStreetView = (ImageView) findViewById(R.id.ic_streetView);
        getLocationPermission();

//        // Initialize the SDK
//        Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_key));
//
//        // Create a new Places client instance
//        PlacesClient placesClient = Places.createClient(this);

    }

    private void init() {
        Log.d("MapsActivity", "Initializing");
        etSearch = (EditText) findViewById(R.id.input_search);
        etSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

//                // Set the fields to specify which types of place data to
//                // return after the user has made a selection.
//                List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME);
//
//                // Start the autocomplete intent.
//                Intent intent = new Autocomplete.IntentBuilder(
//                        AutocompleteActivityMode.OVERLAY, fields)
//                        .build(MapsActivity.this);
//                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                || actionId == EditorInfo.IME_ACTION_DONE
                || event.getAction() == KeyEvent.ACTION_DOWN
                || event.getAction() == KeyEvent.KEYCODE_ENTER) {

                    //execute method for searching:
                    geoLocate();
                }

                return false;
            }
        });

        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MapsActivity", "GPS button clicked");
                getLocationDevices();
            }
        });
    }

    private void geoLocate() {
        Log.d("MapsActivity", "Geolocating");
        String searchString = etSearch.getText().toString();

        Geocoder geocoder = new Geocoder(MapsActivity.this);
        List<Address> list = new ArrayList<>();
        try {
            list = geocoder.getFromLocationName(searchString, 1);
        }
        catch (IOException e) {
            Log.d("MapsActivity", "IOException " + e.getMessage());
        }

        if (list.size() > 0) {
            Address address = list.get(0);
            Log.d("MapsActivity", "Found a location " + address.toString());
            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM,
                    address.getAddressLine(0));
        }
    }

    private void initMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Log.d("MapsActivity", "Initialize map");
    }

    //Get location permission:
    private void getLocationPermission() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ActivityCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION_PERMISSION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.getApplicationContext(),
                COARSE_LOCATION_PERMISSION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            //Initialize map
            initMap();
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mLocationPermissionGranted = false;

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("MapActivity", "Permission Granted");
                mLocationPermissionGranted = true;

            }

        } else {
            Log.d("MapsActivity", "Permission Denied");
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Log.d("MapsActivity", "Map is ready");
        System.out.println(mLocationPermissionGranted);
        if (mLocationPermissionGranted) {
            getLocationDevices();
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            init();
            mStreetView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MapsActivity.this, StreetViewActivity.class);
                    startActivity(intent);
                }
            });
            //mMap.getUiSettings().setCompassEnabled(true);
            //mMap.getUiSettings().setZoomGesturesEnabled(true);
        }

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    private void getLocationDevices () {
        Log.d("MapsActivity", "Get location devices");
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            Task location = fusedLocationProviderClient.getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        Log.d("MapsActivity", "Succesfully get device location");
                        Location currentLocation = (Location) task.getResult();
                        moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                DEFAULT_ZOOM, "My Location");
                    }
                    else {
                        Log.d("Maps Activity", "Current Location is null");
                        Toast.makeText(MapsActivity.this, "Unable to get current location",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (SecurityException e) {
            Log.e("MapsActivity", "Security Exception: " + e.getMessage());
        }
    }

    private void moveCamera (LatLng latLng, float zoom, String title) {
        Log.d("MapsActivity", "Moving camera to lat: " + latLng.latitude + " and lng " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        if(!title.equals("My Location")) {
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(title);
            mMap.addMarker(options);
        }
    }
}
