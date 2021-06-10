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
    User currUser = UserList.getInstance().getCurrentUser();
    String currUserID = UserList.getInstance().getCurrentUserID();
    User user;
    Bitmap scaledBitmap;
    FirebaseStorage fbs = DBAuth.getInstance().getStorage();
    FirebaseFirestore db = DBAuth.getInstance().getDB();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.place_info_frag, container,false);
        ImageView imgView = view.findViewById(R.id.imagePOIInfo);
        TextView descTW = view.findViewById(R.id.descTWPOIInfo);
        TextView usernameTW = view.findViewById(R.id.usernamePOIInfo);

        poi= ((PlacesActivity)getActivity()).getCurrentPOI();

        user = UserList.getInstance().getUser(poi.getuID());

        setPic(view, imgView);

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

    private void setPic(View view, ImageView imgView) {
        String img = ((PlacesActivity)getActivity()).storageDir.getAbsolutePath();
        img += "/" + poi.getName();
        //int targetW = view.getWidth();
        //int targetH = (int) (view.getHeight()*0.4);

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(img, bmOptions);

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        //int scaleFactor = Math.max(1, Math.min(photoW/targetW, photoH/targetH));

        bmOptions.inJustDecodeBounds = false;
        //bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        //Bitmap bitmap = BitmapFactory.decodeFile(img, bmOptions);
        Bitmap bitmap = ((PlacesActivity)getActivity()).getCurrPOIImg();
        scaledBitmap = Bitmap.createScaledBitmap(bitmap, 120, 120, false);
        imgView.setImageBitmap(bitmap);
    }
}
