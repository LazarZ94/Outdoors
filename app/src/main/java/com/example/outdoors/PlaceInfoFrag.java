/*

Fragment za detaljniji prikaz odredjenog POI-a

 */


package com.example.outdoors;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class PlaceInfoFrag extends Fragment {
    View view;
    POI poi;
    String poiID;
    User currUser = UserList.getInstance().getCurrentUser();
    String currUserID = UserList.getInstance().getCurrentUserID();
    User user;
    String poiUserID;
    Bitmap scaledBitmap;
    FirebaseStorage fbs = DBAuth.getInstance().getStorage();
    FirebaseFirestore db = DBAuth.getInstance().getDB();

    String TAG = "POIINFO";

    ImageView imgView;
    ProgressBar pBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.place_info_frag, container,false);
        imgView = view.findViewById(R.id.imagePOIInfo);
        TextView descTW = view.findViewById(R.id.descTWPOIInfo);
        TextView usernameTW = view.findViewById(R.id.usernamePOIInfo);
        pBar = view.findViewById(R.id.poiFragProgressBar);

        poi= ((PlacesActivity)getActivity()).getCurrentPOI();

        poiID = ((PlacesActivity)getActivity()).getCurrPOIID();

        poiUserID = ((PlacesActivity)getActivity()).getPOIUserID();

        user = UserList.getInstance().getUser(poi.getuID());

        getPOIPic(poiUserID, poiID);

        pBar.setVisibility(View.VISIBLE);

        descTW.setText(poi.getDesc());
        usernameTW.setText(user.getUsername());

        Button showPOI = (Button) view.findViewById(R.id.showMapPOIInfo);

        showPOI.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                ((PlacesActivity)getActivity()).showCurrentPOIOnMap();
            }
        });

        final TextView likes = view.findViewById(R.id.pointsPOIInfo);
        likes.setText(String.valueOf(poi.getLikes().size()));
        
        final Button likePOI = (Button) view.findViewById(R.id.likePOIInfo);

        if(user == currUser){
            likePOI.setEnabled(false);
        }

        if(poi.getLikes().contains(currUserID)){
            likePOI.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        }

        likePOI.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(!poi.getLikes().contains(currUserID)){
                    poi.likes.add(currUserID);
                    likes.setText(String.valueOf(poi.getLikes().size()));
                    likePOI.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                }else{
                    poi.likes.remove(currUserID);
                    likes.setText(String.valueOf(poi.getLikes().size()));
                    likePOI.setBackgroundColor(Color.GRAY);
                }
                DocumentReference poiRef = db.collection("POI").document(poi.getName());
                poiRef.update("likes", poi.getLikes())
                        .addOnSuccessListener(new OnSuccessListener<Void>(){
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("AAAAAAAAAAAAAAAA", "DOC UPDATED");
                            }
                        }).addOnFailureListener(new OnFailureListener(){
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("SASDFDSAWFSADFD", "FAILED");
                            }
                });
            }
        });



        return view;
    }

    public void getPOIPic(String poiUser, final String poiID){
        StorageReference picRef = fbs.getReference().child("images/POI/full/"+poiUser+"/"+poiID);
        final long maxSize = 7 * 1024 * 1024;


        Toast.makeText(getContext(), "Downloading image", Toast.LENGTH_SHORT).show();

        picRef.getBytes(maxSize).addOnSuccessListener(new OnSuccessListener<byte[]>(){
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap currPOIImg = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                setPic(currPOIImg, imgView);
                pBar.setVisibility(View.GONE);
//                setCurrentPOI(userListInst.getPOI(poiID), bytes);
            }
        }).addOnFailureListener(new OnFailureListener(){
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Problem getting file(s).", Toast.LENGTH_SHORT).show();
                pBar.setVisibility(View.GONE);
            }
        });

    }

    private void setPic(Bitmap img, ImageView imgView) {

        scaledBitmap = Bitmap.createScaledBitmap(img, 120, 120, false);
        imgView.setImageBitmap(img);

    }
}
