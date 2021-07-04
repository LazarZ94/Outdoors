/*

Activity za prikaz liste prijatelja

 */


package com.example.outdoors;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class FriendListActivity extends BaseDrawerActivity {

    private UserList userListInst = UserList.getInstance();

    private User currentUser = userListInst.getCurrentUser();
    String currID = userListInst.getCurrentUserID();

    FirebaseFirestore db = DBAuth.getInstance().getDB();

    ArrayList<String> userInvites = new ArrayList();

    public static int FRIEND_INVITE = 1;

    int mode = 0;

    ArrayList<String> excluded;
    TextView emptyTW;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout contentLayout = (FrameLayout) findViewById(R.id.contentFrame);
        getLayoutInflater().inflate(R.layout.activity_friend_list, contentLayout);

        setTitle(currentUser.getUsername());

        emptyTW = (TextView) findViewById(R.id.friendListEmptyTW);

        try{
            Intent intent = getIntent();
            Bundle userBundle = intent.getExtras();
            mode = userBundle.getInt("mode");
            excluded = userBundle.getStringArrayList("exclude");
        }catch (Exception e){
            Log.w("FRIENDS ACT", "bundle empty");
        }

        db.collection("users").document(currID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>(){
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot doc = task.getResult();
                            if(doc.exists()){
                                User user = doc.toObject(User.class);
                                currUser.setFriends(user.friends);
                                setupView();
                            }
                        }
                    }
                });


    }

    private void setupView(){
        Button btButt = (Button) findViewById(R.id.btButton);

        LinearLayout toolbarLayout  = (LinearLayout) findViewById(R.id.toolbarItems);
        toolbarLayout.setGravity(Gravity.RIGHT);
        toolbarLayout.setPadding(0,15,30,15);

        if(mode == 1){
            btButt.setVisibility(View.GONE);
            ImageButton confirmButt = new ImageButton(this);
            confirmButt.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_check_24));
            confirmButt.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            confirmButt.setColorFilter(Color.WHITE);
            confirmButt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent retInt = new Intent();
                    retInt.putExtra("result", userInvites);
                    setResult(Activity.RESULT_OK, retInt);
                    finish();
                }
            });
            toolbarLayout.addView(confirmButt);
        }

        btButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(FriendListActivity.this, BluetoothActivity.class);
                startActivity(i);
            }
        });

        ListView users = (ListView) findViewById(R.id.friendListView);

        final ArrayList<User> friends = new ArrayList<>();

        for(String uid : currentUser.getFriends()){
            if(mode==1){
                if(!excluded.contains(uid)){
                    friends.add(userListInst.getUser(uid));
                }
            }else{
                friends.add(userListInst.getUser(uid));
            }
        }

        final ArrayAdapter userAdapter = new ArrayAdapter<User>(this, android.R.layout.simple_list_item_1, friends);

        if(friends.isEmpty()){
            users.setVisibility(View.GONE);
            emptyTW.setText("Nothing Here.");
            emptyTW.setVisibility(View.VISIBLE);
        }else{
            emptyTW.setVisibility(View.GONE);
            users.setAdapter(userAdapter);
            users.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    String uid = userListInst.getUserId((User) adapterView.getAdapter().getItem(i));
                    if(mode == 1){
                        userInvites.add(uid);
                        friends.remove(i);
                        userAdapter.notifyDataSetChanged();
                    }else{
                        Bundle userBundle = new Bundle();
                        userBundle.putString("userID", uid);
                        Intent intent = new Intent(FriendListActivity.this, UserProfileActivity.class);
                        intent.putExtras(userBundle);
                        startActivity(intent);
                    }
                }
            });
            users.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }else {
            super.onBackPressed();
        }
    }
}