/*

Povezivanje na Firebase baze i storage

 */


package com.example.outdoors;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

public class DBAuth {

    private static final FirebaseAuth mAuth = FirebaseAuth.getInstance();


    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private final FirebaseDatabase fbdb = FirebaseDatabase.getInstance();

    private final FirebaseStorage fbs = FirebaseStorage.getInstance();


    private DBAuth(){
    }

    public FirebaseAuth getAuth(){
        return mAuth;
    }

    public FirebaseFirestore getDB(){
        return db;
    }

    public FirebaseDatabase getFBDB() { return fbdb;}

    public FirebaseStorage getStorage() {return fbs;}

    private static class SingletonHolder {
        public static final DBAuth instance = new DBAuth();
    }

    public static DBAuth getInstance() {
        return DBAuth.SingletonHolder.instance;
    }


}
