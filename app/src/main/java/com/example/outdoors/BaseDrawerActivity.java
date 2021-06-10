/*

Super klasa za activity-je, dodaje drawer

 */

package com.example.outdoors;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;

public class BaseDrawerActivity extends AppCompatActivity {

    UserList userListInst = UserList.getInstance();

    String currId = userListInst.getCurrentUserID();
    User currUser = userListInst.getCurrentUser();

    static final private int MAIN_SCREEN = 0;
    static final private int PROFILE = 1;
    static final private int FRIEND_LIST = 2;
    static final private int MY_PLACES = 3;


    static final private int LEADERBOARD = 6;
    static final private int INVITES = 7;
    static final private int SETTINGS = 8;

    boolean avatarSet = false;

    NavigationView navView;

    View headerView;

    DrawerLayout drawer;
    ActionBarDrawerToggle toggle;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_drawer);

        navView = (NavigationView) findViewById(R.id.navView);

        toolbar = (Toolbar) findViewById(R.id.activityBaseToolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        setTitle("Outdoors");

        drawer = (DrawerLayout) findViewById(R.id.baseDrawer);
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navDrawerOpen, R.string.navDrawerClose){
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                if(!avatarSet) {
                    Log.d("AAAAAAAAAAAAA", "AVATAR = " + currUser.getAvatar());
                    ImageView avatarView = (ImageView) headerView.findViewById(R.id.navHeaderAvatar);
                    if (currUser.getAvatar() != null) {
                        avatarView.setImageBitmap(currUser.getAvatar());
                        avatarSet = true;
                    }
                }
            }
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        headerView = navView.getHeaderView(0);


        /*Log.d("AAAAAAAAAAAAA", "AVATAR = " + currUser.getAvatar());
        ImageView avatarView = (ImageView) headerView.findViewById(R.id.navHeaderAvatar);
        if(currUser.getAvatar()!=null){
            avatarView.setImageBitmap(currUser.getAvatar());
        }*/

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
                    case R.id.navPlaces:
                        selectActivity(MY_PLACES);
                        drawer.closeDrawer(GravityCompat.START);
                        break;


                    case R.id.navLeaderboard:
                        selectActivity(LEADERBOARD);
                        drawer.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.navInvites:
                        selectActivity(INVITES);
                        drawer.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.navSettings:
                        selectActivity(SETTINGS);
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

    /*@Override
    protected void onResume() {
        super.onResume();
        Log.d("AAAAAAAAAAAAA", "AVATAR = " + currUser.getAvatar());
        ImageView avatarView = (ImageView) headerView.findViewById(R.id.navHeaderAvatar);
        if(currUser.getAvatar()!=null){
            avatarView.setImageBitmap(currUser.getAvatar());
        }
    }*/

    private void selectActivity(int selected){
        Intent intent = null;
        switch (selected){
            case MAIN_SCREEN:
                if(!(this instanceof MainScreenActivity)) {
                    intent = new Intent(getApplicationContext(), MainScreenActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                }
                break;
            case PROFILE:
                if(!(this instanceof UserProfileActivity) || !(((UserProfileActivity) this).user.equals(currUser))) {
                    Bundle userBundle = new Bundle();
                    userBundle.putString("userID", currId);
                    intent = new Intent(getApplicationContext(), UserProfileActivity.class);
                    intent.putExtras(userBundle);
                }
                break;
            case FRIEND_LIST:
                if(!(this instanceof FriendListActivity)){
                    intent = new Intent(getApplicationContext(), FriendListActivity.class);
                }
                break;
            case MY_PLACES:
                if(!(this instanceof PlacesActivity)){
                    intent = new Intent(getApplicationContext(), PlacesActivity.class);
                }
                break;

            case LEADERBOARD:
                if(!(this instanceof LeaderboardActivity)){
                    intent = new Intent(getApplicationContext(), LeaderboardActivity.class);
                }
                break;
            case INVITES:
                if(!(this instanceof InvitesActivity)){
                    intent = new Intent(getApplicationContext(), InvitesActivity.class);
                }
                break;
            case SETTINGS:
                if(!(this instanceof SettingsActivity)){
                    intent = new Intent(getApplicationContext(), SettingsActivity.class);
                }
                break;
        }
        if(intent != null)
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