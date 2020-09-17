package com.example.outdoors;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

public class LogIn extends AppCompatActivity {

    private static String TAG = "LOG IN ACTIVITY";

    private FirebaseAuth mAuth;

    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        mAuth = DBAuth.getInstance().getAuth();

        db = DBAuth.getInstance().getDB();

        Button logInButt = (Button) findViewById(R.id.logInButton);
        logInButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLogInfo();
            }
        });
    }

    private void getLogInfo(){
        EditText usernameET = (EditText) findViewById(R.id.usernameLogInEdit);
        String username = usernameET.getText().toString();
        EditText passwordET = (EditText) findViewById(R.id.passwordLogInEdit);
        String password = passwordET.getText().toString();
        getEmail(username, password);
    }

    private void getEmail(String username, String password){
        final String pw = password;
        db.collection("users")
                .whereEqualTo("username", username)
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
                                    logIn(user.email, pw);
                                }
                            }else{
                                TextView usernameErr = (TextView) findViewById(R.id.usernameLogInError);
                                String errMsg = "Username doesn't exist";
                                usernameErr.setText(errMsg);
                            }
                        }else{
                            Log.d(TAG, "Error getting data" , task.getException());
                            Toast.makeText(LogIn.this, "Error getting data", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void logIn(String email,String password){
        mAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "LOG IN SUCCESS ");
                            FirebaseUser user = mAuth.getCurrentUser();
                            UserAuthentication inst = UserAuthentication.getInstance();
                            inst.setUserAndUpdate(user, LogIn.this);
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LogIn.this, "Log In failed", Toast.LENGTH_SHORT).show();
                            TextView pwErr = (TextView) findViewById(R.id.passwordLogInError);
                            String errMsg = "Wrong password";
                            pwErr.setText(errMsg);
                            UserAuthentication.getInstance().updateUI(LogIn.this,null);
                        }
                    }
                });
    }
}