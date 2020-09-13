package com.example.outdoors;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainScreen extends AppCompatActivity {

    private static String TAG = "MAIN SCREEN";

    private FirebaseAuth mAuth;

    private GoogleSignInClient gsc;

    TextView tw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        mAuth = UserAuthentication.getInstance().getAuth();

        gsc = UserAuthentication.getInstance().getGSC();

        User currUser = UserAuthentication.getInstance().getCurrentUser();
        String display = currUser!=null ? currUser.getUsername() : "No login info";

        tw = (TextView) findViewById(R.id.textView);
        tw.setText(display);

        Button logOutButt = (Button) findViewById(R.id.logOutButton);
        logOutButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                gsc.signOut();
                Intent i = new Intent(MainScreen.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        });


    }

}