/*

Glavni ekran (mapa)

 */


package com.example.outdoors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.api.LogDescriptor;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.Marker;
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

    ItemizedOverlay<OverlayItem> poiOverlay = null;

    Context context;

    Double currentLat = null, currentLon = null;

    FloatingActionButton fab;

    Double poiLat, poiLon;

    FilterFrag filterFrag = new FilterFrag();

    boolean filterAdd = false;

    int userRange, poiRange;

    boolean serviceToggle;

    private volatile boolean stopThread = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout contentLayout = (FrameLayout) findViewById(R.id.contentFrame);
        getLayoutInflater().inflate(R.layout.activity_main_screen, contentLayout);


        context = getApplicationContext();
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        map = (MapView) findViewById(R.id.map);
        map.setMultiTouchControls(true);

        LinearLayout toolbarLayout  = (LinearLayout) findViewById(R.id.toolbarItems);
        toolbarLayout.setGravity(Gravity.RIGHT);
        toolbarLayout.setPadding(0,15,30,15);


        UserPreferences usrPrefs = currUser.getPreferences();

        userRange = usrPrefs != null ? usrPrefs.getUserRange() : 25;
        poiRange = usrPrefs != null ? usrPrefs.getPOIRange() : 25;


        ImageButton filterButt = new ImageButton(this);
        filterButt.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_filter_list_24));
        filterButt.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        filterButt.setColorFilter(Color.WHITE);
        filterButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(filterAdd){
                    filterFrag.closeFragment();
                    fab.setVisibility(View.VISIBLE);
                    filterAdd = false;
                }else {
                    fab.setVisibility(View.INVISIBLE);

                    Bundle args = new Bundle();
                    args.putInt("userRange", userRange);
                    args.putInt("poiRange", poiRange);
                    filterFrag.setArguments(args);

                    getSupportFragmentManager().beginTransaction().replace(R.id.containerPOI, filterFrag).commit();
                    filterAdd = true;
                }
            }
        });
        toolbarLayout.addView(filterButt);

        setTitle("Outdoors");

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
            GeoPoint startPoint;
            mapController.setZoom(16.0);
            startPoint = new GeoPoint(43.3209, 21.8958);
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


    }

    public void setRanges(int uR, int pR){
        userRange = uR;
        poiRange = pR;
        currUser.getPreferences().setUserRange(uR);
        currUser.getPreferences().setPOIRange(pR);
        DocumentReference usrRef = db.collection("users").document(currId);
        usrRef.update("prefs", currUser.getPreferences());
        fab.setVisibility(View.VISIBLE);
        filterFrag.closeFragment();
        filterAdd = false;
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



    private void updateMapItems(){
        stopThread = false;
        Log.d(TAG, "updateUserPositions: starting thread");
        new Thread(new Runnable() {
            @Override
            public void run() {
                int poiCounter = 0;
//                int inviteCounter = 0;
                while(true){
                    if(stopThread) {
                        Log.d(TAG, "run: stopping thread");
                        return;
                    }
                    try {
                        Log.d(TAG, "run: sleeping 2");
                        Thread.sleep(2000);
                        Log.d(TAG, "run: updating users ... POICOUNTER = " + poiCounter);
                        showUsers();
                        if(poiCounter >2){
                            Log.d(TAG, "run: updating pois ... POICOUNTER = " + poiCounter);
                            poiCounter = 0;
                            showPOIs();
                        }
//                        if(inviteCounter>5){
//                            inviteCounter = 0;
////                            checkInvites();
//                        }
                        poiCounter++;
//                        inviteCounter++;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }



//    private void createNotificationChannel(String channelId, String channelName){
//        if(Build.VERSION.SDK_INT >= 26) {
//            NotificationChannel chan = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
//
//            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//            nm.createNotificationChannel(chan);
//        }
//    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void showUsers(){
        if(usersOverlay!=null){
            this.map.getOverlays().remove(usersOverlay);
        }
        final ArrayList<OverlayItem> users = new ArrayList<OverlayItem>();
        for(User user : userListInst.getOnlineUsers()){
            if(haversineDist(user.lat, user.lon) < userRange){
                Log.d(TAG,"ONLINE JE " + user.getUsername() + "COORDS: " + user.lat + "  :  " + user.lon);
                OverlayItem item = new OverlayItem(user.getUsername(), user.getEmail(), new GeoPoint(user.lat,user.lon));
                Bitmap userIcon;
                if(user.img != null){
                    userIcon = Bitmap.createScaledBitmap(user.img, 84, 84, true);
                }else{
                    Bitmap defIcon = BitmapFactory.decodeResource(getResources(), R.drawable.def_avatar);
                    userIcon = Bitmap.createScaledBitmap(defIcon, 84,84,true);
                }
                Bitmap userImg = getRoundedBitmap(userIcon, 42);
                Bitmap icon = mergeToPin(BitmapFactory.decodeResource(getResources(), R.drawable.mapmarker64), userImg, true);
                item.setMarker(new BitmapDrawable(getResources(), icon));
                users.add(item);
            }
        }

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

    private void showPOIs(){
        if(poiOverlay!=null){
            this.map.getOverlays().remove(poiOverlay);
        }
        final ArrayList<OverlayItem> pois = new ArrayList<OverlayItem>();
        for(POI poi : userListInst.getPOIs()){
            if(haversineDist(poi.getLat(), poi.getLon()) < poiRange){
                OverlayItem item = new OverlayItem(poi.getName(), poi.getDesc(), new GeoPoint(poi.getLat(), poi.getLon()));
                Bitmap scaledThumb = Bitmap.createScaledBitmap(poi.getThumb(), 128, 74, false);
                Bitmap icon = mergeToPin(BitmapFactory.decodeResource(getResources(), R.drawable.poimarker64), scaledThumb, false);
                item.setMarker(new BitmapDrawable(getResources(), icon));
                pois.add(item);
            }
        }

        poiOverlay = new ItemizedIconOverlay<OverlayItem>(pois,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(int index, OverlayItem item) {
                        Intent poiIntent = new Intent(MainScreenActivity.this, PlacesActivity.class);
                        poiIntent.putExtra("poiID", pois.get(index).getTitle());
                        startActivity(poiIntent);
                        return true;
                    }
                    @Override
                    public boolean onItemLongPress(int index, OverlayItem item) {
                        return false;
                    }
                }, getApplicationContext());
        this.map.getOverlays().add(poiOverlay);
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

    public static Bitmap mergeToPin(Bitmap back, Bitmap front, Boolean round) {
//        front = Bitmap.createScaledBitmap(front, 128, 74, true);
        Bitmap result = Bitmap.createBitmap(back.getWidth(), back.getHeight(), back.getConfig());
        Log.d("AAAAAAAAAAAAAAA", String.valueOf(back.getConfig()));
        Canvas canvas = new Canvas(result);
        int widthBack = back.getWidth();
        int widthFront = front.getWidth();
        float move = (widthBack - widthFront) / 2;
        float moveVert = round ? move/2 : move;
        canvas.drawBitmap(back, 0f, 0f, null);
        canvas.drawBitmap(front, move, moveVert, null);
        return result;
    }

    private Double haversineDist(Double lat, Double lon){
        if(lat != null && lon != null && currentLat != null && currentLon != null){
            double R = 6371;
            double dLat = lat - currentLat;
            double dLon = lon - currentLon;
            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(currentLat) * Math.cos(lat) * Math.sin(dLon/2) * Math.sin(dLon/2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
            Double d = R * c;
            return d;
        }
        return 999999.0;
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
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }


    @Override
    protected void onResume() {
        super.onResume();
        map.onResume();
        updateMapItems();


        if(currUser.getPreferences()!= null && currUser.getPreferences().getBackgroundService()){
            if(!userListInst.isMyServiceRunning(this, BackgroundService.class)){
                Intent i = new Intent(MainScreenActivity.this, BackgroundService.class);
                startService(i);
            }
        }


        try{
            Intent intent = getIntent();
            Bundle userBundle = intent.getExtras();
            poiLat = userBundle.getDouble("lat");
            poiLon = userBundle.getDouble("lon");
            Log.w(TAG, "LAT IS " + poiLat);
        }catch (Exception e){
//            Log.w(TAG, e.getMessage());
        }

        if(poiLat != null) {
            GeoPoint startPoint;
            mapController.setZoom(20.0);
            startPoint = new GeoPoint(poiLat, poiLon);
            mapController.setCenter(startPoint);
            Toast.makeText(this, "CENTERING", Toast.LENGTH_SHORT).show();
        }
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


    protected Bitmap getRoundedBitmap(Bitmap srcBitmap, int cornerRadius) {
        // Initialize a new instance of Bitmap
        Bitmap dstBitmap = Bitmap.createBitmap(
                srcBitmap.getWidth(), // Width
                srcBitmap.getHeight(), // Height

                Bitmap.Config.ARGB_8888 // Config
        );

        /*
            Canvas
                The Canvas class holds the "draw" calls. To draw something, you need 4 basic
                components: A Bitmap to hold the pixels, a Canvas to host the draw calls (writing
                into the bitmap), a drawing primitive (e.g. Rect, Path, text, Bitmap), and a paint
                (to describe the colors and styles for the drawing).
        */
        // Initialize a new Canvas to draw rounded bitmap
        Canvas canvas = new Canvas(dstBitmap);

        // Initialize a new Paint instance
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        /*
            Rect
                Rect holds four integer coordinates for a rectangle. The rectangle is represented by
                the coordinates of its 4 edges (left, top, right bottom). These fields can be accessed
                directly. Use width() and height() to retrieve the rectangle's width and height.
                Note: most methods do not check to see that the coordinates are sorted correctly
                (i.e. left <= right and top <= bottom).
        */
        /*
            Rect(int left, int top, int right, int bottom)
                Create a new rectangle with the specified coordinates.
        */
        // Initialize a new Rect instance
        Rect rect = new Rect(0, 0, srcBitmap.getWidth(), srcBitmap.getHeight());

        /*
            RectF
                RectF holds four float coordinates for a rectangle. The rectangle is represented by
                the coordinates of its 4 edges (left, top, right bottom). These fields can be
                accessed directly. Use width() and height() to retrieve the rectangle's width and
                height. Note: most methods do not check to see that the coordinates are sorted
                correctly (i.e. left <= right and top <= bottom).
        */
        // Initialize a new RectF instance
        RectF rectF = new RectF(rect);

        /*
            public void drawRoundRect (RectF rect, float rx, float ry, Paint paint)
                Draw the specified round-rect using the specified paint. The roundrect will be
                filled or framed based on the Style in the paint.

            Parameters
                rect : The rectangular bounds of the roundRect to be drawn
                rx : The x-radius of the oval used to round the corners
                ry : The y-radius of the oval used to round the corners
                paint : The paint used to draw the roundRect
        */
        // Draw a rounded rectangle object on canvas
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint);

        /*
            public Xfermode setXfermode (Xfermode xfermode)
                Set or clear the xfermode object.
                Pass null to clear any previous xfermode. As a convenience, the parameter passed
                is also returned.

            Parameters
                xfermode : May be null. The xfermode to be installed in the paint
            Returns
                xfermode
        */
        /*
            public PorterDuffXfermode (PorterDuff.Mode mode)
                Create an xfermode that uses the specified porter-duff mode.

            Parameters
                mode : The porter-duff mode that is applied

        */
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        /*
            public void drawBitmap (Bitmap bitmap, float left, float top, Paint paint)
                Draw the specified bitmap, with its top/left corner at (x,y), using the specified
                paint, transformed by the current matrix.

                Note: if the paint contains a maskfilter that generates a mask which extends beyond
                the bitmap's original width/height (e.g. BlurMaskFilter), then the bitmap will be
                drawn as if it were in a Shader with CLAMP mode. Thus the color outside of the
                original width/height will be the edge color replicated.

                If the bitmap and canvas have different densities, this function will take care of
                automatically scaling the bitmap to draw at the same density as the canvas.

            Parameters
                bitmap : The bitmap to be drawn
                left : The position of the left side of the bitmap being drawn
                top : The position of the top side of the bitmap being drawn
                paint : The paint used to draw the bitmap (may be null)
        */
        // Make a rounded image by copying at the exact center position of source image
        canvas.drawBitmap(srcBitmap, 0, 0, paint);

        // Free the native object associated with this bitmap.
        srcBitmap.recycle();

        // Return the circular bitmap
        return dstBitmap;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}


