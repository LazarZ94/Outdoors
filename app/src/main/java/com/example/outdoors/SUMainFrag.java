/*

Fragment za neophodne informacije pri registrovanju

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

public class SUMainFrag extends Fragment {

    View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.signup_frag1, container,false);

        Button nextButt = (Button) view.findViewById(R.id.nextbutton);

        nextButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((SignUpActivity)getActivity()).setViewPager(1);
            }
        });

        return view;
    }

    public String getInfo(int viewID){
        EditText elET = (EditText) view.findViewById(viewID);
        return elET.getText().toString().trim();
    }
}
