package com.example.savvy.whoswherev1;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;

import java.util.List;
import java.util.UUID;

public class Map extends MainActivity implements OnMapReadyCallback {

    View myView;
    @Nullable





    private static final String TAG = "MapActivity";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION__REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;
    DynamoDBMapper dynamoDBMapper;


    //vars
    private Boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private String userId;


    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(Map.this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: google play services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            //Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(FirstFragment.this, available, ERROR_DIALOG_REQUEST);
            // dialog.show();
        }else{
            //Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is ready", Toast.LENGTH_SHORT).show();
        mMap = googleMap;
        Log.d(TAG, "onMapReady: map is ready");

        if (mLocationPermissionsGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_layout);

        userId = getIntent().getStringExtra("User");

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

        getLocationPermission();



        /*NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        spots spot = new spots("gym", 33.942187,-84.520501);
        spots[] spots= new spots[]{spot};
        String savvy = "savvy";

        //user user = new user(savvy, spots);
        displaySpots(spots);*/

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

    /*public void displaySpots(spots[] spots)
    {
        LatLng latLng;

        for(int i = 0; i<spots.length - 1; i++)
        {
            latLng = new LatLng(spots[i].getLastLocationLat(), spots[i].getLastLocationLong());
            chartSpot(latLng, 100);
        }
    }*/
    private void getDeviceLocation(){
        Log.d(TAG, "getDeviceLocation: getting current device location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try{
            if(mLocationPermissionsGranted){
                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "onComplete: found location");
                            Location currentLocation = (Location) task.getResult();

                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM);

                        }else{
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(Map.this, "unable to get current location", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
            }
        }catch (SecurityException e){
            Log.d(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }

    private void moveCamera(LatLng latLng, float zoom){
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        //Displaying Spot
        //double radius = 100;
        //chartSpot(latLng, radius);
        readUser(userId);
    }

    private void getSpots(User_DB user){
        List locations = user.getLocations();
        Object[] loc = locations.toArray();
        for(int i=0; i<loc.length; i++){
            readLocation(loc[i].toString());
        }
    }

    private void initMap(){
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(Map.this);
    }

    private void chartSpot(double latitude, double longitude, final double radius, final String id){
        final LatLng test = new LatLng(latitude,longitude);

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Circle spot = mMap.addCircle(new CircleOptions().center(test).radius(radius).strokeColor(Color.BLUE).fillColor(Color.CYAN));
                spot.setClickable(true);
                spot.setTag(id);
                mMap.setOnCircleClickListener(new GoogleMap.OnCircleClickListener() {
                    @Override
                    public void onCircleClick(Circle circle) {
                        Intent intent = new Intent(getApplicationContext(), spotInfo.class);
                        intent.putExtra("User", userId);
                        intent.putExtra("Location", circle.getTag().toString());
                        startActivity(intent);
                    }
                });
                //withinSpot(spot, latLng);
            }
        });

    }

    private void withinSpot(Circle spot, LatLng current){
        double r = spot.getRadius();
        LatLng center = spot.getCenter();
        double cX = center.latitude;
        double cY = center.longitude;
        double pX = current.latitude;
        double pY = current.longitude;

        float[] results = new float[1];

        Location.distanceBetween(cX, cY, pX, pY, results);

        if(results[0] < r) {
            Toast.makeText(this, "You are in the spot", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "You are NOT in the spot", Toast.LENGTH_LONG).show();
        }
    }

    private void getLocationPermission(){
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {FINE_LOCATION,
                COURSE_LOCATION};

        if (isServicesOK()==true) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionsGranted = true;
                    initMap();
                } else {
                    ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION__REQUEST_CODE);
                }

            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION__REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        mLocationPermissionsGranted = false;

        switch (requestCode){
            case LOCATION_PERMISSION__REQUEST_CODE:{
                if(grantResults.length > 0){
                    for(int i = 0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                    //initialize the map
                    initMap();
                }
            }
        }
    }

    public void createLocationDB(double lat, double lng, String name, String email) {

        UUID locationID = UUID.randomUUID();
        //Set users = new HashSet();
        Toast.makeText(this, "Location ID: " + locationID.toString(), Toast.LENGTH_LONG).show();

        final location_db newLocation = new location_db();

        newLocation.setLocationId(locationID.toString());

        newLocation.setLatitude(33.972741);
        newLocation.setLongitude(-84.733283);
        newLocation.setName("Test");
        newLocation.setRadius(100.0);
        //newLocation.setUsers(users);

        new Thread(new Runnable() {
            @Override
            public void run() {
                dynamoDBMapper.save(newLocation);
                // Item saved
            }
        }).start();
    }

    public void readLocation(final String id) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                location_db locationItem = dynamoDBMapper.load(
                        location_db.class,
                        id);

                // Item read
                //Log.d("Location Item", locationItem.getName());

                chartSpot(locationItem.getLatitude(),locationItem.getLongitude(),locationItem.getRadius(), locationItem.getLocationId());
            }
        }).start();
    }

    public void updateLocation() {
        final location_db locationItem = new location_db();
        //UUID locationID = UUID.randomUUID();

        locationItem.setLocationId("d0333ce5-35e8-46c5-acae-90e4b2556397");

        locationItem.setName("Test2");
        locationItem.setLatitude(33.972741);
        locationItem.setLongitude(-84.733283);
        locationItem.setRadius(100.0);

        new Thread(new Runnable() {
            @Override
            public void run() {

                dynamoDBMapper.save(locationItem);

                // Item updated
            }
        }).start();
    }

    public void deleteLocation() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                location_db locationItem = new location_db();

                locationItem.setLocationId("23603ff2-2ae1-47ac-b002-a845b9f7ab2e");    //partition key

                dynamoDBMapper.delete(locationItem);

                // Item deleted
            }
        }).start();
    }

    public void createUserDB() {

        final User_DB newUser = new User_DB();

        newUser.setUserId("test2@email.com");

        newUser.setFirst_name("Rachel");
        newUser.setLast_name("Camp");
        newUser.setPassword("1234");
        //newUser.setLocations(locations);

        new Thread(new Runnable() {
            @Override
            public void run() {
                dynamoDBMapper.save(newUser);
                // Item saved
            }
        }).start();
    }

    public void readUser(final String id) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                User_DB userItem = dynamoDBMapper.load(
                        User_DB.class,
                        id);

                // Item read
                //Log.d("User Item", userItem.getFirst_name());
                if(userItem.getLocations() != null){
                    getSpots(userItem);
                }
            }
        }).start();
    }

    public void updateUser() {
        final User_DB userItem = new User_DB();

        userItem.setUserId("test@email.com");

        userItem.setFirst_name("Test");
        userItem.setLast_name("Camp");
        userItem.setPassword("1234");
        //newUser.setLocations(locations);

        new Thread(new Runnable() {
            @Override
            public void run() {

                dynamoDBMapper.save(userItem);

                // Item updated
            }
        }).start();
    }

    public void deleteUser() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                User_DB userItem = new User_DB();

                userItem.setUserId("test@email.com");    //partition key

                dynamoDBMapper.delete(userItem);

                // Item deleted
            }
        }).start();
    }

}