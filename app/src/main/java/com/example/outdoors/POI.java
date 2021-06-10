package com.example.outdoors;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

@IgnoreExtraProperties
public class POI {
    public String desc;
    public double lat;
    public double lon;
    public String uID;
    public ArrayList<String> likes = new ArrayList<String>();
    @Exclude public String name;
    @Exclude public Bitmap thumb;

    public POI(){}

    public POI(String desc, String lat, String lon, String name, String uID, ArrayList<String> likes){
        this.desc = desc;
        this.lat = Double.parseDouble(lat);
        this.lon = Double.parseDouble(lon);
        this.name = name;
        this.uID = uID;
        this.likes = likes;
        this.thumb = null;

    }

    public void setThumb(Bitmap thumb) {this.thumb = thumb;}

    public String getDesc() {return desc;}
    public double getLat() {return lat;}
    public double getLon() {return lon;}
    public String getName() {return name;}
    public String getuID() {return uID;}
    public ArrayList<String> getLikes() {return likes;}
    public Bitmap getThumb() {return thumb;}


}
