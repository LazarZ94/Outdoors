package com.example.outdoors;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Array;
import java.util.regex.Pattern;

public class SignUp extends AppCompatActivity {

    private static String TAG = "SIGN UP ACTIVITY";

    private FirebaseFirestore db;
    private static final String COLLECTION = "users";

    private SectionsStatePagesAdapter mSectionsStatePagerAdapter;
    private ViewPager mViewPager;

    private String email;
    private String username;
    private String password;
    private String fName;
    private String lName;
    private String phoneNumber;

    private int EMAIL_ERROR = 0;
    private int USERNAME_ERROR = 1;
    private int PASSWORD_ERROR = 2;
    private int PASSWORD_CONF_ERROR = 3;
    private int FNAME_ERROR = 4;
    private int LNAME_ERROR = 5;
    private int PHONE_ERROR = 6;

    private String[] errors = {null, null, null, null, null, null, null};

    private static final Pattern USERNAME_PATTERN = Pattern.compile("[@#$%^&*()_+={}]");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{6,}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z]*$");

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mSectionsStatePagerAdapter = new SectionsStatePagesAdapter(getSupportFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

        mViewPager = (ViewPager) findViewById(R.id.container);
        setUpViewPager(mViewPager);

        mAuth = UserAuthentication.getInstance().getAuth();
        db = UserAuthentication.getInstance().getDB();

    }


    private SectionsStatePagesAdapter adapter;

    private void setUpViewPager(ViewPager viewPager){
        adapter = new SectionsStatePagesAdapter(getSupportFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        adapter.addFragment(new SUMainFrag(), "MainSUInfo");
        adapter.addFragment(new SUSecFrag(), "PersonalSUInfo");
        viewPager.setAdapter(adapter);
    }

    public void setViewPager(int fragNum){
        mViewPager.setCurrentItem(fragNum);
    }

    public void setInfo(){
        SUMainFrag main = (SUMainFrag) adapter.getItem(0);
        SUSecFrag pers = (SUSecFrag) adapter.getItem(1);
        this.email = validateMail(main.getInfo(R.id.signUpMailEdit));
        this.username = validateUsername(main.getInfo(R.id.signUpUsernameEdit));
        this.password = validatePass(main.getInfo(R.id.signUpPasswordEdit), main.getInfo(R.id.signUpPasswordConfirmEdit));
        this.fName = validateName(pers.getInfo(R.id.signUpFirstNameEdit), true);
        this.lName = validateName(pers.getInfo(R.id.signUpLastNameEdit), false);
        this.phoneNumber = validateNumber(pers.getInfo(R.id.signUpPhoneEdit));
        checkInput();
        //createUser();
    }

    private void checkInput(){
        int errs = 0;
        for(String err : errors){
            if(err!=null)
                errs++;
        }
        if(errs == 0){
            db.collection("users")
                    .whereEqualTo("username", username)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful()){
                                if(task.getResult().isEmpty()){
                                    createUser();
                                }else{
                                    errors[USERNAME_ERROR] = "Username taken";
                                    setErrors();
                                }

                            }else{
                                Log.d(TAG, "Error getting data" , task.getException());
                                Toast.makeText(SignUp.this, "Error getting data", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }else{
            setErrors();
        }
    }

    private void setErrors(){
        TextView mailErr = (TextView) findViewById(R.id.signUpMailError);
        TextView usernameErr = (TextView) findViewById(R.id.signUpUsernameError);
        TextView pwErr = (TextView) findViewById(R.id.signUpPasswordError);
        TextView pwcErr = (TextView) findViewById(R.id.signUpPasswordConfirmError);
        TextView fnameErr = (TextView) findViewById(R.id.signUpFirstNameError);
        TextView lnameErr = (TextView) findViewById(R.id.signUpLastNameError);
        TextView phoneErr = (TextView) findViewById(R.id.signUpPhoneError);
        mailErr.setText(errors[EMAIL_ERROR]);
        usernameErr.setText(errors[USERNAME_ERROR]);
        pwErr.setText(errors[PASSWORD_ERROR]);
        pwcErr.setText(errors[PASSWORD_CONF_ERROR]);
        fnameErr.setText(errors[FNAME_ERROR]);
        lnameErr.setText(errors[LNAME_ERROR]);
        phoneErr.setText(errors[PHONE_ERROR]);
        setViewPager(0);
    }

    private String validateNumber(String number){
        errors[PHONE_ERROR] = null;
        return number;
    }

    private String validateName(String name, boolean first){
        if(!NAME_PATTERN.matcher(name).matches()){
            if(first){
                errors[FNAME_ERROR] = "Name must only contain letters";
            }else{
                errors[LNAME_ERROR] = "Name must only contain letters";
            }
            return "";
        }else{
            int ind = first ? FNAME_ERROR : LNAME_ERROR;
            errors[ind] = null;
            return name;
        }
    }

    private String validatePass(String pw, String pwc){
        if(pw.isEmpty()){
            errors[PASSWORD_ERROR] = "This field is required";
            return "";
        }else if(pw.length()<6){
            errors[PASSWORD_ERROR] = "Password must be atleast 6 characters long";
            return "";
        }else if(pwc.isEmpty()){
            errors[PASSWORD_CONF_ERROR] = "Confirm your password";
            return "";
        }else if(!pw.equals(pwc)){
            errors[PASSWORD_CONF_ERROR] = "Passwords don't match";
            return "";
        }else if(!PASSWORD_PATTERN.matcher(pw).matches()){
            errors[PASSWORD_ERROR] = "Password must contain upper and lowercase letters and a number";
            return "";
        }else{
            errors[PASSWORD_ERROR] = null;
            errors[PASSWORD_CONF_ERROR] = null;
            return pw;
        }
    }

    private String validateUsername(String username){
        if(username.isEmpty()){
            errors[USERNAME_ERROR] = "This field is required";
            return "";
        }else if(USERNAME_PATTERN.matcher(username).matches()){
            errors[USERNAME_ERROR] = "Username can't contain special characters";
            return "";
        }else{
            errors[USERNAME_ERROR] = null;
            return username;
        }
    }

    private String validateMail(String mail){
        if(mail.isEmpty()){
            errors[EMAIL_ERROR] = "This field is required";
            return "";
        }else if(!Patterns.EMAIL_ADDRESS.matcher(mail).matches()){
            errors[EMAIL_ERROR] = "Invalid email address";
            return "";
        }else{
            errors[EMAIL_ERROR] = null;
            return mail;
        }
    }

    private void createUser(){
        mAuth.createUserWithEmailAndPassword(this.email, this.password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            //Toast.makeText(SignUp.this, "User created", Toast.LENGTH_SHORT).show();
                            FirebaseUser userFB = mAuth.getCurrentUser();
                            Toast.makeText(SignUp.this, "UserID" + userFB.getUid(), Toast.LENGTH_SHORT).show();
                            Log.w(TAG+" PRE POZIVA ADDUSER", "USERID = " + userFB.getUid());
                            String uid = userFB.getUid();
                            addNewUser(uid);
                            UserAuthentication inst = UserAuthentication.getInstance();
                            inst.setUserAndUpdate(userFB, SignUp.this);
                        }else {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignUp.this, "Error creating user", Toast.LENGTH_SHORT).show();
                            UserAuthentication.getInstance().updateUI(SignUp.this,null);
                        }
                    }
                });
    }

    private void addNewUser(String uid){
        User user = new User(email,username,fName,lName,phoneNumber);
        db.collection(COLLECTION)
                .document(uid).set(user);
    }
}