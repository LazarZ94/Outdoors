package com.example.outdoors;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

public class PlansActivity extends BaseDrawerActivity {

    public static int INVITES_ACT = 1;

    FirebaseFirestore fbfs = DBAuth.getInstance().getDB();

    NewPlanFrag frag = new NewPlanFrag();

    FloatingActionButton fab;

    String planTitle = "";

    boolean coordsSet = false;

    boolean newPlanFrag = false;

    double lat, lon;

    Date date = new Date();

    TextView emptyTW;

    RecyclerView recView;

    String planID = null;

    private BiMap<String, Plan> planList = HashBiMap.create();

    ArrayList<String> invites = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout contentLayout = (FrameLayout) findViewById(R.id.contentFrame);
        getLayoutInflater().inflate(R.layout.activity_plans, contentLayout);

        LinearLayout toolbarLayout  = (LinearLayout) findViewById(R.id.toolbarItems);
        toolbarLayout.setGravity(Gravity.RIGHT);
        toolbarLayout.setPadding(0,15,30,15);

        try{
            Log.w("IN PLANS ACT", "AAA");
            Intent intent = getIntent();
            Bundle userBundle = intent.getExtras();
            planID = userBundle.getString("planID");
            Log.w("RECEIVED PLAN ID", planID);
        }catch (Exception e){
//            Log.w("PLANS", "BUNDLE EMPTY");
        }

        if(planID != null){
            Bundle args = new Bundle();
            args.putString("planID", planID);
            frag.setArguments(args);
            Log.w("PLANSACT", "SETTIGN FRAG");
            getSupportFragmentManager().beginTransaction().replace(R.id.plansContainer, frag).commit();
        }else{
            emptyTW = (TextView) findViewById(R.id.emptyTW);

            recView = (RecyclerView) findViewById(R.id.plansRecView);

            fbfs.collection("plans").whereArrayContains("confirmed", userListInst.getCurrentUserID())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>(){
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful()){
                                for(QueryDocumentSnapshot doc : task.getResult()){
                                    planList.put(doc.getId(), doc.toObject(Plan.class));
                                }
                                userListInst.setPlans(planList);
                                showPlans();
                            }
                        }
                    });


            final ImageButton confirmButt = new ImageButton(this);
            confirmButt.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_check_24));
            confirmButt.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            confirmButt.setColorFilter(Color.WHITE);
            confirmButt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    frag.getTitle();
                    if(coordsSet){
                        final Plan newPlan = new Plan(planTitle, lat, lon, date, userListInst.getCurrentUserID(), invites);
                        final String planID = userListInst.getCurrentUserID() + new Date(System.currentTimeMillis());
                        fbfs.collection("plans").document(planID)
                                .set(newPlan)
                                .addOnSuccessListener(new OnSuccessListener<Void>(){
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        for(String inv : invites){
                                            sendInvite(inv, planID);
                                            currUser.addUserPlan(newPlan);
                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener(){
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(PlansActivity.this, "Problem uploading plan", Toast.LENGTH_SHORT).show();
                                    }
                                });
                        frag.closeFragment();
                        confirmButt.setVisibility(View.GONE);
                        fab.setVisibility(View.VISIBLE);
                    }else {
                        Toast.makeText(PlansActivity.this, "Please set coordinates by tapping the map", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            toolbarLayout.addView(confirmButt);

            if(!newPlanFrag){
                confirmButt.setVisibility(View.GONE);
            }


            fab = findViewById(R.id.plansFab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(PlansActivity.this, "FAB clicked", Toast.LENGTH_SHORT).show();
                    getSupportFragmentManager().beginTransaction().replace(R.id.plansContainer, frag).commit();
                    fab.setVisibility(View.GONE);
                    confirmButt.setVisibility(View.VISIBLE);
                }
            });
        }

    }

    private void sendInvite(String userID, String planID){
        DocumentReference userInvites = fbfs.collection("users").document(userID);
        Invite invite = new Invite(planID);
        userInvites.update("planInvites", FieldValue.arrayUnion(invite));
    }

    public Plan getPlan(String id){
        Log.w("GET PLAN", String.valueOf(planList.size()));
        return planList.get(id);
    }

    private void showPlans(){
        ArrayList<String> planIDs = new ArrayList();
        for(String key : planList.keySet()){
            planIDs.add(key);
        }
        if(!planIDs.isEmpty()) {
            emptyTW.setVisibility(View.GONE);
//            RecViewAdapter recAdapter = new RecViewAdapter(getActivity().getApplicationContext(), usernames, avatar, SENT_VIEW);
            RecViewAdapter recAdapter = new RecViewAdapter(this, planIDs, RecViewAdapter.PLAN_VIEW, planList);
            recView.setAdapter(recAdapter);
            recView.setLayoutManager(new LinearLayoutManager(this));
            recView.setVisibility(View.VISIBLE);
        }else{
            recView.setVisibility(View.GONE);
            emptyTW.setText("Nothing here.");
            emptyTW.setVisibility(View.VISIBLE);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == INVITES_ACT){
            Log.w("REQ CODE", String.valueOf(requestCode));
            Log.w("RES CODE", String.valueOf(resultCode));
            if(resultCode == RESULT_OK){
                Log.w("AAAAAA", "RETURNED RESULT");
                Log.w("AAAAAAAAAAA", String.valueOf(data));
                invites = data.getStringArrayListExtra("result");
                frag.updateList();
                Log.w("DATA", String.valueOf(invites));
            }
        }
    }

    public void setCoords(double lat, double lon){
        this.lat = lat;
        this.lon = lon;
        coordsSet = true;
    }

    public void setPlanTitle(String title){
        this.planTitle = title;
    }

    public void setDate(int y, int m, int d){
        date.setYear(y);
        date.setMonth(m);
        date.setDate(d);
        String dateLabel = String.valueOf(d) + "-" + String.valueOf(m) + "-" + String.valueOf(y);
        frag.setDateLabel(dateLabel);
    }

    public void setTime(int h, int m){
        date.setHours(h);
        date.setMinutes(m);
        String timeLabel = padTime(String.valueOf(h)) + ":" + padTime(String.valueOf(m));
        frag.setTimeLabel(timeLabel);
    }

    public String padTime(String time){
        if(time.length() >=2){
            return time;
        }
        return "0" + time;
    }

    public ArrayList<String> getInvites(){
        return this.invites;
    }

    public Date getDate(){
        return this.date;
    }

}
