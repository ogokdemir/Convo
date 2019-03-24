package com.example.ozangokdemir.convomap;

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

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    EditText mEtEmail, mEtPassword;
    TextView mTwSignInButton;
    FirebaseAuth mAuth;
    private static final String TAG = LoginActivity.class.getSimpleName();

    //Creating an instance to the shared preferences. I will use this to cache the last entered email for user convenience.
    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initWidgets();

        mAuth = FirebaseAuth.getInstance(); //instantiate the firebase auth api reference.

        //Initialize the shared preferences(app cache) reference and its editor.
        prefs = getSharedPreferences(getResources().getString(R.string.shared_prefs_key), 0);
        editor = prefs.edit();

        //If there is a cached email, retrieve it and put it in the email input box for user's convenience (remember me kinda thing).
        String emailCache=prefs.getString(getResources().getString(R.string.email_cache_key), "");
        mEtEmail.setText(emailCache);


    }

    //Sign out the user by default when the app is shut down.
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAuth.signOut();
    }


    //Simple helper for finding the widgets from the xml layout.
    private void initWidgets(){
     mEtEmail = (EditText) findViewById(R.id.et_signin_user_email);
     mEtPassword = (EditText) findViewById(R.id.et_signin_user_password);
     mTwSignInButton = (TextView) findViewById(R.id.tw_sign_in);
     mTwSignInButton.setOnClickListener(this);

    }

    private void signInWithFirebase(){

        //Retrieve the data from email and the password input.
        final String email = mEtEmail.getText().toString();
        final String password = mEtPassword.getText().toString();

        //If the user did not enter anything, prompt them.
        if (email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "Please enter your email and password to view other users' location"
                    ,Toast.LENGTH_SHORT).show();
        }

        //If they entered information, attempt Firebase login.
        else {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            //Login was successful!
                            if (task.isSuccessful()) {
                                // Sign in success, direct the user to the TrackerActivity which will publish their location.
                                Log.d(TAG, "signInWithEmail:success");

                                //first, cache this valid email address and remember it for the next time the user enters the app.
                                editor.putString(getResources().getString(R.string.email_cache_key), email);
                                editor.commit();


                                //second, start the UserStatusService.
                                Intent startUserStatusService = new Intent(LoginActivity.this, UserStatusService.class);
                                startService(startUserStatusService);

                                //now take the user to the display activity map. pass the email and password to DisplayActivity, too.
                                Intent toDisplayActivity = new Intent(LoginActivity.this, DisplayActivity.class);
                                String[] emailAndPassword = {email, password};
                                Bundle bundle = new Bundle();
                                bundle.putStringArray(DisplayActivity.INTENT_RECEIVE_KEY, emailAndPassword);
                                toDisplayActivity.putExtras(bundle);
                                startActivity(toDisplayActivity);

                             //Login failed, notify them.
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


    //onClick callback for the OnClickListener interface this activity implements.
    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.tw_sign_in:
                signInWithFirebase();
                break;
        }
    }
}

