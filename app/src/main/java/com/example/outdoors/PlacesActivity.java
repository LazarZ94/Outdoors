/*

Activity za prikaz svih POI, deli se na PlaceInfoFrag i PlacesListFrag fragmente

 */


package com.example.outdoors;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
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

    private POI currentPOI = new POI("Nothing here", "0", "0", "");

    public File storageDir;

    User currUser = UserList.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_places);

        FrameLayout contentLayout = (FrameLayout) findViewById(R.id.contentFrame);
        getLayoutInflater().inflate(R.layout.activity_places, contentLayout);

        storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

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

    public void setCurrentPOI(StorageReference poi){
        currentPOIName = poi.getName();
        StorageMetadata metadata = currUser.POIMetadata.get(currentPOIName);
        currentPOI = new POI (metadata.getCustomMetadata("desc"), metadata.getCustomMetadata("lat"), metadata.getCustomMetadata("lon"), currentPOIName);
        if(adapter.getCount()<2){
            adapter.addFragment(new PlaceInfoFrag(), "InfoFragment");
            adapter.notifyDataSetChanged();
        }
        setViewPager(1);
    }

    public void showCurrentPOI(){
        Intent intent = new Intent(this, MainScreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    public POI getCurrentPOI(){
        return currentPOI;
    }

    public SectionsStatePagesAdapter getAdapter(){
        return adapter;
    }

    private void setUpViewPager(ViewPager viewPager){
        adapter = new SectionsStatePagesAdapter(getSupportFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        adapter.addFragment(new PlacesListFrag(), "ListFragment");
        //adapter.addFragment(new PlaceInfoFrag(), "InfoFragment");
        viewPager.setAdapter(adapter);
    }

    public void setViewPager(int fragNum){
        mViewPager.setCurrentItem(fragNum);
    }
}