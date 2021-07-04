package com.example.outdoors;

import java.util.ArrayList;
import java.util.Date;

public class Plan {

    public String planTitle;
    public double lat, lon;
    public Date date;
    public String createdBy;
    public ArrayList<String> invites = new ArrayList();
    public ArrayList<String> confirmed = new ArrayList();

    public Plan(){}

    public Plan(String title, double lat, double lon, Date date, String createdBy, ArrayList<String> invites){
        this.planTitle = title;
        this.lat = lat;
        this.lon = lon;
        this.date = date;
        this.createdBy = createdBy;
        this.invites = invites;
        this.confirmed.add(createdBy);
    }
}
