package com.example.outdoors;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class DBAuth {

    private static final FirebaseAuth mAuth = FirebaseAuth.getInstance();


    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();


    private DBAuth(){
    }

    public FirebaseAuth getAuth(){
        return mAuth;
    }

    public FirebaseFirestore getDB(){
        return db;
    }

    private static class SingletonHolder {
        public static final DBAuth instance = new DBAuth();
    }

    public static DBAuth getInstance() {
        return DBAuth.SingletonHolder.instance;
    }


}
