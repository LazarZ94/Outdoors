package com.example.outdoors;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

public class BaseDrawerActivity extends AppCompatActivity {

    UserList userListInst = UserList.getInstance();

    String currId = userListInst.getCurrentUserID();
    User currUser = userListInst.getCurrentUser();

    static final private int MAIN_SCREEN = 0;
    static final private int PROFILE = 1;
    static final private int FRIEND_LIST = 2;

    DrawerLayout drawer;
    ActionBarDrawerToggle toggle;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_drawer);


        NavigationView navView = (NavigationView) findViewById(R.id.navView);

        toolbar = (Toolbar) findViewById(R.id.activityBaseToolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        setTitle("Outdoors");

        drawer = (DrawerLayout) findViewById(R.id.baseDrawer);
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navDrawerOpen, R.string.navDrawerClose);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        View headerView = navView.getHeaderView(0);

        TextView navUsername = (TextView) headerView.findViewById(R.id.navHeaderUsername);
        navUsername.setText(currUser.getUsername());

        TextView navEmail = (TextView) headerView.findViewById(R.id.navHeaderEmail);
        navEmail.setText(currUser.getEmail());

        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.navMap:
                        selectActivity(MAIN_SCREEN);
                        drawer.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.navProfile:
                        selectActivity(PROFILE);
                        drawer.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.navFriends:
                        selectActivity(FRIEND_LIST);
                        drawer.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.navLogOut:
                        drawer.closeDrawer(GravityCompat.START);
                        showDialog();
                        //logOut();
                        break;
                }
                return true;
            }
        });
    }


    private void selectActivity(int selected){
        Intent intent = null;
        switch (selected){
            case 0:
                if(!(this instanceof MainScreen)) {
                    intent = new Intent(getApplicationContext(), MainScreen.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                }
                break;
            case 1:
                if(!(this instanceof UserProfile) || !(((UserProfile) this).user.equals(currUser))) {
                    Bundle userBundle = new Bundle();
                    userBundle.putString("userID", currId);
                    intent = new Intent(getApplicationContext(), UserProfile.class);
                    intent.putExtras(userBundle);
                }
                break;
            case 2:
                if(!(this instanceof FriendListActivity)){
                    intent = new Intent(getApplicationContext(), FriendListActivity.class);
                }
                break;
        }
        startActivity(intent);
    }

    protected void setTitle(String title){
        toolbar.setTitle(title);
    }


    protected void showDialog(){
        new AlertDialog.Builder(this)
                .setTitle("Log Out")
                .setMessage("Do you want to log out?")
                .setPositiveButton(R.string.acceptBTReq, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        userListInst.logOut(getApplicationContext());
                        finish();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

}