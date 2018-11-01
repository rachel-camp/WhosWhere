package com.example.savvy.whoswherev1;

import android.location.Location;

import java.util.Set;

public class user {
    String id;
    Set<String> spots;
    Location current;
    String firstName;
    String lastName;
    String password;
    user(){ }

    user(String id, Set<String> spots, String firstName, String lastName, String password){
        this.id = id;
        this.spots = spots;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        //this.current = current;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Set<String> getSpots() {
        return spots;
    }

    public void setSpots(Set<String> spots) {
        this.spots = spots;
    }

    public Location getCurrent() {
        return current;
    }

    public void setCurrent(Location current) {
        this.current = current;
    }
}
