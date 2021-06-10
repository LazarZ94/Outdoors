/*

Klasa za prikaz korisnickog profila

 */


package com.example.outdoors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class UserProfileActivity extends BaseDrawerActivity {

    final static String TAG = "USERPROFILE";

    User currUser;
    String userID;
    User user;
    ArrayList<User> userFriends;
    FirebaseFirestore db;
    final String currId = DBAuth.getInstance().getAuth().getCurrentUser().getUid();

    public final int IMAGE_PICKER = 1;

    ArrayList<User> onlineUsers;

    ArrayList<User> offlineUsers;

    final UserList userListInst = UserList.getInstance();

    ImageView avatarView;

    FirebaseStorage storage = DBAuth.getInstance().getStorage();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_user_profile);

        FrameLayout contentLayout = (FrameLayout) findViewById(R.id.contentFrame);
        getLayoutInflater().inflate(R.layout.activity_user_profile, contentLayout);
        ;

        try{
            Intent intent = getIntent();
            Bundle userBundle = intent.getExtras();
            userID = userBundle.getString("userID");
        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }

        db = DBAuth.getInstance().getDB();


        user = userListInst.getUser(userID);

        setTitle(user.getUsername());

        userFriends = new ArrayList<>();

        for(String friendID : user.friends){
            userFriends.add(userListInst.getUser(friendID));
        }

        currUser = UserList.getInstance().getCurrentUser();

        avatarView = (ImageView) findViewById(R.id.userProfileAvatar);
        if(user.getAvatar()!=null){
            avatarView.setImageBitmap(user.getAvatar());
        }

        avatarView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Toast.makeText(UserProfileActivity.this, "Choose image", Toast.LENGTH_SHORT).show();
                Intent i = new Intent();
                i.setType("image/*");
                i.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(i, "Select Image"), IMAGE_PICKER);
            }
        });


        final Button addButt = (Button) findViewById(R.id.userProfileAddFriend);

        if(user.friends.contains(currId) || user.equals(currUser)){
            addButt.setVisibility(View.GONE);
        }

        addButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!user.friendRequests.contains(currId) && !currUser.friendRequests.contains(userID)){
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
                    userListInst.updateUsers(null);
                }
            }
        });


        onlineUsers = new ArrayList<>();

        offlineUsers = new ArrayList<>();

        for(User usr : userListInst.getUserList()){
            if(usr.getStatus()){
                onlineUsers.add(usr);
            }else{
                offlineUsers.add(usr);
            }
        }

        Log.d(TAG, "ONLINE USERS: " + onlineUsers);

        Log.d(TAG, "OFFLINE USERS: " + offlineUsers);

        Toast.makeText(this, "Online users: " + onlineUsers.size(), Toast.LENGTH_SHORT).show();

        Button fbButt = (Button) findViewById(R.id.fireBaseButton);
        fbButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onlineUsers = new ArrayList<>();

                offlineUsers = new ArrayList<>();
                for(User usr : userListInst.getUserList()){
                    if(usr.getStatus()){
                        onlineUsers.add(usr);
                    }else{
                        offlineUsers.add(usr);
                    }
                }
                Toast.makeText(UserProfileActivity.this, "Online users: " + onlineUsers.size(), Toast.LENGTH_SHORT).show();
            }
        });


        ListView friendList = (ListView) findViewById(R.id.userProfileFriendList);

        friendList.setAdapter(new ArrayAdapter<User>(this, android.R.layout.simple_list_item_1, userFriends));

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == IMAGE_PICKER && resultCode == RESULT_OK){
            if(data != null && data.getData() != null){
                try {
                    final Bitmap imgBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                    StorageReference avatarRef = storage.getReference().child("images/avatars/" + userListInst.getCurrentUserID());
                    byte[] newAvatar = bArrFromBM(imgBitmap);
                    UploadTask upTask = avatarRef.putBytes(newAvatar);
                    upTask.addOnFailureListener(new OnFailureListener(){
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(UserProfileActivity.this, "Problem uploading image!", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener(){
                        @Override
                        public void onSuccess(Object o) {
                            Toast.makeText(UserProfileActivity.this, "Image uploaded successfully!", Toast.LENGTH_SHORT).show();
                            avatarView.setImageBitmap(imgBitmap);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private byte[] bArrFromBM(Bitmap bm){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Bitmap scaled = Bitmap.createScaledBitmap(bm, 90, 90, true);
        scaled.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }else {
            super.onBackPressed();
        }
    }

    private void setRequests(){
        ArrayList<String> currFrSent = new ArrayList<>(currUser.sentFriendRequests);
        currFrSent.add(userID);
        ArrayList<String> otherFReq = new ArrayList<>(user.friendRequests);
        otherFReq.add(currId);
        db.collection("users").document(currId).update("sentFriendRequests", currFrSent);
        db.collection("users").document(userID).update("friendRequests", otherFReq);
                /*.addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                    }
                });*/
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
    }
}