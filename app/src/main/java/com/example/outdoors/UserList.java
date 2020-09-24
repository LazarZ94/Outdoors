package com.example.outdoors;

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

    private GoogleSignInClient mGoogleSignInClient;

    private BiMap<String, User> userList;

    //private ArrayList<User> userList;


    private UserList(){
        mAuth = DBAuth.getInstance().getAuth();
        currentUserFB = mAuth.getCurrentUser();
        db = DBAuth.getInstance().getDB();
        userList = HashBiMap.create();
        getAllUsers(null);
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
                                    Log.d(TAG, "EMAIL JE " + user.email);
                                    userList.put(doc.getId(), user);
                                    //setFriends(user, doc.getId());
                                    //Log.d(TAG,"USER JE " + doc.getId() + "LISTE SU " + user.friends + " " + user.friendRequests + " " + user.sentFriendRequests);
                                }
                                Log.d(TAG, "PRE SETIUPDATE USERLIST JE " + userList);
                                setUserAndUpdate(mAuth.getCurrentUser(), context);
                                Log.d(TAG, "BROJ KORISNIKA JE " + userList.size());
                            }else{
                                //User user = new User("","","","","");
                                //userList.put("aaa", user);
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

    public void setFriends(User user, String userID){
        getFriendRef("friends", userID, user);
        getFriendRef("friendRequests", userID, user);
        getFriendRef("sentFriendRequests", userID, user);
    }

    private void getFriendRef(final String collection,final String userID, final User user){
        db.collection("users").document(userID)
                .collection(collection).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            if(collection.equals("friends")) {
                                for (QueryDocumentSnapshot doc : task.getResult()) {
                                    user.friends.add((String) doc.get("id"));
                                    Log.d(TAG, "USER JE " + userID + "FRIEND JE " + doc.getData() + "LISTA JE " + user.friends);
                                }
                            }else if(collection.equals("friendRequests")){
                                for (QueryDocumentSnapshot doc : task.getResult()) {
                                    user.friendRequests.add((String) doc.get("id"));
                                    Log.d(TAG, "USER JE " + userID + "FRIENDREQ JE " + doc.getData() + "LISTA JE " + user.friendRequests);
                                }
                            }else if (collection.equals("sentFriendRequests")){
                                for (QueryDocumentSnapshot doc : task.getResult()) {
                                    user.sentFriendRequests.add((String) doc.get("id"));
                                    Log.d(TAG, "USER JE " + userID + "SENTFRIENDREQ JE " + doc.getData() + "LISTA JE " + user.sentFriendRequests);
                                }
                            }
                            userList.put(userID, user);
                        }else{
                            Log.d(TAG, "PROBLEM GETTING FRIEND INFO");
                        }
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
            if(context!= null) {
                updateUI(context, user);
            }
            /*String uid = user.getUid();
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
            });*/
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

    public void updateUI(AppCompatActivity context, FirebaseUser user){
        if(user != null){
            Log.d(TAG, "CURRENT USER U UPDATEUI " + currentUser);
            Intent i = new Intent(context, MainScreen.class);
            context.startActivity(i);
            context.finish();
        }
    }

}
