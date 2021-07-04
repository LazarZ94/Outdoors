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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class SentInvFrag extends Fragment {

    private static final int SENT_VIEW = 2;

    RecyclerView recView;
    TextView msg;

    UserList userListInst = UserList.getInstance();
    User currUser = userListInst.getCurrentUser();
    String currID = userListInst.getCurrentUserID();

    FirebaseFirestore db = DBAuth.getInstance().getDB();

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

        db.collection("users").document(currID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>(){
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot doc = task.getResult();
                            if(doc.exists()){
                                User user = doc.toObject(User.class);
                                currUser.setSentRequests(user.sentFriendRequests);
                                setupView();
                            }
                        }
                    }
                });



        return view;
    }

    private void setupView(){
        if(!currUser.sentFriendRequests.isEmpty()) {
            msg.setVisibility(View.GONE);
//            RecViewAdapter recAdapter = new RecViewAdapter(getActivity().getApplicationContext(), usernames, avatar, SENT_VIEW);
            RecViewAdapter recAdapter = new RecViewAdapter(getActivity().getApplicationContext(), sentInvs, SENT_VIEW, null);
            recView.setAdapter(recAdapter);
            recView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
            recView.setVisibility(View.VISIBLE);
        }else{
            recView.setVisibility(View.GONE);
            msg.setText("Nothing here.");
            msg.setVisibility(View.VISIBLE);
        }
    }
}