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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.storage.StorageReference;

public class PlacesListFrag extends Fragment {

    User currUser = UserList.getInstance().getCurrentUser();

    View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.places_list_frag, container,false);

        LinearLayout scrollLayout = (LinearLayout) view.findViewById(R.id.placesListLayout);



        for(final StorageReference poi : currUser.POIs){
            TextView tw = new TextView(getContext());
            String desc = currUser.POIMetadata.get(poi.getName()).getCustomMetadata("desc") + "AAAAAAA";
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

        return view;
    }

    private void showPOI(StorageReference poi){
        ((PlacesActivity)getActivity()).setCurrentPOI(poi);
    }
}
