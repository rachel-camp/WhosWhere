package com.example.savvy.whoswherev1;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class spots extends MainActivity {
    private LocationRequest mLocationRequest;
    Button getCrntBtn;
    Button createBtn;
    String locName;
    double lat;
    double longi;
    EditText locNameView;
    DynamoDBMapper dynamoDBMapper;
    String loc;
    String locID;

    String user;

    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */

    public spots(String locName, double lat, double longi)
    {
        this.locName = locName;
        this.lat = lat;
        this.longi=longi;
        //this.user = user;

    }
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.second_layout);
        startLocationUpdates();

        user = getIntent().getStringExtra("User");

        // AWSMobileClient enables AWS user credentials to access your table
        AWSMobileClient.getInstance().initialize(this).execute();

        AWSCredentialsProvider credentialsProvider = AWSMobileClient.getInstance().getCredentialsProvider();
        AWSConfiguration configuration = AWSMobileClient.getInstance().getConfiguration();


        // Add code to instantiate a AmazonDynamoDBClient
        AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(credentialsProvider);

        this.dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(configuration)
                .build();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        userId = getIntent().getStringExtra("User");

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }


    public void currentOnClick(View v){

        final FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // GPS location can be null if GPS is switched off
                            if (location != null) {
                                onLocationChanged(location);
                                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                                lat = location.getLatitude();
                                longi = location.getLongitude();

                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("MapDemoActivity", "Error trying to get last GPS location");
                            e.printStackTrace();
                        }
                    });
        } else {
            requestPermissions();
        }
    }


    public void createOnClick(View v){
        EditText name = findViewById(R.id.locNameView);
        spots location = new spots(name.getText().toString(), lat, longi);
        setContentView(R.layout.map_layout);


        createLocationDB(location.lat, location.longi, name.getText().toString(), user);

        Intent intent = new Intent(spots.this, Map.class);
        intent.putExtra("User", user);
        startActivity(intent);
    }
    // Trigger new location updates at interval
    protected void startLocationUpdates() {

        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        // do work here
                        onLocationChanged(locationResult.getLastLocation());
                    }
                },
                Looper.myLooper());
    }
    public void onLocationChanged(Location location) {
        // New location has now been determined
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        // You can now create a LatLng Object for use with maps
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        lat = location.getLatitude();
        longi = location.getLongitude();
    }
    public double getLastLocationLat() {

        // Get last known recent location using new Google Play Services SDK (v11+)
        final FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this);

        locationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // GPS location can be null if GPS is switched off
                        if (location != null) {
                            onLocationChanged(location);
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                           double lat = location.getLatitude();

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("MapDemoActivity", "Error trying to get last GPS location");
                        e.printStackTrace();
                    }
                });
        return lat;
    }
    public void getLastLocationLong() {

        // Get last known recent location using new Google Play Services SDK (v11+)
        final FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this);

        locationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // GPS location can be null if GPS is switched off
                        if (location != null) {
                            onLocationChanged(location);
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            double lat = location.getLongitude();

                            setLocation(latLng);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("MapDemoActivity", "Error trying to get last GPS location");
                        e.printStackTrace();
                    }
                });
        //return lat;
    }

    public void setLocation(LatLng latLng){
        lat = latLng.latitude;
        longi = latLng.longitude;
    }


    public void onMapReady(GoogleMap googleMap) {

        if(checkPermissions()) {
            googleMap.setMyLocationEnabled(true);
        }
    }

    private boolean checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            requestPermissions();
            return false;
        }
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                8);
    }

    public spots() {

    }


    public String getLocName() {
        return locName;
    }

    public void setLocName(String locName) {
        this.locName = locName;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLongi() {
        return longi;
    }

    public void setLongi(double longi) {
        this.longi = longi;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void createLocationDB(double lat, double lng, String name, String email) {

        UUID locationID = UUID.randomUUID();
        List<String> users = new ArrayList();
        users.add(email);
        //Toast.makeText(this, "Location ID: " + locationID.toString(), Toast.LENGTH_LONG).show();

        final location_db newLocation = new location_db();

        newLocation.setLocationId(locationID.toString());

        newLocation.setLatitude(lat);
        newLocation.setLongitude(lng);
        newLocation.setName(name);
        newLocation.setRadius(100.0);
        newLocation.setUsers(users);

        new Thread(new Runnable() {
            @Override
            public void run() {
                dynamoDBMapper.save(newLocation);
                // Item saved
            }
        }).start();

        locID = locationID.toString();
        loc = locationID.toString();
        readUser(email);
        //updateUser(u.id,u.firstName,u.lastName,u.spots,u.password,locationID.toString());
    }

    /*public void readUser(final String email) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                User_DB userItem = dynamoDBMapper.load(
                        User_DB.class,
                        email);
                updateUser(userItem.getUserId(),userItem.getFirst_name(),userItem.getLast_name(),userItem.getLocations(),userItem.getPassword());

                // Item read
                Log.d("User Item", userItem.getFirst_name());
                //u = new user(userItem.getUserId(), userItem.getLocations(), userItem.getFirst_name(), userItem.getLast_name(),  userItem.getPassword());

            }
        }).start();

        //updateUser(u.id,u.firstName,u.lastName,u.spots,u.password,locationID);
    }*/

    public void readUser(final String email) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                User_DB userItem = new User_DB();
    try {
        userItem = dynamoDBMapper.load(

                User_DB.class,
                email);
    }
    catch (Exception e) {
        System.out.println("Problem with db: " + e);
    }

                updateUser(userItem.getUserId(),userItem.getFirst_name(),userItem.getLast_name(),userItem.getLocations(),userItem.getPassword());
                // Item read
                Log.d("User Item", userItem.getFirst_name());
            }
        }).start();
    }

    public void updateUser(String email, String firstName, String lastName, List<String> locations, String password) {
        final User_DB userItem = new User_DB();

        if(locations == null){
            List<String> location = new ArrayList();
            location.add(loc);
            userItem.setLocations(location);
        }else{
            locations.add(loc);
            userItem.setLocations(locations);
        }


        userItem.setUserId(email);

        userItem.setFirst_name(firstName);
        userItem.setLast_name(lastName);
        userItem.setPassword(password);

        new Thread(new Runnable() {
            @Override
            public void run() {

                dynamoDBMapper.save(userItem);

                // Item updated
            }
        }).start();
    }
}
