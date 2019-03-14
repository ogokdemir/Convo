package com.example.ozangokdemir.convotracker;

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
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends AppCompatActivity{

    EditText mEtPassword, mEtEmail;
    TextView mTwSignUp;
    FirebaseAuth mAuth;
    private static final String TAG = SignUpActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        initWidgets();

        //initialize the firebase auth api reference.
        mAuth = FirebaseAuth.getInstance();
    }


    //Simply init the widgets.
    private void initWidgets(){
        mEtPassword = (EditText) findViewById(R.id.et_signup_user_password);
        mEtEmail = (EditText) findViewById(R.id.et_signup_user_email);
        mTwSignUp = (TextView) findViewById(R.id.tw_sign_up_complete);

    }

    /**
     * Callback method for the signup button tap.
     * Gets the email and password from the edittexts, checks their validity. Initiates Firebase signup.
     */
    public void onSignupTapped(View view){

        String userEmail = mEtEmail.getText().toString();
        String userPassword = mEtPassword.getText().toString();

        if(checkInputValidity(userEmail, userPassword)){
            signUpTheUser(userEmail, userPassword);
        }

    }

    /**
     * Helper method that checks whether the user's input is valid or not.
     * @param userEmail User's inputted email.
     * @param userPassword User's inputted password.
     * @return boolean isValid whether the input is valid or not.
     */

    private boolean checkInputValidity(String userEmail, String userPassword){

        Boolean isValid;

        //User skipped one of the input fields. Notify them with a Toast.
        if(userEmail.length()==0 || userPassword.length()==0){
            isValid = false;
            Toast.makeText(this, "Make sure you enter both your NCF email and a password.",Toast.LENGTH_SHORT).show();
        }

        //Firebase does not allow passwords to be shorter than 6 characters. Notify the user with a Toast.
        else if (userPassword.length()<6){
            isValid = false;
            Toast.makeText(this, "Your password should be at least 6 characters long", Toast.LENGTH_SHORT).show();
            mEtPassword.getText().clear();
        }

        else if(!userEmail.contains("ncf.edu")){
            isValid = false;
            Toast.makeText(this, "Make sure you entered an NCF email.", Toast.LENGTH_SHORT).show();
            mEtPassword.getText().clear();
        }

        //If the user did input an email and a password, return True for isValid.
        else{
            isValid = true;
        }

        return isValid;

    }

    /**
     *
     * @param email the email that the user inputted.
     * @param password the password that user inputted.
     */
    private void signUpTheUser(String email, String password){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");

                            //Ask the Firebase about the currently logged in user.
                            FirebaseUser user = mAuth.getCurrentUser();

                            //Welcome them with their name on a Toast message.
                            Toast.makeText(SignUpActivity.this, "Welcome! "+ "Sign up is complete," +
                                            "please log in now.", Toast.LENGTH_SHORT).show();

                            //Signup is complete, take the user back to the login activity.
                            Intent toLoginSignupActivity = new Intent(SignUpActivity.this, LoginSignupActivity.class);
                            startActivity(toLoginSignupActivity);


                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignUpActivity.this, "Signup Failed. It's our bad. Please try again...",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }
}
