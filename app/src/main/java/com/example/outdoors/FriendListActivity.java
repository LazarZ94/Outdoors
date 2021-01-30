/*

Activity za prikaz liste prijatelja

 */


package com.example.outdoors;

import androidx.core.view.GravityCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;

import java.util.ArrayList;

public class FriendListActivity extends BaseDrawerActivity {

    private UserList userListInst = UserList.getInstance();

    private User currentUser = userListInst.getCurrentUser();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_friend_list);

        FrameLayout contentLayout = (FrameLayout) findViewById(R.id.contentFrame);
        getLayoutInflater().inflate(R.layout.activity_friend_list, contentLayout);

        setTitle(currentUser.getUsername());

        Button btButt = (Button) findViewById(R.id.btButton);

        btButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(FriendListActivity.this, BluetoothActivity.class);
                startActivity(i);
            }
        });

        ListView users = (ListView) findViewById(R.id.friendListView);

        ArrayList<User> friends = new ArrayList<>();

        for(String uid : currentUser.getFriends()){
            friends.add(userListInst.getUser(uid));
        }


        users.setAdapter(new ArrayAdapter<User>(this, android.R.layout.simple_list_item_1, friends));


        users.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Bundle userBundle = new Bundle();
                String uid = userListInst.getUserId((User) adapterView.getAdapter().getItem(i));
                userBundle.putString("userID", uid);
                Intent intent = new Intent(FriendListActivity.this, UserProfileActivity.class);
                intent.putExtras(userBundle);
                startActivity(intent);
            }
        });

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