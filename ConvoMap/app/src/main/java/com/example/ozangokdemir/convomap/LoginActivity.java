package com.example.ozangokdemir.convomap;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    EditText mEtEmail, mEtPassword;
    TextView mTwSignInButton;
    FirebaseAuth mAuth;
    private static final String TAG = LoginActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initWidgets();

        mAuth = FirebaseAuth.getInstance();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAuth.signOut();
    }

    private void initWidgets(){

     mEtEmail = (EditText) findViewById(R.id.et_signin_user_email);
     mEtPassword = (EditText) findViewById(R.id.et_signin_user_password);
     mTwSignInButton = (TextView) findViewById(R.id.tw_sign_in);
     mTwSignInButton.setOnClickListener(this);

    }

    private void signInWithFirebase(){

        final String email = mEtEmail.getText().toString();
        final String password = mEtPassword.getText().toString();

        if (email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "Please enter your email and password to view other users' location"
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


                                //Sign in was successful, now take the user to the display activity map.
                                Intent toDisplayActivity = new Intent(LoginActivity.this, DisplayActivity.class);

                                startActivity(toDisplayActivity);

                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(LoginActivity.this, "Hmm.. sign in failed. Please check your email and password.",
                                        Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
        }
    }



    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.tw_sign_in:
                signInWithFirebase();
                break;
        }
    }
}

