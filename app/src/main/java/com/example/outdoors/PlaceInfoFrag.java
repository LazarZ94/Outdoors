/*

Fragment za detaljniji prikaz odredjenog POI-a

 */


package com.example.outdoors;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class PlaceInfoFrag extends Fragment {
    View view;
    POI poi;
    User currUser = UserList.getInstance().getCurrentUser();
    Bitmap scaledBitmap;
    FirebaseStorage fbs = DBAuth.getInstance().getStorage();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.place_info_frag, container,false);
        ImageView imgView = view.findViewById(R.id.imagePOIInfo);
        TextView descTW = view.findViewById(R.id.descTWPOIInfo);
        TextView usernameTW = view.findViewById(R.id.usernamePOIInfo);

        poi= ((PlacesActivity)getActivity()).getCurrentPOI();

        setPic(view, imgView);

        descTW.setText(poi.getDesc());
        usernameTW.setText(currUser.getUsername());

        Button showPOI = (Button) view.findViewById(R.id.showMapPOIInfo);

        /*showPOI.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                ((PlacesActivity)getActivity()).showCurrentPOI();
            }
        });*/

        showPOI.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                StorageReference stRef = fbs.getReference().child("images/POI/scaled/test2.jpg");
                Bitmap bitmap = scaledBitmap;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();
                Toast.makeText(getActivity(), "Uploading", Toast.LENGTH_SHORT).show();
                UploadTask uploadTask = stRef.putBytes(data);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                        // ...
                    }
                });
            }
        });

        return view;
    }

    private void setPic(View view, ImageView imgView) {
        String img = ((PlacesActivity)getActivity()).storageDir.getAbsolutePath();
        img += "/" + poi.getImg();
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

        Bitmap bitmap = BitmapFactory.decodeFile(img, bmOptions);
        scaledBitmap = Bitmap.createScaledBitmap(bitmap, 120, 120, false);
        imgView.setImageBitmap(bitmap);
    }
}
