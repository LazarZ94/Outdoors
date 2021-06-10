package com.example.outdoors;

import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

public class LeaderboardActivity extends BaseDrawerActivity {

    UserList userListInst = UserList.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main_screen);

        FrameLayout contentLayout = (FrameLayout) findViewById(R.id.contentFrame);
        getLayoutInflater().inflate(R.layout.activity_leaderboard, contentLayout);


        LinearLayout scrollLayout = (LinearLayout) findViewById(R.id.leaderboardLayout);

        ArrayList<User> users = userListInst.getUserList();

        Collections.sort(users);


        for(int i=0; i<10 && i<users.size(); i++){
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.HORIZONTAL);
            TextView usrname = new TextView(this);
            TextView points = new TextView(this);
            User usr = users.get(i);
            usrname.setText(usr.getUsername());
            points.setText(String.valueOf(usr.getPoints()));
            layout.addView(usrname);
            layout.addView(points);
            scrollLayout.addView(layout);
        }


    }


}
