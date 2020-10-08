package com.example.outdoors;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainScreen extends BaseDrawerActivity implements LocationListener {

    private static String TAG = "MAIN SCREEN";

    private FirebaseAuth mAuth = DBAuth.getInstance().getAuth();

    private UserList userListInst = UserList.getInstance();

    private GoogleSignInClient gsc = userListInst.getGSC();

    private FirebaseFirestore db = DBAuth.getInstance().getDB();

    private FirebaseDatabase fbdb = DBAuth.getInstance().getFBDB();

    boolean doubleTap = false;

    final String currId = mAuth.getCurrentUser().getUid();

    static final int PERMISSION_FINE_LOC = 0;

    static final int PERMISSION_COARSE_LOC = 1;

    private MapView map = null;

    IMapController mapController = null;

    MyLocationNewOverlay myLocationOverlay;

    ItemizedOverlay<OverlayItem> usersOverlay = null;

    Context context;

    private volatile boolean stopThread = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main_screen);

        FrameLayout contentLayout = (FrameLayout) findViewById(R.id.contentFrame);
        getLayoutInflater().inflate(R.layout.activity_main_screen, contentLayout);

        context = getApplicationContext();
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        map = (MapView) findViewById(R.id.map);
        map.setMultiTouchControls(true);

        statusCheck();


        mapController = map.getController();

        if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_FINE_LOC);
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_COARSE_LOC);
        } else {
            LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,this);
            manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0,this);
            setLocationOverlay();
        }

        if (mapController != null) {
            mapController.setZoom(16.0);
            GeoPoint startPoint = new GeoPoint(43.3209, 21.8958);
            mapController.setCenter(startPoint);
        }

        ArrayList<User> onlineUsers = new ArrayList<>();

        ArrayList<User> offlineUsers = new ArrayList<>();

        for (User usr : userListInst.getUserList()) {
            if (usr.getStatus()) {
                onlineUsers.add(usr);
            } else {
                offlineUsers.add(usr);
            }
        }

        Log.d(TAG, "ONLINE USERS: " + onlineUsers);

        Log.d(TAG, "OFFLINE USERS: " + offlineUsers);

        Toast.makeText(this, "Online users: " + onlineUsers.size(), Toast.LENGTH_SHORT).show();

        /*ArrayList<User> userList = userListInst.getUserList();

        ListView requests = (ListView) findViewById(R.id.mainScreenReuests);

        Log.d(TAG, "CURRENT USER JE " + currUser.toString());

        Log.d(TAG, "CURRUSER FRIENDREQUESTS : " + currUser.getFriendRequests());

        ArrayList<String> reqList = new ArrayList<>();
        for(String id : currUser.getFriendRequests()){
            reqList.add(userListInst.getUser(id).getUsername());
        }

        requests.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, reqList));


        requests.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                /*String uid = UserList.getInstance().getUserId((User) adapterView.getAdapter().getItem(i));
                Map<String, Object> currAddFr = new HashMap<>();
                currAddFr.put("id", uid);
                Map<String, Object> otherAddFr = new HashMap<>();
                otherAddFr.put("id", currId);
                db.collection("users")
                        .document(currId).collection("friends").add(currAddFr);
                db.collection("users")
                        .document(uid).collection("friends").add(otherAddFr);*/
                /*String userID = userListInst.getUserId((String) adapterView.getAdapter().getItem(i));
                User other = userListInst.getUser(userID);
                ArrayList<String> currFReq = new ArrayList<>(currUser.friendRequests);
                currFReq.remove(userID);
                db.collection("users").document(currId).update("friendRequests", currFReq);
                ArrayList<String> otherSentFR = new ArrayList<>(other.sentFriendRequests);
                otherSentFR.remove(currId);
                db.collection("users").document(userID).update("sentFriendRequests", otherSentFR);
                ArrayList<String> currFriends = new ArrayList<>(currUser.friends);
                currFriends.add(userID);
                db.collection("users").document(currId).update("friends", currFriends);
                ArrayList<String> otherFriends = new ArrayList<>(other.friends);
                otherFriends.add(currId);
                db.collection("users").document(userID).update("friends", otherFriends);
                userListInst.updateUsers(MainScreen.this);
            }

        });


        ListView users = (ListView) findViewById(R.id.mainScreenUserList);

        userList.remove(currUser);

        users.setAdapter(new ArrayAdapter<User>(this, android.R.layout.simple_list_item_1, userList));


        users.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Bundle userBundle = new Bundle();
                String uid = userListInst.getUserId((User) adapterView.getAdapter().getItem(i));
                userBundle.putString("userID", uid);
                Intent intent = new Intent(MainScreen.this, UserProfile.class);
                intent.putExtras(userBundle);
                startActivity(intent);
            }
        });*/


    }

    private void updateUserPositions(){
        stopThread = false;
        Log.d(TAG, "updateUserPositions: starting thread");
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    if(stopThread) {
                        Log.d(TAG, "run: stopping thread");
                        return;
                    }
                    try {
                        Log.d(TAG, "run: sleeping 2");
                        Thread.sleep(2000);
                        Log.d(TAG, "run: updating users");
                        showUsers();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void showUsers(){
        if(usersOverlay!=null){
            this.map.getOverlays().remove(usersOverlay);
        }
        final ArrayList<OverlayItem> users = new ArrayList<OverlayItem>();
        for(User user : userListInst.getOnlineUsers()){
            Log.d(TAG,"ONLINE JE " + user.getUsername() + "COORDS: " + user.lat + "  :  " + user.lon);
            OverlayItem item = new OverlayItem(user.getUsername(), user.getEmail(), new GeoPoint(user.lat,user.lon));
            item.setMarker(ContextCompat.getDrawable(getApplicationContext(), R.drawable.baseline_person_outline_black_18dp));
            users.add(item);
        }

        Log.d(TAG, "USEWRS" + users);
        usersOverlay = new ItemizedIconOverlay<OverlayItem>(users,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(int index, OverlayItem item) {
                        Intent profileIntent = new Intent(MainScreen.this, UserProfile.class);
                        profileIntent.putExtra("userID", userListInst.getUserId(users.get(index).getTitle()));
                        startActivity(profileIntent);
                        return true;
                    }

                    @Override
                    public boolean onItemLongPress(int index, OverlayItem item) {
                        return false;
                    }
                }, getApplicationContext());
        this.map.getOverlays().add(usersOverlay);
        Log.d(TAG, "showUsers: USERS UPDATED");
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        DatabaseReference lonRef = fbdb.getReference("users/"+currId+"/lon");
        DatabaseReference latRef = fbdb.getReference("users/"+currId+"/lat");
        lonRef.setValue(location.getLongitude());
        latRef.setValue(location.getLatitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_FINE_LOC:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                    manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
                    setLocationOverlay();
                }
                break;
            case PERMISSION_COARSE_LOC:
                break;
        }
    }

    private void setLocationOverlay() {
        mapController.setZoom(16.0);
        this.myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(context), map);
        this.myLocationOverlay.enableFollowLocation();
        this.myLocationOverlay.enableMyLocation();
        map.getOverlays().add(this.myLocationOverlay);
        showUsers();
    }


    @Override
    protected void onResume() {
        super.onResume();
        map.onResume();
        //showUsers();
        updateUserPositions();
    }

    @Override
    protected void onPause() {
        super.onPause();
        map.onPause();
        stopThread = true;
    }


    public void statusCheck() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();

        }

    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }else {
            if(doubleTap) {
                super.onBackPressed();
                return;
            }

            this.doubleTap = true;
            Toast.makeText(this, "Tap again to exit!", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleTap = false;
                }
            }, 2000);
        }
    }
}