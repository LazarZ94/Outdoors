/*

Glavni ekran (mapa)

 */


package com.example.outdoors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static java.security.AccessController.getContext;

public class MainScreenActivity extends BaseDrawerActivity implements LocationListener {

    private static String TAG = "MAIN SCREEN";

    private FirebaseAuth mAuth = DBAuth.getInstance().getAuth();

    private UserList userListInst = UserList.getInstance();

    private GoogleSignInClient gsc = userListInst.getGSC();

    private FirebaseFirestore db = DBAuth.getInstance().getDB();

    private FirebaseDatabase fbdb = DBAuth.getInstance().getFBDB();

    private FirebaseStorage fbs = DBAuth.getInstance().getStorage();

    boolean doubleTap = false;

    final String currId = mAuth.getCurrentUser().getUid();

    static final int PERMISSION_FINE_LOC = 0;

    static final int PERMISSION_COARSE_LOC = 1;

    static final int REQUEST_IMAGE_CAPTURE = 5;

    String currentPhotoPath;

    private MapView map = null;

    IMapController mapController = null;

    MyLocationNewOverlay myLocationOverlay;

    ItemizedOverlay<OverlayItem> usersOverlay = null;

    Context context;

    Double currentLat = null, currentLon = null;

    FloatingActionButton fab;

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

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if(cameraIntent.resolveActivity(getPackageManager()) != null){
                    File photoFile = null;
                    photoFile = createImageFile();

                    if(photoFile != null){
                        Uri photoURI = FileProvider.getUriForFile(MainScreenActivity.this, "com.example.android.fileprovider", photoFile);
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
                    }
                }
            }
        });


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

        Log.d(TAG, "onActivityResult: MAINSCR CONTEXT" + getContext());

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

    public void uploadedPOI(){
        Toast.makeText(this, "Photo uploaded!", Toast.LENGTH_SHORT).show();
        fab.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            fab.setVisibility(View.INVISIBLE);

            //POIFragment poiFragment = POIFragment.newInstance(currentPhotoPath, currentLat.toString(), currentLon.toString(), currId);

            POIFragment poiFragment = new POIFragment();

            Bundle args = new Bundle();
            args.putString("img", currentPhotoPath);
            args.putString("lat", currentLat.toString());
            args.putString("lon", currentLon.toString());
            args.putString("currID", currId);
            poiFragment.setArguments(args);

            getSupportFragmentManager().beginTransaction().replace(R.id.containerPOI, poiFragment).commit();

        }
    }

    private File createImageFile(){
        String timestamp = new SimpleDateFormat("yyyyMM_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timestamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        Log.d(TAG, "createImageFile: storagedir" + storageDir.getAbsolutePath());
        try {
            File image = File.createTempFile(imageFileName, ".jpg", storageDir);
            currentPhotoPath = image.getAbsolutePath();
            return image;
        }catch (Exception e){
            Log.d(TAG, "createImageFile: ", e);
        }
        return  null;
    }

    /*private void setNavHeaderImg(){
        NavigationView navView = (NavigationView) findViewById(R.id.navView);
        View headerView = navView.getHeaderView(0);
        Log.d("AAAAAAAAAAAAA", "AVATAR = " + currUser.getAvatar());
        ImageView avatarView = (ImageView) headerView.findViewById(R.id.navHeaderAvatar);
        if(currUser.getAvatar()!=null){
            avatarView.setImageBitmap(currUser.getAvatar());
        }
    }*/

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
                        Intent profileIntent = new Intent(MainScreenActivity.this, UserProfileActivity.class);
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
        currentLat = location.getLatitude();
        currentLon = location.getLongitude();
        lonRef.setValue(currentLon);
        latRef.setValue(currentLat);
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