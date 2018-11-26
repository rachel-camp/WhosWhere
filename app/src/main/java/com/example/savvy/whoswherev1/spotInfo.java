package com.example.savvy.whoswherev1;


//TODO: Clean up imports - MG
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;

import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import java.util.ArrayList;
import java.util.List;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class spotInfo extends AppCompatActivity {
    NotificationReceiver receiver;
    IntentFilter filter;
    Intent broadcastIntent;

    TextView spotLat;
    TextView spotLong;
    TextView spotName;
    ListView spotMembers;
    Button leaveBtn;
    String userId;
    String locationId;
    String newUserId;
    List<String> memberListText = new ArrayList<>();
    Double lt;
    Double lg;

    DynamoDBMapper dynamoDBMapper;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spot_info_layout);

        userId = getIntent().getStringExtra("User");
        locationId = getIntent().getStringExtra("Location");

        // dynamic intent for broadcast
        filter = new IntentFilter();
        receiver = new NotificationReceiver();

        filter.addAction("test");
        broadcastIntent = new Intent();
        broadcastIntent.setAction("test");
        registerReceiver(receiver, filter);

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

        readLocation(locationId);

        spotLat = findViewById(R.id.spotLat);
        spotLong = findViewById(R.id.spotLong);
        spotName = findViewById(R.id.spotName);
        spotMembers = findViewById(R.id.mySpotsList);


        double lat = getSpotLat();
        String latStrng = ("" + lat);
        String longStrng = (""+getSpotLong());
        spotName.setText(getSpotName());
        spotLat.setText(latStrng);
        spotLong.setText(longStrng);

        final Switch checkIn = (Switch) findViewById(R.id.checkIn);
        checkIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!checkIn.isChecked()){
                    Toast.makeText(getApplicationContext(), "You are checked out of the spot", Toast.LENGTH_LONG).show();
                    checkOutReadUser(userId);
                }else {
                    checkInSpot(100, lt, lg);
                }
            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    public void checkInSpot(final double r, final double lat, final double lng){
        final FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // GPS location can be null if GPS is switched off
                            if (location != null) {
                                //onLocationChanged(location);
                                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                                double cX = lat;
                                double cY = lng;
                                double pX = latLng.latitude;
                                double pY = latLng.longitude;

                                float[] results = new float[1];

                                Location.distanceBetween(cX, cY, pX, pY, results);

                                if(results[0] < r) {
                                    Toast.makeText(getApplicationContext(), "You are checked in to the spot", Toast.LENGTH_LONG).show();
                                    checkInReadUser(userId);

                                } else {
                                    Toast.makeText(getApplicationContext(), "You are NOT in the spot", Toast.LENGTH_LONG).show();
                                    Switch checkIn = (Switch) findViewById(R.id.checkIn);
                                    checkIn.setChecked(!checkIn.isChecked());
                                }
                                //lat = location.getLatitude();
                                //longi = location.getLongitude();

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

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                8);
    }

    public void readLocation(final String id) {
        memberListText = new ArrayList<>();
        new Thread(new Runnable() {
            @Override
            public void run() {

                location_db locationItem = dynamoDBMapper.load(
                        location_db.class,
                        id);

                // Item read
                //Log.d("Location Item", locationItem.getName());

                writeInfo(locationItem);
            }
        }).start();
    }

    public void writeInfo(final location_db locationItem){
        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                spotLat = findViewById(R.id.spotLat);
                spotLong = findViewById(R.id.spotLong);
                spotName = findViewById(R.id.spotName);
                spotMembers = findViewById(R.id.mySpotsList);

                lt = locationItem.getLatitude();
                lg = locationItem.getLongitude();

                String latStrng = ("" + locationItem.getLatitude());
                String longStrng = ("" + locationItem.getLongitude());
                spotName.setText(locationItem.getName());
                spotLat.setText(latStrng);
                spotLong.setText(longStrng);

                List<String> membList = locationItem.getUsers();
                Object[] membIds = membList.toArray();
                String[] mem;
                for(int i = 0; i < membIds.length; i++){
                    MemberListReadUser(membIds[i].toString());
                }

            }
        });
    }

    public void MemberListReadUser(final String id) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                User_DB userItem = dynamoDBMapper.load(
                        User_DB.class,
                        id);

                // Item read
                //Log.d("User Item", userItem.getFirst_name());
                setMemberList(userItem);
            }
        }).start();
    }

    public void setMemberList(User_DB userItem){
        String name = userItem.getFirst_name() + " " + userItem.getLast_name();
        if(userItem.getCurrent_location() == null){
            name += " is not here.";

        }

        else if(userItem.getCurrent_location().equals(locationId)){
            name += " is here!";
            if(userItem.getUserId().equals(userId)){
                final Switch checkIn = (Switch) findViewById(R.id.checkIn);
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        checkIn.setChecked(true);
                        //TODO: Need Rachel to take a look and see how we can implement the broadcast to push to another user's phone. - MG
                        //TODO: for now a notification triggers when user checks in - MG
                        sendBroadcast(broadcastIntent);


                    }
                });
            }
        }
        else name += " is not here.";

        memberListText.add(name);
        final ListView members = findViewById(R.id.mySpotsList);
        final ArrayAdapter<String> arrayAdapter= new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, memberListText);
        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                members.setAdapter(arrayAdapter);

            }
        });
    }

    public void checkInReadUser(final String id) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                User_DB userItem = dynamoDBMapper.load(
                        User_DB.class,
                        id);

                // Item read
                //Log.d("User Item", userItem.getFirst_name());
                checkInUpdateUser(userItem);

            }
        }).start();
    }

    public void checkInUpdateUser(User_DB user) {
        final User_DB userItem = new User_DB();

        userItem.setUserId(user.getUserId());

        userItem.setFirst_name(user.getFirst_name());
        userItem.setLast_name(user.getLast_name());
        userItem.setPassword(user.getPassword());
        userItem.setLocations(user.getLocations());
        userItem.setCurrent_location(locationId);

        new Thread(new Runnable() {
            @Override
            public void run() {

                dynamoDBMapper.save(userItem);

                // Item updated

                readLocation(locationId);

            }
        }).start();
    }

    public void checkOutReadUser(final String id) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                User_DB userItem = dynamoDBMapper.load(
                        User_DB.class,
                        id);

                // Item read
                //Log.d("User Item", userItem.getFirst_name());
                checkOutUpdateUser(userItem);

            }
        }).start();
    }

    public void checkOutUpdateUser(User_DB user) {
        final User_DB userItem = new User_DB();

        userItem.setUserId(user.getUserId());

        userItem.setFirst_name(user.getFirst_name());
        userItem.setLast_name(user.getLast_name());
        userItem.setPassword(user.getPassword());
        userItem.setLocations(user.getLocations());

        new Thread(new Runnable() {
            @Override
            public void run() {

                dynamoDBMapper.save(userItem);

                // Item updated

                readLocation(locationId);
            }
        }).start();
    }

    public void addUserOnClick(View v) {

        EditText addUser = (EditText) findViewById(R.id.addUserET);
        if(addUser.getText().toString().equals("")){
            Toast.makeText(getApplicationContext(), "You need to enter an email!", Toast.LENGTH_LONG).show();
        } else {
            addUserReadUser(addUser.getText().toString());
        }
    }

    public void addUserReadUser(final String id) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                User_DB userItem = dynamoDBMapper.load(
                        User_DB.class,
                        id);

                // Item read
                //Log.d("User Item", userItem.getFirst_name());

                if(userItem != null){
                    newUserId = userItem.getUserId();
                    addUserUpdateUser(userItem);
                }else {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            Toast.makeText(getApplicationContext(), "Not a valid email", Toast.LENGTH_LONG).show();

                        }
                    });
                }
            }
        }).start();
    }

    public void addUserUpdateUser(User_DB user) {
        final User_DB userItem = new User_DB();

        userItem.setUserId(user.getUserId());

        List<String> locations = new ArrayList();
        locations = user.getLocations();
        if(locations == null){
            List<String> location = new ArrayList();
            location.add(locationId);
            userItem.setLocations(location);
        }else{
            locations.add(locationId);
            userItem.setLocations(locations);
        }

        userItem.setFirst_name(user.getFirst_name());
        userItem.setLast_name(user.getLast_name());
        userItem.setPassword(user.getPassword());

        new Thread(new Runnable() {
            @Override
            public void run() {

                dynamoDBMapper.save(userItem);

                // Item updated
                addUserReadLocation(locationId);
            }
        }).start();
    }

    public void addUserReadLocation(final String id) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                location_db locationItem = dynamoDBMapper.load(
                        location_db.class,
                        id);

                // Item read
                //Log.d("Location Item", locationItem.getName());
                addUserUpdateLocation(locationItem);

            }
        }).start();
    }

    public void addUserUpdateLocation(location_db location) {
        final location_db locationItem = new location_db();

        List<String> users = new ArrayList();
        users = location.getUsers();
        users.add(newUserId);

        locationItem.setLocationId(location.getLocationId());

        locationItem.setName(location.getName());
        locationItem.setLatitude(location.getLatitude());
        locationItem.setLongitude(location.getLongitude());
        locationItem.setRadius(location.getRadius());
        locationItem.setUsers(users);

        new Thread(new Runnable() {
            @Override
            public void run() {

                dynamoDBMapper.save(locationItem);

                // Item updated
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        Toast.makeText(getApplicationContext(), "New user added", Toast.LENGTH_LONG).show();

                    }
                });
                readLocation(locationId);
            }
        }).start();
    }

    public String getSpotName()
    {
        String name = "Name";
        //this needs to get the name of the spot selected from the database
        return name;
    }

    public double getSpotLat()
    {
        double lat = 0;
        //this needs to get the latitude of the spot selected from the database
        return lat;
    }

    public double getSpotLong()
    {
        double lng = 0;
        //this needs to get longitude of the spot selected from the database
        return lng;
    }

    public void leaveOnClick(View v){
        //this needs to remove the user from the spotMembers in the database
    }


}
