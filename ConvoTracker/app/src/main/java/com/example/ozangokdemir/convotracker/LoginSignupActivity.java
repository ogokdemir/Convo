package com.example.ozangokdemir.convotracker;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginSignupActivity extends AppCompatActivity implements View.OnClickListener {

    TextView mTwSignUp, mTwSignIn;
    EditText mEtPassword, mEtEmail;
    private FirebaseAuth mAuth;


    private final String TAG = LoginSignupActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_signup);
        initWidgets();

        mAuth = FirebaseAuth.getInstance();

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        //If the user is currently signed in, directly move on the tracker activity.
        if(currentUser != null){
            Intent toTrackerActivity = new Intent (this, TrackerActivity.class);
            startActivity(toTrackerActivity);
        }

        else{
            //if the user is not signed in, don't do anything because the activity already asks them to sign in.
        }
    }


    private void signInWithFirebase(){

        String email = mEtEmail.getText().toString();
        String password = mEtPassword.getText().toString();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, direct the user to the TrackerActivity which will publish their location.
                            Log.d(TAG, "signInWithEmail:success");

                            Intent toTrackerActivity = new Intent(LoginSignupActivity.this, TrackerActivity.class);
                            startActivity(toTrackerActivity);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginSignupActivity.this, "Hmm..Are you sure you already signed up?",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }


    private void initWidgets(){
        mTwSignIn = (TextView) findViewById(R.id.tw_sign_in);
        mTwSignUp = (TextView) findViewById(R.id.tw_signup);
        mTwSignIn.setOnClickListener(this);
        mTwSignUp.setOnClickListener(this);
        mEtEmail = (EditText) findViewById(R.id.et_signin_user_email);
        mEtPassword = (EditText) findViewById(R.id.et_signin_user_password);

    }



    //If the user does not have an account already, send them to the signup activity.
    public void sendUserToSignup(){
        Intent toSignupActivity = new Intent(this, SignUpActivity.class);
        startActivity(toSignupActivity);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.tw_sign_in:
                signInWithFirebase();
                break;
            case R.id.tw_signup:
                sendUserToSignup();
                break;
        }
    }
}
