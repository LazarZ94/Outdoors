/*

????????????????

 */


package com.example.outdoors;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

public class POIFragment extends Fragment {
    private final String TAG = "POIFragment";

    FirebaseStorage fbs = DBAuth.getInstance().getStorage();

    private String img;
    private String lat;
    private String lon;
    private String currId;
    private String desc;

    private int width, height;

    /*public static POIFragment newInstance(String img, String lat, String lon, String currID){
        POIFragment poiFragment = new POIFragment();

        Bundle args = new Bundle();
        args.putString("img", img);
        args.putString("lat", lat);
        args.putString("lon", lon);
        args.putString("currID", currID);
        poiFragment.setArguments(args);
        return poiFragment;
    }*/

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.poi_fragment, container, false);
        ImageView imgView = view.findViewById(R.id.imagePOI);
        final EditText descEdit = view.findViewById(R.id.descEditPOI);
        Button submit  = view.findViewById(R.id.submitPOI);

        if(getArguments() != null){
            img = getArguments().getString("img");
            lat = getArguments().getString("lat");
            lon = getArguments().getString("lon");
            currId = getArguments().getString("currID");
        }

        Log.d(TAG, "onCreateView: CONTEXT" + getContext());

        setPic(view, imgView);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                desc = descEdit.getText().toString();
                Uri file = Uri.fromFile(new File(img));
                final StorageReference imagePOIRef = fbs.getReference().child("images/POI/"+currId+"/"+file.getLastPathSegment());

                final StorageMetadata metadata = new StorageMetadata.Builder()
                        .setCustomMetadata("lat", lat)
                        .setCustomMetadata("lon", lon)
                        .setCustomMetadata("desc", desc)
                        .build();

                Toast.makeText(getContext(), "Photo uploading", Toast.LENGTH_SHORT).show();
                UploadTask uploadTask = imagePOIRef.putFile(file, metadata);

                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to upload photo", e);
                        closeFragment();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //Toast.makeText(getContext(), "Photo uploaded!", Toast.LENGTH_SHORT).show();
                        User currUser = UserList.getInstance().getCurrentUser();
                        currUser.POIs.add(imagePOIRef);
                        currUser.POIMetadata.put(imagePOIRef.getName(),metadata);
                        ((MainScreenActivity)getActivity()).uploadedPOI();
                        closeFragment();
                    }
                });
            }
        });

        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Toast.makeText(getContext(), "WIDTH"+ view.getWidth(), Toast.LENGTH_SHORT).show();
    }

    private void closeFragment(){
        getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
    }

    private void setPic(View view, ImageView imgView) {
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
        imgView.setImageBitmap(bitmap);
    }

}
