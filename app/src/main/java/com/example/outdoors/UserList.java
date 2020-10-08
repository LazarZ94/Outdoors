package com.example.outdoors;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Random;

public class UserList {

    private static String TAG = "USER LIST";

    private FirebaseAuth mAuth;
    private FirebaseUser currentUserFB;
    private User currentUser = null;
    private FirebaseFirestore db;
    private FirebaseDatabase fbdb;

    private GoogleSignInClient mGoogleSignInClient;

    private BiMap<String, User> userList;


    private UserList(){
        mAuth = DBAuth.getInstance().getAuth();
        currentUserFB = mAuth.getCurrentUser();
        db = DBAuth.getInstance().getDB();
        fbdb = DBAuth.getInstance().getFBDB();
        userList = HashBiMap.create();
        getAllUsers(null);
    }

    public ArrayList<User> getOnlineUsers(){
        ArrayList<User> onlineUsers = new ArrayList<>();
        for(User user : userList.values()){
            if(user.getStatus())
                if(!user.equals(currentUser))
                    onlineUsers.add(user);
        }
        return onlineUsers;
    }
    

    public String getCurrentUserID(){
        return getUserId(currentUser.getUsername());
    }

    private static class SingletonHolder {
        public static final UserList instance = new UserList();
    }

    public static UserList getInstance() {
        return UserList.SingletonHolder.instance;
    }

    public String getUserId(User user){
        return userList.inverse().get(user);
    }

    public String getUserId(String username){
        User user = null;
        for(User usr : new ArrayList<User>(userList.values())){
            if(usr.getUsername().equals(username))
                user = usr;
        }
        if(user!=null)
            return userList.inverse().get(user);
        else
            return "";
    }

    public User getUser(String id){
        return userList.get(id);
    }

    public boolean userExists(String id){
        if(userList.containsKey(id)){
            return true;
        }else{
            return false;
        }
    }

    public ArrayList<User> getUserList(){
        return new ArrayList<User>(userList.values());
    }

    public void updateUsers(AppCompatActivity context){
        userList = HashBiMap.create();
        getAllUsers(context);
    }

    private void getAllUsers(final AppCompatActivity context){
        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            if(!task.getResult().isEmpty()) {
                                for (QueryDocumentSnapshot doc : task.getResult()) {
                                    Log.d(TAG, doc.getId() + " => " + doc.getData());
                                    User user = doc.toObject(User.class);
                                    if (user.statusRef == null) {
                                        user.setStatusListener(doc.getId());
                                    }
                                    user.setLocationListener(doc.getId());
                                    Log.d(TAG, "EMAIL JE " + user.email);
                                    userList.put(doc.getId(), user);
                                }
                                Log.d(TAG, "PRE SETIUPDATE USERLIST JE " + userList);
                                setUserAndUpdate(mAuth.getCurrentUser(), context);
                                Log.d(TAG, "BROJ KORISNIKA JE " + userList.size());
                            }
                        }else{
                            Log.d(TAG, "Error getting data" , task.getException());
                        }
                    }
                });
    }

    public User getCurrentUser(){
        return currentUser;
    }



    private void setUserStatus(final String UID){
        final DatabaseReference statusRef = fbdb.getReference("users/"+UID+"/onlineStatus");;
        DatabaseReference connRef = fbdb.getReference(".info/connected");
        connRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                statusRef.onDisconnect().setValue(false);
                statusRef.setValue(connected);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void setUserAndUpdate(final FirebaseUser user, final AppCompatActivity context){
        if(user!=null) {
            if(userExists(user.getUid())) {
                currentUser = getUser(user.getUid());
            }else{
                currentUser = addNewGoogleUser(user);
            }
            setUserStatus(user.getUid());
            if(context!= null) {
                updateUI(context, user);
            }
        }else{
            currentUser = null;
        }
    }


    private User addNewGoogleUser(FirebaseUser userFB){
        Random rand = new Random();
        String uid = userFB.getUid();
        String mail = userFB.getEmail();
        String[] names = userFB.getDisplayName().split(" ", 2);
        String username = names[0] + "#" + rand.nextInt(1000);
        String fName = names[0];
        String lName = "";
        if(names.length>1)
            lName = names[1];
        ArrayList<String> emptyArr = new ArrayList<>();
        User user = new User(mail,username,fName,lName, "", emptyArr, emptyArr, emptyArr);
        fbdb.getReference("users/" + uid + "/onlineStatus").setValue(true);
        fbdb.getReference("users/" + uid + "/lat").setValue(0.0d);
        fbdb.getReference("users/" + uid + "/lon").setValue(0.0d);
        db.collection("users")
                .document(uid).set(user);
        return user;
    }

    public void setGoogleSignInClient(AppCompatActivity context, String webClient){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClient)
                .requestProfile()
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
    }

    public GoogleSignInClient getGSC(){
        return  mGoogleSignInClient;
    }

    public void logOut(Context context){
        Log.d(TAG, "LOGOUT CALLED");
        mAuth.signOut();
        mGoogleSignInClient.signOut();
        final DatabaseReference statusRef = fbdb.getReference("users/"+getCurrentUserID()+"/onlineStatus");
        statusRef.setValue(false);
        final DocumentReference userRef = db.collection("users").document(getCurrentUserID());
        userRef.update("onlineStatus", false);
        Intent i = new Intent(context, MainActivity.class);
        context.startActivity(i);
    }

    public void updateUI(AppCompatActivity context, FirebaseUser user){
        if(user != null){
            Log.d(TAG, "CURRENT USER U UPDATEUI " + currentUser);
            Intent i = new Intent(context, MainScreen.class);
            context.startActivity(i);
            context.finish();
        }
    }

}
