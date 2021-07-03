package com.example.outdoors;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class FilterFrag extends Fragment {

    public int USER = 0;
    public int POI = 1;

    int userRange, poiRange;
    int selected = USER;

    Context mCont;
    MainScreenActivity act;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mCont = getContext();
        View view = inflater.inflate(R.layout.filter_frag, container, false);

        if(getArguments() != null){
            userRange = getArguments().getInt("userRange");
            poiRange = getArguments().getInt("poiRange");
        }


        ImageButton userButt = (ImageButton) view.findViewById(R.id.filterUserRangeButton);
        ImageButton poiButt = (ImageButton) view.findViewById(R.id.filterPOIRangeButton);

        final SeekBar rangeBar = (SeekBar) view.findViewById(R.id.filterRangeBar);

        final TextView switchTW = (TextView) view.findViewById(R.id.filterSwitchTW);
        switchTW.setText("User Range");
        final TextView rangeTW = (TextView) view.findViewById(R.id.filterRangeDisplay);
        rangeTW.setText("Range: " + String.valueOf(userRange) + "km");
        rangeBar.setProgress(userRange);

        Button confirmButt = (Button) view.findViewById(R.id.filterConfirmButton);


        rangeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            int range = 1;
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                range = i;
                rangeTW.setText("Range: " + String.valueOf(range) + "km");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(selected == USER){
                    userRange = range;
                }else{
                    poiRange = range;
                }
            }
        });

        poiButt.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                selected = POI;
                switchTW.setText("POI Range");
                rangeTW.setText("Range: " + String.valueOf(poiRange) + "km");
                rangeBar.setProgress(poiRange);
            }
        });

        userButt.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                selected = USER;
                switchTW.setText("USER Range");
                rangeTW.setText("Range: " + String.valueOf(userRange) + "km");
                rangeBar.setProgress(userRange);
            }
        });

        confirmButt.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                ((MainScreenActivity) getActivity()).setRanges(userRange, poiRange);
            }
        });

        return view;
    }


    public void closeFragment(){
        getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
    }
}
