package com.example.outdoors;

import android.content.Intent;
import android.content.res.Resources;
import android.os.UserManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Random;

public class UserAuthentication {

    private static String TAG = "USER AUTHENTICATION";

    private FirebaseAuth mAuth;
    private FirebaseUser currentUserFB;
    private User currentUser = null;
    private FirebaseFirestore db;

    private GoogleSignInClient mGoogleSignInClient;

    private UserAuthentication(){
        mAuth = DBAuth.getInstance().getAuth();
        currentUserFB = mAuth.getCurrentUser();
        db = DBAuth.getInstance().getDB();
    }

    public void setUserAndUpdate(final FirebaseUser user, final AppCompatActivity context){
        if(user!=null) {
            /*UserList userList = UserList.getInstance();
            if(userList.userExists(user.getUid())) {
                currentUser = userList.getUser(user.getUid());
                updateUI(context, user);
            }else{
                currentUser = addNewGoogleUser(user);
                updateUI(context,user);
            }*/
            String uid = user.getUid();
            DocumentReference userRef = db.collection("users").document(uid);
            userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if(documentSnapshot.getData()!=null) {
                        Log.d(TAG, "Current user is " + documentSnapshot.getData());
                        currentUser = documentSnapshot.toObject(User.class);
                        UserList.getInstance().setFriends(currentUser, user.getUid());
                        //UserList.getInstance().updateUsers();
                        Log.d(TAG, "POSLE POSTAVLJANJA user je " + currentUser.getUsername());
                        updateUI(context, user);
                    }else{
                        currentUser = addNewGoogleUser(user);
                        updateUI(context, user);
                    }
                }
            });
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
        User user = new User(mail,username,fName,lName, "");
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


    private static class SingletonHolder {
        public static final UserAuthentication instance = new UserAuthentication();
    }

    public static UserAuthentication getInstance() {
        return SingletonHolder.instance;
    }


    public User getCurrentUser(){
        return currentUser;
    }

    /*public void setCurrentUser(FirebaseUser currentUser) {
        this.currentUser = currentUser;
    }*/

    public void updateUI(AppCompatActivity context, FirebaseUser user){
        if(user != null){
            Log.d(TAG, "CURRENT USER U UPDATEUI " + currentUser);
            Intent i = new Intent(context, MainScreen.class);
            context.startActivity(i);
            context.finish();
        }
    }
}
