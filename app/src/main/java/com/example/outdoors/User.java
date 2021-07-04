/*

User klasa

 */


package com.example.outdoors;

import android.graphics.Bitmap;
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
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class User implements Comparable {

    FirebaseDatabase fbdb = DBAuth.getInstance().getFBDB();

    public DatabaseReference statusRef = null;


    UserList userListInst = UserList.getInstance();

    String TAG = "USER CLASS";

    public String email;
    public String username;
    public String fName;
    public String lName;
    public String phoneNumber;
    public ArrayList<String> friends = new ArrayList<>();
    public ArrayList<String> friendRequests = new ArrayList<>();
    public ArrayList<String> sentFriendRequests = new ArrayList<>();
    public ArrayList<Plan> userPlans = new ArrayList<>();
    public ArrayList<Invite> planInvites = new ArrayList<>();
    public UserPreferences prefs;
    @Exclude public boolean onlineStatus;
    @Exclude public Bitmap img;
    @Exclude public double lat;
    @Exclude public double lon;
    @Exclude public ArrayList<StorageReference> POIs = new ArrayList<>();
    @Exclude public Map<String, StorageMetadata> POIMetadata = new HashMap<String, StorageMetadata>();
    public User(){

    }

    public User(String email, String username, String fName, String lName,
                String phone, ArrayList<String> friends, ArrayList<String> fReq,  ArrayList<String> sentFR,
                ArrayList<Plan> userPlans, ArrayList<Invite> planInvites, UserPreferences prefs){
        this.email = email;
        this.username = username;
        this.fName = fName;
        this.lName = lName;
        this.phoneNumber = phone;
        this.friends = new ArrayList<>(friends);
        this.friendRequests = new ArrayList<>(fReq);
        this.sentFriendRequests = new ArrayList<>(sentFR);
        this.userPlans = new ArrayList<>(userPlans);
        this.planInvites = new ArrayList<>(planInvites);
        this.prefs = prefs;
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

    public ArrayList<Plan> getUserPlans(){
        return this.userPlans;
    }

    public ArrayList<Invite> getPlanInvites(){
        return this.planInvites;
    }

    public void addUserPlan(Plan plan){
        this.userPlans.add(plan);
    }

    public void setAvatar(Bitmap avatar){
        this.img = avatar;
    }

    public Bitmap getAvatar(){
        return this.img;
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

    public void setFriends(ArrayList<String> fr){
        this.friends = fr;
    }

    public ArrayList<String> getFriendRequests(){
        return friendRequests;
    }

    public void setFriendRequests(ArrayList<String> fReqs){
        this.friendRequests = fReqs;
    }

    public ArrayList<String> getSentFriendRequests(){
        return sentFriendRequests;
    }

    public void setSentRequests(ArrayList<String> sentReqs){
        this.sentFriendRequests = sentReqs;
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

    public void setPreferences(UserPreferences prefs){
        this.prefs = prefs;
    }

    public UserPreferences getPreferences(){
        return this.prefs;
    }

    public int getPoints(){
        ArrayList<POI> pois = userListInst.getPOIs();
        int points = 0;
        String id = userListInst.getUserId(this);
        for(POI poi : pois){
            if(poi.getuID().equals(id)){
                points += poi.getLikes().size();
            }
        }
        return points;
    }

    @Override
    public int compareTo(Object o) {
        int rank = ((User) o).getPoints();

        return rank-this.getPoints();
    }
}
