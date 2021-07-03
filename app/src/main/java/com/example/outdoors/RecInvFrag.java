package com.example.outdoors;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecInvFrag extends Fragment {

    private static final int REC_VIEW = 1;

    UserList userListInst = UserList.getInstance();
    User currUser = userListInst.getCurrentUser();

    RecyclerView recView;
    TextView msg;

    ArrayList<String> recInvs = currUser.getFriendRequests();

    ArrayList<String> usernames = new ArrayList<>();
    int avatar = R.drawable.ic_launcher_foreground;

    View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.rec_inv_frag, container,false);

        recView = view.findViewById(R.id.recInvView);
        
        msg = view.findViewById(R.id.recInvTVEmpty);


        if(!currUser.friendRequests.isEmpty()) {
            msg.setVisibility(View.GONE);
//            RecViewAdapter recAdapter = new RecViewAdapter(getActivity().getApplicationContext(), usernames, avatar, REC_VIEW);
            RecViewAdapter recAdapter = new RecViewAdapter(getActivity().getApplicationContext(), recInvs, REC_VIEW, null);
            recView.setAdapter(recAdapter);
            recView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
            recView.setVisibility(View.VISIBLE);
        }else{
            recView.setVisibility(View.GONE);
            msg.setText("Nothing here.");
            msg.setVisibility(View.VISIBLE);
        }

        return view;
    }
}
