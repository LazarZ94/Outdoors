package com.example.outdoors;

public class Invite {

    public String inviteID;
    public boolean seen;

    public Invite(){}

    public Invite(String id){
        this.inviteID = id;
        this.seen = false;
    }


    public String getInviteID(){
        return this.inviteID;
    }

    public boolean isSeen(){
        return this.seen;
    }

    public void setSeen(){
        this.seen = true;
    }

}
