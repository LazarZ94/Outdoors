package com.example.outdoors;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UserProfile extends AppCompatActivity {

    final static String TAG = "USERPROFILE";

    String userID;
    User user;
    ArrayList<User> userFriends;
    FirebaseFirestore db;
    final String currId = DBAuth.getInstance().getAuth().getCurrentUser().getUid();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        try{
            Intent intent = getIntent();
            Bundle userBundle = intent.getExtras();
            userID = userBundle.getString("userID");
        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }

        db = DBAuth.getInstance().getDB();

        final UserList userListInst = UserList.getInstance();

        user = userListInst.getUser(userID);

        userFriends = new ArrayList<>();

        for(String friendID : user.friends){
            userFriends.add(userListInst.getUser(friendID));
        }

        final User currUser = UserAuthentication.getInstance().getCurrentUser();


        final Button addButt = (Button) findViewById(R.id.userProfileAddFriend);

        if(user.friends.contains(currId)){
            addButt.setVisibility(View.GONE);
        }

        addButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!user.friendRequests.contains(currId)){
                    Log.d(TAG, "USER ID JE " + userID);
                    Log.d(TAG, "CURRENT ID JE " + currId);
                    /*Map<String, Object> currAdd = new HashMap<>();
                    currAdd.put("id", currId);
                    Map<String, Object> otherAdd = new HashMap<>();
                    otherAdd.put("id", userID);
                    db.collection("users")
                            .document(userID).collection("friendRequests").add(currAdd)
                            .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                    if(task.isSuccessful()){
                                        Log.d(TAG, "REQUEST ADDED");
                                    }else{
                                        Log.w(TAG, "ADD REQUEST ERROR ", task.getException());
                                    }
                                }
                            });
                    db.collection("users")
                            .document(currId).collection("sentFriendRequests").add(otherAdd)
                            .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                    if(task.isSuccessful()){
                                        Log.d(TAG, "REQUEST ADDED");
                                    }else{
                                        Log.w(TAG, "ADD REQUEST ERROR ", task.getException());
                                    }
                                }
                            });*/
                    setRequests();
                    addButt.setVisibility(View.GONE);
                    userListInst.updateUsers();
                }
            }
        });


        ListView friendList = (ListView) findViewById(R.id.userProfileFriendList);

        friendList.setAdapter(new ArrayAdapter<User>(this, android.R.layout.simple_list_item_1, userFriends));

    }

    private void setRequests(){
        Map<String, Object> currAdd = new HashMap<>();
        currAdd.put("id", currId);
        Map<String, Object> otherAdd = new HashMap<>();
        otherAdd.put("id", userID);
        db.collection("users")
                .document(userID).collection("friendRequests").add(currAdd)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "REQUEST ADDED");
                        }else{
                            Log.w(TAG, "ADD REQUEST ERROR ", task.getException());
                        }
                    }
                });
        db.collection("users")
                .document(currId).collection("sentFriendRequests").add(otherAdd)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "REQUEST ADDED");
                        }else{
                            Log.w(TAG, "ADD REQUEST ERROR ", task.getException());
                        }
                    }
                });
    }
}