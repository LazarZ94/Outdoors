/*

Activity za prikaz svih POI, deli se na PlaceInfoFrag i PlacesListFrag fragmente

 */


package com.example.outdoors;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlacesActivity extends BaseDrawerActivity {

    final String TAG = "PLACES ACTIVITY";

    //private SectionsStatePagesAdapter mSectionsStatePagerAdapter;
    private ViewPager mViewPager;

    private SectionsStatePagesAdapter adapter;

    private String currentPOIName = "";

    private POI currentPOI = new POI("Nothing here", "0", "0", "", "", new ArrayList<String>());

    public File storageDir;

    private String poiID = null;

    private FirebaseStorage fbs = DBAuth.getInstance().getStorage();

    UserList userListInst = UserList.getInstance();

    User currUser = userListInst.getCurrentUser();

    String poiUserID = null;

    ArrayList<POI> poiList = new ArrayList<>();

    Bitmap currPOIImg = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_places);

        FrameLayout contentLayout = (FrameLayout) findViewById(R.id.contentFrame);
        getLayoutInflater().inflate(R.layout.activity_places, contentLayout);

        storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        try{
            Intent intent = getIntent();
            Bundle userBundle = intent.getExtras();
            poiID = userBundle.getString("poiID");
            poiUserID = userBundle.getString("userID");
        }catch (Exception e){
            //Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            //finish();
        }

        if(poiID != null){
            POI currPOI = userListInst.getPOI(poiID);
            poiUserID = currPOI.getuID();
            getPOIPic(poiUserID, poiID);
        }

        if(poiUserID == null){
            poiUserID = userListInst.getCurrentUserID();
        }

        getUserPOIs(poiUserID);



        //Log.d(TAG, poiID);

        //mSectionsStatePagerAdapter = new SectionsStatePagesAdapter(getSupportFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

        //Toast.makeText(this, "POIS length" + currUser.POIs.size(), Toast.LENGTH_SHORT).show();

        //Log.d(TAG, "onCreate: POIS" + currUser.POIs);

        mViewPager = (ViewPager) findViewById(R.id.containerPlaces);
        setUpViewPager(mViewPager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getPOIs();
    }

    private void getUserPOIs(String userID){
        for(POI poi : userListInst.getPOIs()){
            if(poi.getuID().equals(userID)){
                poiList.add(poi);
            }
        }
    }

    public void getPOIPic(String poiUser, final String poiID){
        StorageReference picRef = fbs.getReference().child("images/POI/full/"+poiUser+"/"+poiID);
        final long maxSize = 7 * 1024 * 1024;

        picRef.getBytes(maxSize).addOnSuccessListener(new OnSuccessListener<byte[]>(){
            @Override
            public void onSuccess(byte[] bytes) {
                setCurrentPOI(userListInst.getPOI(poiID), bytes);
            }
        }).addOnFailureListener(new OnFailureListener(){
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(PlacesActivity.this, "Problem getting file(s).", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getPOIs(){
        ArrayList<StorageReference> missingPOIs = new ArrayList<>();
        for(StorageReference poi : currUser.POIs){
            Log.d(TAG, "getPOIs: stDIR" + storageDir);
            String poiPath = storageDir + "/" + poi.getName();
            Log.d(TAG, "getPOIs: POI PATH" + poiPath);
            File poiLocal = new File(poiPath);
            if(!poiLocal.exists()){
                missingPOIs.add(poi);
            }
        }
        downloadMissingPOIs(missingPOIs, storageDir);
    }

    private void downloadMissingPOIs(ArrayList<StorageReference> pois, final File storageDir){
        for(StorageReference poi : pois){
            final String fileName = poi.getName();
            //fileName = fileName.substring(0, fileName.length()-4);
            Log.d(TAG, "downloadMissingPOIs: REMOVEEEEEEED" + fileName);
            try {
                final File localFile = File.createTempFile(fileName, "", storageDir);
                poi.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(PlacesActivity.this, "File downloaded", Toast.LENGTH_SHORT).show();
                        File newName = new File(storageDir, fileName);
                        localFile.renameTo(newName);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(PlacesActivity.this, "Problem downloading file", Toast.LENGTH_SHORT).show();
                    }
                });
            }catch (Exception e){
                Log.d(TAG, "downloadMissingPOIs: ERROR DOWNLOADING FILE", e);
            }

        }
    }


    public void setCurrentPOI(POI cPOI, byte[] img){
        currentPOI = cPOI;
        currPOIImg = BitmapFactory.decodeByteArray(img, 0, img.length);
        if(adapter.getCount()<2){
            adapter.addFragment(new PlaceInfoFrag(), "InfoFragment");
            adapter.notifyDataSetChanged();
        }
        setViewPager(1);
    }

//    public void setCurrentPOI(StorageReference poi){
//        currentPOIName = poi.getName();
//        StorageMetadata metadata = currUser.POIMetadata.get(currentPOIName);
//        currentPOI = new POI (metadata.getCustomMetadata("desc"), metadata.getCustomMetadata("lat"), metadata.getCustomMetadata("lon"), currentPOIName, "");
//        if(adapter.getCount()<2){
//            adapter.addFragment(new PlaceInfoFrag(), "InfoFragment");
//            adapter.notifyDataSetChanged();
//        }
//        setViewPager(1);
//    }

    public void showCurrentPOIOnMap(){
        Intent intent = new Intent(this, MainScreenActivity.class);
        intent.putExtra("lat", currentPOI.getLat());
        intent.putExtra("lon", currentPOI.getLon());
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    public POI getCurrentPOI(){
        return currentPOI;
    }
    public Bitmap getCurrPOIImg() {return currPOIImg;}

    public ArrayList<POI> getPOIList(){
        return poiList;
    }

    public SectionsStatePagesAdapter getAdapter(){
        return adapter;
    }

    private void setUpViewPager(ViewPager viewPager){
        adapter = new SectionsStatePagesAdapter(getSupportFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
//        if(poiID != null){
//            adapter.addFragment(new PlaceInfoFrag(), "InfoFragment");
//        }else{
//            adapter.addFragment(new PlacesListFrag(), "ListFragment");
//        }
        adapter.addFragment(new PlacesListFrag(), "ListFragment");
        viewPager.setAdapter(adapter);
    }

    public void setViewPager(int fragNum){
        mViewPager.setCurrentItem(fragNum);
    }
}