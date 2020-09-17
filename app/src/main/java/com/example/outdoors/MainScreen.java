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

    TextView tw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        mAuth = DBAuth.getInstance().getAuth();

        db = DBAuth.getInstance().getDB();

        gsc = UserAuthentication.getInstance().getGSC();


        final User currUser = UserAuthentication.getInstance().getCurrentUser();
        String display = currUser!=null ? currUser.getUsername() : "No login info";

        tw = (TextView) findViewById(R.id.textView);
        tw.setText(display);


        ArrayList<User> userList = UserList.getInstance().getUserList();

        ListView requests = (ListView) findViewById(R.id.mainScreenReuests);

        Log.d(TAG, "CURRENT USER JE " + currUser.toString());

        Log.d(TAG, "CURRUSER FRIENDREQUESTS : " + currUser.getFriendRequests());

        requests.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, currUser.getFriendRequests()));



        final String currId = mAuth.getCurrentUser().getUid();

        requests.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String uid = UserList.getInstance().getUserId((User) adapterView.getAdapter().getItem(i));
                Map<String, Object> currAddFr = new HashMap<>();
                currAddFr.put("id", uid);
                Map<String, Object> otherAddFr = new HashMap<>();
                otherAddFr.put("id", currId);
                db.collection("users")
                        .document(currId).collection("friends").add(currAddFr);
                db.collection("users")
                        .document(uid).collection("friends").add(otherAddFr);
            }
        });


        ListView users = (ListView) findViewById(R.id.mainScreenUserList);


        users.setAdapter(new ArrayAdapter<User>(this, android.R.layout.simple_list_item_1, userList));


        users.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Bundle userBundle = new Bundle();
                String uid = UserList.getInstance().getUserId((User) adapterView.getAdapter().getItem(i));
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
                mAuth.signOut();
                gsc.signOut();
                Intent i = new Intent(MainScreen.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        });


    }

}