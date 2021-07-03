package com.example.outdoors;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class HikeInvFrag extends Fragment {

    View view;

    UserList userListInst = UserList.getInstance();

    User currUser = userListInst.getCurrentUser();

    FirebaseFirestore fbfs = DBAuth.getInstance().getDB();

    RecyclerView recView;

    BiMap<String, Plan> planInvites = HashBiMap.create();
    ArrayList<String> inviteIDs = new ArrayList();

    TextView emptyTV;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.hike_inv_frag, container,false);

        recView = view.findViewById(R.id.hikeInvRecView);

        emptyTV = view.findViewById(R.id.hikeEmptyTV);



        fbfs.collection("plans").whereArrayContains("invites", userListInst.getCurrentUserID())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>(){
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot doc : task.getResult()){
                                planInvites.put(doc.getId(), doc.toObject(Plan.class));
                            }
                            userListInst.setPlanInvites(planInvites);
                            setupView();
                        }
                    }
                });



        return view;
    }

    private void setupView(){
        for(Invite inv : currUser.planInvites){
            inviteIDs.add(inv.inviteID);
        }

        if(!currUser.planInvites.isEmpty()) {
            emptyTV.setVisibility(View.GONE);
//            RecViewAdapter recAdapter = new RecViewAdapter(getActivity().getApplicationContext(), usernames, avatar, SENT_VIEW);
            RecViewAdapter recAdapter = new RecViewAdapter(getActivity().getApplicationContext(), inviteIDs, RecViewAdapter.HIKE_INV, null);
            recView.setAdapter(recAdapter);
            recView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
            recView.setVisibility(View.VISIBLE);
        }else{
            recView.setVisibility(View.GONE);
            emptyTV.setText("Nothing here.");
            emptyTV.setVisibility(View.VISIBLE);
        }
    }

}
