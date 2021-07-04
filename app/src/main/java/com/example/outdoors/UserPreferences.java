package com.example.outdoors;

public class UserPreferences {

    public int userSearchRange;
    public int poiSearchRange;
    public boolean backgroundService;

    public UserPreferences() {}

    public UserPreferences(int uR, int pR, boolean bgS){
        this.userSearchRange = uR;
        this.poiSearchRange = pR;
        this.backgroundService = bgS;
    }

    public void setUserRange(int uR){
        this.userSearchRange = uR;
    }

    public void setPOIRange(int pR){
        this.poiSearchRange = pR;
    }

    public void setBackgroundService(boolean bgS){
        this.backgroundService = bgS;
    }

    public boolean getBackgroundService(){
        return this.backgroundService;
    }

    public int getUserRange() {
        return this.userSearchRange;
    }

    public int getPOIRange(){
        return this.poiSearchRange;
    }
}
