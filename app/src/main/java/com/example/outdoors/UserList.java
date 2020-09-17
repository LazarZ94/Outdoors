package com.example.outdoors;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class UserList {

    private static String TAG = "USER LIST";

    private FirebaseFirestore db = DBAuth.getInstance().getDB();

    private BiMap<String, User> userList;

    //private ArrayList<User> userList;


    private UserList(){
        userList = HashBiMap.create();
        getAllUsers();
    }

    public String getUserId(User user){
        return userList.inverse().get(user);
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

    public void updateUsers(){
        userList = HashBiMap.create();
        getAllUsers();
    }

    private void getAllUsers(){
        final String currId = DBAuth.getInstance().getAuth().getCurrentUser().getUid();
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
                                    setFriends(user, doc.getId());
                                    //Log.d(TAG,"USER JE " + doc.getId() + "LISTE SU " + user.friends + " " + user.friendRequests + " " + user.sentFriendRequests);
                                    //userList.put(doc.getId(), user);
                                }
                                Log.d(TAG, "BROJ KORISNIKA JE " + userList.size());
                            }else{
                                User user = new User("","","","","");
                                userList.put("aaa", user);
                            }
                        }else{
                            Log.d(TAG, "Error getting data" , task.getException());
                        }
                    }
                });
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

    private static class SingletonHolder {
        public static final UserList instance = new UserList();
    }


    public static UserList getInstance() {
        return UserList.SingletonHolder.instance;
    }


}
