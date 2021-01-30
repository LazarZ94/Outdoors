/*

Fragment za sekundarne informacije pri reg

 */


package com.example.outdoors;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SUSecFrag extends Fragment {

    View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.signup_frag2, container,false);

        Button prevButt = (Button) view.findViewById(R.id.prevbutton);

        prevButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((SignUpActivity)getActivity()).setViewPager(0);
            }
        });

        Button submitButt = (Button) view.findViewById(R.id.signUpSubmit);

        submitButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((SignUpActivity)getActivity()).setInfo();
            }
        });

        return view;
    }

    public String getInfo(int viewID){
        EditText elET = (EditText) view.findViewById(viewID);
        return elET.getText().toString().trim();
    }
}
