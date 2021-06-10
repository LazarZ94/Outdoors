/*

Staticka singleton klasa za listu korisnika

 */


package com.example.outdoors;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Random;

public class UserList {

    private static String TAG = "USER LIST";

    private FirebaseAuth mAuth;
    private FirebaseUser currentUserFB;
    private User currentUser = null;
    private FirebaseFirestore db;
    private FirebaseDatabase fbdb;
    private FirebaseStorage fbs;
    private Bitmap currAvatar = null;
    private GoogleSignInClient mGoogleSignInClient;
    private BiMap<String, POI> poiList;
    private BiMap<String, User> userList;
    private BiMap<String, Bitmap> userAvatars = HashBiMap.create();


    private UserList(){
        mAuth = DBAuth.getInstance().getAuth();
        currentUserFB = mAuth.getCurrentUser();
        db = DBAuth.getInstance().getDB();
        fbdb = DBAuth.getInstance().getFBDB();
        fbs = DBAuth.getInstance().getStorage();
        userList = HashBiMap.create();
        poiList = HashBiMap.create();
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

    public ArrayList<POI> getPOIs(){
        ArrayList<POI> pois = new ArrayList<>(poiList.values());
        return pois;
    }

    public POI getPOI(String poiName){
        return poiList.get(poiName);
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
                                    String docID = doc.getId();
                                    Log.d(TAG, docID + " => " + doc.getData());
                                    User user = doc.toObject(User.class);
                                    if (user.statusRef == null) {
                                        user.setStatusListener(docID);
                                    }
                                    user.setLocationListener(docID);
                                    Log.d(TAG, "EMAIL JE " + user.email);
                                    setPOIList(user, docID);
                                    userList.put(docID, user);
                                }
                                Log.d(TAG, "PRE SETIUPDATE USERLIST JE " + userList);
                                getPOIs(mAuth.getCurrentUser(), context);
                                //setUserAndUpdate(mAuth.getCurrentUser(), context);
                                Log.d(TAG, "BROJ KORISNIKA JE " + userList.size());
                            }
                        }else{
                            Log.d(TAG, "Error getting data" , task.getException());
                        }
                    }
                });
    }

    private void getPOIs(final FirebaseUser FBuser, final AppCompatActivity context){
        db.collection("POI")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>(){
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(!task.getResult().isEmpty()){
                            for(QueryDocumentSnapshot doc : task.getResult()){
                                String docID = doc.getId();
                                POI poi = doc.toObject(POI.class);
                                StorageReference thumbRef = fbs.getReference().child("images/POI/scaled/"+poi.getuID()+"/"+poi.getName());
                                setThumb(poi, thumbRef);
                                poiList.put(docID, poi);
                            }
                        }
                        setUserAndUpdate(FBuser, context);
                    }
                });
    }

    private void setThumb(final POI poi, StorageReference path){
        final long mb = 1024 * 1024;
        path.getBytes(mb).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Log.d(TAG, "AAAAAAAAAAAAAAAAA SET THUMB");
                poi.setThumb(BitmapFactory.decodeByteArray(bytes,0,bytes.length));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
            }
        });
    }

    public User getCurrentUser(){
        return currentUser;
    }

    private void setPOIList(final User user, String uid){
        Log.d(TAG, "setPOIList: IN POIIII");
        //TODO: uzimaj poi listu iz firestore umesto storage
        StorageReference listPOI = fbs.getReference().child("images/POI/full/"+ uid + "/");
        listPOI.listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        for(StorageReference item : listResult.getItems()){
                            Log.d(TAG, "onSuccess: ITEM" + item);
                            addPOIMetadata(user, item);
                            user.POIs.add(item);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: ERROR GETTING FILE");;
            }
        });
    }

    private void addPOIMetadata(final User user, StorageReference ref){
        final String fileName = ref.getName();
        ref.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                user.POIMetadata.put(fileName, storageMetadata);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: Problem getting metadata");
            }
        });
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
            loadUserAvatars();
            if(context!= null) {
                updateUI(context, user);
            }
        }else{
            currentUser = null;
        }
    }

    private void loadUserAvatars(){
        ArrayList<User> users = new ArrayList<>(userList.values());
        for (User usr : users){
            if(!userAvatars.containsKey(getUserId(usr))) {
                loadAvatar(getUserId(usr), usr);
            }else{
                usr.setAvatar(userAvatars.get(getUserId(usr)));
            }
        }
    }

    private void loadAvatar(String uID, final User user){
        StorageReference avatarRef = fbs.getReference().child("images/avatars/"+uID);
        final long size = 1024 * 1024 * 10;
        avatarRef.getBytes(size).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                loadBitmapAvatar(user, bytes);
            }
        }).addOnFailureListener(new OnFailureListener(){
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void loadBitmapAvatar(User user, byte[] bytes){
        Bitmap avatar = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        userAvatars.put(getUserId(user), avatar);
        user.setAvatar(avatar);
    }

    public boolean isMyServiceRunning(AppCompatActivity context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
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
        User user = new User(mail,username,fName,lName, "", emptyArr, emptyArr, emptyArr, null);
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
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    public void updateUI(AppCompatActivity context, FirebaseUser user){
        if(user != null){
            Log.d(TAG, "CURRENT USER U UPDATEUI " + currentUser);
            Intent i = new Intent(context, MainScreenActivity.class);
            context.startActivity(i);
            context.finish();
        }
    }

}
