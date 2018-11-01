package com.example.savvy.whoswherev1;


import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.Set;



@DynamoDBTable(tableName = "whoswhere-mobilehub-99353394-Users")

public class User_DB {
    private String _userId;
    private String _current_location;
    private String _first_name;
    private String _last_name;
    private List<String> _locations;
    private String _password;

    @DynamoDBHashKey(attributeName = "userId")
    @DynamoDBAttribute(attributeName = "userId")
    public String getUserId() {
        return _userId;
    }

    public void setUserId(final String _userId) {
        this._userId = _userId;
    }
    @DynamoDBAttribute(attributeName = "current_location")
    public String getCurrent_location() {
        return _current_location;
    }

    public void setCurrent_location(final String _current_location) {
        this._current_location = _current_location;
    }
    @DynamoDBAttribute(attributeName = "first_name")
    public String getFirst_name() {
        return _first_name;
    }

    public void setFirst_name(final String _first_name) {
        this._first_name = _first_name;
    }
    @DynamoDBAttribute(attributeName = "last_name")
    public String getLast_name() {
        return _last_name;
    }

    public void setLast_name(final String _last_name) {
        this._last_name = _last_name;
    }
    @DynamoDBAttribute(attributeName = "locations")
    public List<String> getLocations() {
        return _locations;
    }

    public void setLocations(final List<String> _locations) {
        this._locations = _locations;
    }
    @DynamoDBAttribute(attributeName = "password")
    public String getPassword() {
        return _password;
    }

    public void setPassword(final String _password) {
        this._password = _password;
    }

}
