package com.example.outdoors;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;

@IgnoreExtraProperties
public class User {

    FirebaseDatabase fbdb = DBAuth.getInstance().getFBDB();

    public DatabaseReference statusRef = null;


    UserList userListInst = UserList.getInstance();

    public String email;
    public String username;
    public String fName;
    public String lName;
    public String phoneNumber;
    public ArrayList<String> friends = new ArrayList<>();
    public ArrayList<String> friendRequests = new ArrayList<>();
    public ArrayList<String> sentFriendRequests = new ArrayList<>();
    @Exclude public boolean onlineStatus;
    @Exclude public Uri img;
    @Exclude public double lat;
    @Exclude public double lon;
    public User(){

    }

    public User(String email, String username, String fName, String lName,
                String phone, ArrayList<String> friends, ArrayList<String> fReq,  ArrayList<String> sentFR){
        this.email = email;
        this.username = username;
        this.fName = fName;
        this.lName = lName;
        this.phoneNumber = phone;
        this.friends = new ArrayList<>(friends);
        this.friendRequests = new ArrayList<>(fReq);
        this.sentFriendRequests = new ArrayList<>(sentFR);
        this.onlineStatus = false;
        this.img = null;
    }

    public void setLocationListener(String UID){
        DatabaseReference latRef = fbdb.getReference("users/"+ UID + "/lat");
        DatabaseReference lonRef = fbdb.getReference("users/"+ UID + "/lon");

        latRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                lat = snapshot.getValue(double.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
         lonRef.addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot snapshot) {
                 lon = snapshot.getValue(double.class);
             }

             @Override
             public void onCancelled(@NonNull DatabaseError error) {

             }
         });
    }

    public void setStatusListener(String UID){
        statusRef = fbdb.getReference("users/" + UID + "/onlineStatus");
        //statusRef.setValue(false);

        statusRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                onlineStatus = snapshot.getValue(Boolean.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    public void setStatus(boolean status){ this.onlineStatus = status;}

    public boolean getStatus() { return onlineStatus;}

    @Override
    public String toString(){
        return username;
    }

    public String getEmail() {return email;}

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
