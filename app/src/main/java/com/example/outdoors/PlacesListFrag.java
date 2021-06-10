/*

Fragment za prikaz POI liste

 */


package com.example.outdoors;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class PlacesListFrag extends Fragment {

    User currUser = UserList.getInstance().getCurrentUser();

    View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.places_list_frag, container,false);

        LinearLayout scrollLayout = (LinearLayout) view.findViewById(R.id.placesListLayout);

        ArrayList<POI> poiList = ((PlacesActivity)getActivity()).getPOIList();

        for(final POI poi : poiList){
            Log.d("AAAAAAAAAAAAAAAAAAAAAA", String.valueOf(poi));
            TextView tw = new TextView(getContext());
//            String desc = currUser.POIMetadata.get(poi.getName()).getCustomMetadata("desc") + "AAAAAAA";
            String desc = poi.getDesc();
            tw.setText(desc);
            tw.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
            tw.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showPOI(poi);
                }
            });
            scrollLayout.addView(tw);
        }

        Log.d("AAAA", "StorageDIR : " + ((PlacesActivity)getActivity()).storageDir);

        return view;
    }

    private void showPOI(POI poi){
        Toast.makeText(getContext(), "Downloading image", Toast.LENGTH_SHORT).show();
        ((PlacesActivity)getActivity()).getPOIPic(poi.getuID(), poi.getName());
    }
}
