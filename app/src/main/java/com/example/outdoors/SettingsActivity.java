package com.example.outdoors;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingsActivity extends BaseDrawerActivity {

    UserList userListInst = UserList.getInstance();
    User currUser = userListInst.getCurrentUser();
    String currId = userListInst.getCurrentUserID();
    UserPreferences userPrefs;
    boolean oldPref;
    FirebaseFirestore db = DBAuth.getInstance().getDB();
    Switch serviceSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        FrameLayout contentLayout = (FrameLayout) findViewById(R.id.contentFrame);
        getLayoutInflater().inflate(R.layout.activity_settings, contentLayout);

        serviceSwitch = (Switch) findViewById(R.id.settingsServiceSwitch);



        LinearLayout toolbarLayout  = (LinearLayout) findViewById(R.id.toolbarItems);
        toolbarLayout.setGravity(Gravity.RIGHT);
        toolbarLayout.setPadding(0,15,30,15);



        ImageButton doneButt = new ImageButton(this);
        doneButt.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_check_24));
        doneButt.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        doneButt.setColorFilter(Color.WHITE);
        doneButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(SettingsActivity.this, "Button clicked", Toast.LENGTH_SHORT).show();
                Log.w("oldpref", String.valueOf(oldPref));
                Log.w("switch", String.valueOf(serviceSwitch.isChecked()));
                if((oldPref != serviceSwitch.isChecked())){
                    if(serviceSwitch.isChecked()){
                        currUser.getPreferences().setBackgroundService(true);
                    }else{
                        currUser.getPreferences().setBackgroundService(false);
                    }
                    DocumentReference usrRef = db.collection("users").document(currId);
                    usrRef.update("prefs", currUser.getPreferences());
                    Intent intent = new Intent(getApplicationContext(), MainScreenActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                }
            }
        });
        toolbarLayout.addView(doneButt);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(currUser.getPreferences() == null){
//            userPrefs = new UserPreferences(25, 25, false);
            currUser.setPreferences(new UserPreferences(25,25,false));
        }

        userPrefs = currUser.getPreferences();

        oldPref = userPrefs.getBackgroundService();

        if(userPrefs.getBackgroundService()){
            serviceSwitch.setChecked(true);
        }
    }
}
