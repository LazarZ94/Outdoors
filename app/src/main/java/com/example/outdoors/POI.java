package com.example.outdoors;

public class POI {
    private String desc;
    private String lat;
    private String lon;
    private String img;

    public POI(String desc, String lat, String lon, String img){
        this.desc = desc;
        this.lat = lat;
        this.lon = lon;
        this.img = img;
    }

    public String getDesc(){return desc;}
    public String getLat() {return lat;}
    public String getLon() {return lon;}
    public String getImg() {return img;}
}
