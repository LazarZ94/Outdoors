package com.example.outdoors;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainScreen extends AppCompatActivity {

    private static String TAG = "MAIN SCREEN";

    private FirebaseAuth mAuth;

    private GoogleSignInClient gsc;

    private FirebaseFirestore db;

    private UserList userListInst = UserList.getInstance();

    TextView tw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        mAuth = DBAuth.getInstance().getAuth();

        db = DBAuth.getInstance().getDB();

        gsc = userListInst.getGSC();


        final User currUser = userListInst.getCurrentUser();
        String display = currUser!=null ? currUser.getUsername() : "No login info";

        tw = (TextView) findViewById(R.id.textView);
        tw.setText(display);


        ArrayList<User> userList = userListInst.getUserList();

        ListView requests = (ListView) findViewById(R.id.mainScreenReuests);

        Log.d(TAG, "CURRENT USER JE " + currUser.toString());

        Log.d(TAG, "CURRUSER FRIENDREQUESTS : " + currUser.getFriendRequests());

        ArrayList<String> reqList = new ArrayList<>();
        for(String id : currUser.getFriendRequests()){
            reqList.add(userListInst.getUser(id).getUsername());
        }

        requests.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, reqList));



        final String currId = mAuth.getCurrentUser().getUid();

        requests.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                /*String uid = UserList.getInstance().getUserId((User) adapterView.getAdapter().getItem(i));
                Map<String, Object> currAddFr = new HashMap<>();
                currAddFr.put("id", uid);
                Map<String, Object> otherAddFr = new HashMap<>();
                otherAddFr.put("id", currId);
                db.collection("users")
                        .document(currId).collection("friends").add(currAddFr);
                db.collection("users")
                        .document(uid).collection("friends").add(otherAddFr);*/
                String userID = userListInst.getUserId((String) adapterView.getAdapter().getItem(i));
                User other = userListInst.getUser(userID);
                ArrayList<String> currFReq = new ArrayList<>(currUser.friendRequests);
                currFReq.remove(userID);
                db.collection("users").document(currId).update("friendRequests", currFReq);
                ArrayList<String> otherSentFR = new ArrayList<>(other.sentFriendRequests);
                otherSentFR.remove(currId);
                db.collection("users").document(userID).update("sentFriendRequests", otherSentFR);
                ArrayList<String> currFriends = new ArrayList<>(currUser.friends);
                currFriends.add(userID);
                db.collection("users").document(currId).update("friends", currFriends);
                ArrayList<String> otherFriends = new ArrayList<>(other.friends);
                otherFriends.add(currId);
                db.collection("users").document(userID).update("friends", otherFriends);
                userListInst.updateUsers(MainScreen.this);
            }

        });


        ListView users = (ListView) findViewById(R.id.mainScreenUserList);

        userList.remove(currUser);

        users.setAdapter(new ArrayAdapter<User>(this, android.R.layout.simple_list_item_1, userList));


        users.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Bundle userBundle = new Bundle();
                String uid = userListInst.getUserId((User) adapterView.getAdapter().getItem(i));
                userBundle.putString("userID", uid);
                Intent intent = new Intent(MainScreen.this, UserProfile.class);
                intent.putExtras(userBundle);
                startActivity(intent);
            }
        });

        Button logOutButt = (Button) findViewById(R.id.logOutButton);
        logOutButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "LOGOUT CALLED");
                mAuth.signOut();
                gsc.signOut();
                Intent i = new Intent(MainScreen.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        });



        Button btButt = (Button) findViewById(R.id.btButton);

        btButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainScreen.this, BluetoothActivity.class);
                startActivity(i);
            }
        });

    }

}