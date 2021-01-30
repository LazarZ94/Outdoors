package com.example.outdoors;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SentInvFrag extends Fragment {

    private static final int SENT_VIEW = 2;

    RecyclerView recView;
    TextView msg;

    UserList userListInst = UserList.getInstance();
    User currUser = userListInst.getCurrentUser();

    ArrayList<String> sentInvs = currUser.getSentFriendRequests();

    ArrayList<String> usernames = new ArrayList<>();
    int avatar = R.drawable.ic_launcher_foreground;

    View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.sent_inv_frag, container,false);

        recView = view.findViewById(R.id.sentInvView);

        msg = view.findViewById(R.id.sentInvTVEmpty);

        for(String id : sentInvs){
            usernames.add(userListInst.getUser(id).getUsername());
        }

        if(!currUser.sentFriendRequests.isEmpty()) {
            msg.setVisibility(View.GONE);
            RecViewAdapter recAdapter = new RecViewAdapter(getActivity().getApplicationContext(), usernames, avatar, SENT_VIEW);
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