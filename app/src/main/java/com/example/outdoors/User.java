package com.example.outdoors;

import android.net.Uri;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;

@IgnoreExtraProperties
public class User {
    public String email;
    public String username;
    public String fName;
    public String lName;
    public String phoneNumber;
    @Exclude public final ArrayList<String> friends = new ArrayList<>();
    @Exclude public final ArrayList<String> friendRequests = new ArrayList<>();
    @Exclude public final ArrayList<String> sentFriendRequests = new ArrayList<>();
    @Exclude public Uri img;

    public User(){

    }

    public User(String email, String username, String fName, String lName, String phone){
        this.email = email;
        this.username = username;
        this.fName = fName;
        this.lName = lName;
        this.phoneNumber = phone;
        //this.friends = new ArrayList<>();
        //this.friendRequests = new ArrayList<>();
        //this.sentFriendRequests = new ArrayList<>();
        this.img = null;
    }

    @Override
    public String toString(){
        return username;
    }


    public ArrayList<String> getFriends(){
        return friends;
    }

    public ArrayList<String> getFriendRequests(){
        return friendRequests;
    }

    public ArrayList<String> getSentFriendRequests(){
        return sentFriendRequests;
    }

    public String getUsername(){
        return username;
    }

    public String getfName(){
        return fName;
    }

    public String getlName(){
        return lName;
    }

    public String getPhoneNumber(){
        return phoneNumber;
    }

    public void setUsername(String username){
        this.username = username;
    }

    public void setfName(String fName){
        this.fName = fName;
    }

    public void setlName(String lName){
        this.lName = lName;
    }

    public void setPhoneNumber(String phone){
        this.phoneNumber = phone;
    }
}
