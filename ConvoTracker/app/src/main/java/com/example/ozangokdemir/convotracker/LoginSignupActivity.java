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
    protected void onDestroy() {
        super.onDestroy();
        mAuth.signOut();
    }

    private void signInWithFirebase(){

        final String email = mEtEmail.getText().toString();
        final String password = mEtPassword.getText().toString();

        if (email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "Please enter your email and password to activate tracking"
                    ,Toast.LENGTH_SHORT).show();
        }

        else {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, direct the user to the TrackerActivity which will publish their location.
                                Log.d(TAG, "signInWithEmail:success");

                                Intent toTrackerActivity = new Intent(LoginSignupActivity.this, TrackerActivity.class);
                                String[] emailAndPassword = new String[2];

                            /*
                            Sending the user's email and password to the tracker activity so updates on their location is entered
                            on the Firebase Realtime Database.

                            */
                                emailAndPassword[0] = email;
                                emailAndPassword[1] = password;
                                Bundle packageForTrackerActivity = new Bundle();
                                packageForTrackerActivity.putStringArray(TrackerActivity.INTENT_RECEIVE_CODE, emailAndPassword);
                                toTrackerActivity.putExtras(packageForTrackerActivity);

                                startActivity(toTrackerActivity);

                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(LoginSignupActivity.this, "Hmm.. Are you sure you already signed up?",
                                        Toast.LENGTH_SHORT).show();
                            }

                            // ...
                        }
                    });
        }
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
