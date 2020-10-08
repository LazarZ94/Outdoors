package com.example.outdoors;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

public class DBAuth {

    private static final FirebaseAuth mAuth = FirebaseAuth.getInstance();


    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private final FirebaseDatabase fbdb = FirebaseDatabase.getInstance();


    private DBAuth(){
    }

    public FirebaseAuth getAuth(){
        return mAuth;
    }

    public FirebaseFirestore getDB(){
        return db;
    }

    public FirebaseDatabase getFBDB() { return fbdb;}

    private static class SingletonHolder {
        public static final DBAuth instance = new DBAuth();
    }

    public static DBAuth getInstance() {
        return DBAuth.SingletonHolder.instance;
    }


}
