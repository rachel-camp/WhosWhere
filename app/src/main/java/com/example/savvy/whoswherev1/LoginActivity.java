package com.example.savvy.whoswherev1;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.regex.Pattern;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;


public class LoginActivity extends AppCompatActivity {

    Button loginButton;
    Button newUser;
    EditText username;
    EditText password;
    ArrayList<String> createdUsernames = new ArrayList<>();
    DynamoDBMapper dynamoDBMapper;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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

    }
            public void newUserOnClick(View v) {
                final Intent intent = new Intent(getApplicationContext(), CreateUser.class);
                startActivity(intent);
                newUser = (Button) findViewById(R.id.button_createUsername);
            }
            public void logInOnCLick(View v){
                EditText email = (EditText) findViewById(R.id.et_email);
                EditText password = (EditText) findViewById(R.id.et_password);

                if(email.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(), "You need to enter an email!", Toast.LENGTH_LONG).show();
                } else if(password.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(), "You need to enter a password!", Toast.LENGTH_LONG).show();
                } else{
                    readUser(email.getText().toString());
                }
            }


        /*final Button login = (Button) findViewById(R.id.button_login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText email = (EditText) findViewById(R.id.et_email);
                EditText password = (EditText) findViewById(R.id.et_password);

                if(email.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(), "You need to enter an email!", Toast.LENGTH_LONG).show();
                } else if(password.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(), "You need to enter a password!", Toast.LENGTH_LONG).show();
                } else{
                    readUser(email.getText().toString());
                }
            }
        });

        /*login();
        newUser();
        Bundle b = getIntent().getExtras();

        if (b!=null)
            setCreatedUsernames();*/


    public void createUser(){
        final Intent intent = new Intent(getApplicationContext(), CreateUser.class);
        startActivity(intent);
    }

    public void readUser(final String email) {
        Toast.makeText(getApplicationContext(),"readUser running", Toast.LENGTH_SHORT).show();
        new Thread(new Runnable() {
            @Override
            public void run() {

                User_DB userItem = dynamoDBMapper.load(
                        User_DB.class,
                        email);


                // Item read
                //Log.d("User Item", userItem.getFirst_name());
                logInUser(userItem);
            }
        }).start();
    }

    public void logInUser(final User_DB userItem){

        EditText password = (EditText) findViewById(R.id.et_password);
        String pass = password.getText().toString();

        if(userItem == null){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "NOT A VALID EMAIL", Toast.LENGTH_LONG).show();
                }
            });
        }else if(pass.equals(userItem.getPassword())){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.putExtra("User", userItem.getUserId());
                    startActivity(intent);
                    //Toast.makeText(getApplicationContext(), "Golden", Toast.LENGTH_LONG).show();
                }
            });
        }else{
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "INCORRECT PASSWORD", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    /*// Password validation by comparing a set pattern to the pw input
    protected static boolean isValidPassword(String s) {
        Pattern PASSWORD_PATTERN
                = Pattern.compile(
                "123456");

        return !TextUtils.isEmpty(s) && PASSWORD_PATTERN.matcher(s).matches();
    }

    // Validates login username through String comparison, password is validated using isValidPassword()
    protected void login(){

        loginButton = findViewById(R.id.button_login);
        username = findViewById(R.id.et_email);
        password = findViewById(R.id.et_password);
        createdUsernames.add("Michael");
        createdUsernames.add("Savvy");
        createdUsernames.add("Rachel");

        loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {


                if ( isValidUsername(username.getText().toString()) && isValidPassword(password.getText().toString()) ) {
                    Intent loginSuccessActivity = new Intent(LoginActivity.this, LoginSuccess_Activity.class);
                    String s = username.getText().toString();
                    loginSuccessActivity.putExtra("username", s);
                    startActivity(loginSuccessActivity);
                }
                else {
                    Intent loginFailActivity = new Intent(LoginActivity.this, LoginFail_Activity.class);
                    startActivity(loginFailActivity);
                }

            }
        });

    }//end login()

    protected void newUser(){
        newUser = findViewById(R.id.button_createUsername);

        newUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, CreateUser.class);
                i.putStringArrayListExtra("usernames", createdUsernames);
                startActivity(i);
            }
        });
    }

    protected void setCreatedUsernames(){
        Intent i = getIntent();
        createdUsernames = i.getStringArrayListExtra("updated_usernames");
        System.out.println("Using Enhanced for loop");
        System.out.println("-----------------------");
        for (String str : createdUsernames) {
            System.out.println(str);
        }
    }

    protected boolean isValidUsername(String s){
        for (String str : createdUsernames) {
            if (s.equals(str)){
                return true;
            }

        }
        return false;
    }*/



}//end class