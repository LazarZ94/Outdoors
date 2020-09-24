package com.example.outdoors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static String TAG = "MAIN ACTIVITY";

    private static int RC_SIGN_IN = 1;

    private FirebaseAuth mAuth;

    private GoogleSignInClient mGoogleSignInClient;

    UserList userInst;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button logInButt = (Button) findViewById(R.id.logIn);
        logInButt.setOnClickListener(this);

        Button logInGoogleButt = (Button) findViewById(R.id.logInGoogle);
        logInGoogleButt.setOnClickListener(this);

        TextView signUpLink = (TextView) findViewById(R.id.signUp);
        signUpLink.setOnClickListener(this);

        userInst = UserList.getInstance();

        mAuth = DBAuth.getInstance().getAuth();

        String webClient = getString(R.string.default_web_client_id);
        userInst.setGoogleSignInClient(this, webClient);

        mGoogleSignInClient = userInst.getGSC();


    }

    private void googleSignIn(){
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> task){
        try{
            GoogleSignInAccount account = task.getResult(ApiException.class);
            if(account != null){
               manageGoogleAuth(account);
            }

            //String idToken = account.getIdToken();

            //SIGNED IN

        }catch (ApiException e) {
            Log.w(TAG, "signInResult: failed code=" + e.getStatusCode());
            //ERROR SIGNING IN
        }
    }

    private void manageGoogleAuth(GoogleSignInAccount account){
        Log.d(TAG, "FB AUTH WITH GOOGLE " + account.getId());

        AuthCredential creds = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(creds).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Log.d(TAG, "GOOGLE SIGN IN SUCCESSFUL");
                    FirebaseUser userFB = mAuth.getCurrentUser();
                    Log.d(TAG, "USERFB ATTRS: " + userFB.getDisplayName() + userFB.getEmail());
                    userInst.updateUsers(MainActivity.this);
                }else{
                    Toast.makeText(MainActivity.this, "Sign in with google failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }



    @Override
    protected void onStart() {
        super.onStart();
        //GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        //IF ALLREADY SIGNED IN account != null
        FirebaseUser currUser = mAuth.getCurrentUser();
        Log.d(TAG, "CURRENT USER MAINACT PRVI PUT" + currUser);
        UserList.getInstance().updateUsers(this);
    }



    @Override
    public void onClick(View view){
        switch(view.getId()){
            case R.id.logIn: {
                Log.d(TAG, "Clicked LOGINBUTTON GOING TO LOG IN SCREEN");
                Intent i = new Intent(MainActivity.this, LogIn.class);
                startActivity(i);
                break;
            }
            case R.id.signUp: {
                Log.d(TAG, "Clicked SIGNUPBUTTON GOING TO SIGN UP SCREEN");
                Intent i = new Intent(MainActivity.this, SignUp.class);
                startActivity(i);
                break;
            }
            case R.id.logInGoogle: {
                Log.d(TAG, "Clicked LOGINGOOGLE GOING TO MAIN SCREEN");
                googleSignIn();
                break;
            }
        }
    }

    //TODO KONFLIKT ZA GOOGLE LOGIN USERNAME (WHILE TREBA)
    //TODO FJE I IZGLED
    //TODO LAYOUT ZA INPUT FIELD I ERRMSG ZA SIGNUP
    //TODO VALIDACIJA BROJA TELEFONA I AUTOFILL

    //TODO LANDSCAPE

    //TODO REFACTOR USERDB I DA IDE KROZ USERLIST SVE I U LOGIN I SIGNUP
    //TODO USERPROFILE STRANA I FRAGMENTI ZA UPRAVLJANJE FRIENDLISTE

    //TODO BT FRIEND ADD
    //TODO PROVERI DA LI SU VEC PRIJATELJI

    //TODO REFACTOR SVE ZA BAZU DA BUDE JEDNA KLASA AUTH I USERLIST DA KORISTE AUTH IDE PREKO UL

    //TODO MAIL CONFIRM, INFO RECOVERY

}