package com.example.ozangokdemir.convotracker;

import android.content.Intent;
import android.content.SharedPreferences;
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


public class LoginSignupActivity extends AppCompatActivity implements View.OnClickListener {

    TextView mTwSignUp, mTwSignIn;
    EditText mEtPassword, mEtEmail;
    private FirebaseAuth mAuth;
    private final String TAG = LoginSignupActivity.class.getSimpleName();

    //Creating an instance to the shared preferences. I will use this to cache the last entered email for user convenience.
    SharedPreferences prefs;
    SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_signup);
        initWidgets();
        mAuth = FirebaseAuth.getInstance();

        //Initialize the shared preferences(app cache) reference and its editor.
        prefs = getSharedPreferences(getResources().getString(R.string.shared_prefs_key), 0);
        editor = prefs.edit();

        //If there is a cached email, retrieve it and put it in the email input box for user's convenience (remember me kinda thing).
        String emailCache=prefs.getString(getResources().getString(R.string.email_cache_key), "");
        mEtEmail.setText(emailCache);

    }


    //When the app is shut down, sign the user out by default.
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAuth.signOut();
    }

    private void signInWithFirebase(){

        //Retrive data from the edit text fields on the screen.
        final String email = mEtEmail.getText().toString();
        final String password = mEtPassword.getText().toString();

        //If the input fields are empty, ask the user for password and email input.
        if (email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "Please enter your email and password to activate tracking"
                    ,Toast.LENGTH_SHORT).show();
        }

        //If the user did input credentials, attempt Firebase login with those credentials.
        else {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            //Firebase sign in was successful!
                            if (task.isSuccessful()) {


                                //Before moving on, cache the user's valid email address and remember it next time for convenience.
                                editor.putString(getString(R.string.email_cache_key), email);
                                editor.commit();

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

                            }

                            //Firebase authentication sign in failed!
                            else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(LoginSignupActivity.this, "Hmm.. Sign in failed, please check your email and password.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }


    //Simple helper method for finding the widgets from the layout xml.
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






    //Callback for the onClick interface that this interface implements.
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
