package com.example.ozangokdemir.convomap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.UUID;

public class SurveysActivity extends AppCompatActivity implements View.OnClickListener{

    TextView twWelcome, twUserId;
    Button btnLoneliness, btnHappiness, btnPostConversation, btnPostStudy;
    String usersName; //will get this from the display activity and use it for greeting the user.
    private static final String USER_ID_CACHE_FILE = "participant_id_cache"; //key for shared prefs directory.
    private static final String USER_ID_RETRIEVE_KEY = "participants_id"; // key for retrieving the participants id.
    SharedPreferences.Editor editor;
    SharedPreferences prefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surveys);

        initWidgets();
        setOnClickListeners();

        Intent intent = getIntent();
        usersName = intent.getStringExtra("user_name");

        twWelcome.setText("Welcome "+ usersName+"!");
        twUserId.setText("Your unique participant ID is: "+ getParticipantId());

    }

    //Inflates the widgets from the xml layout file.
    private void initWidgets(){
        twWelcome = (TextView) findViewById(R.id.tw_welcome);
        twUserId = (TextView) findViewById(R.id.tw_user_id);
        btnLoneliness = (Button) findViewById(R.id.btn_loneliness);
        btnHappiness = (Button) findViewById(R.id.btn_happiness);
        btnPostConversation = (Button) findViewById(R.id.btn_post_conversation);
        btnPostStudy = (Button) findViewById(R.id.btn_post_study);
    }

    //Sets onclicklisteners to the buttons in the activity.
    private void setOnClickListeners(){
        btnPostStudy.setOnClickListener(this);
        btnPostConversation.setOnClickListener(this);
        btnLoneliness.setOnClickListener(this);
        btnHappiness.setOnClickListener(this);
    }



    /**
     *Helper method that returns a unique id for each installation of the app.
     *Users will put this id number in their survey responses so that I can track their responses
     *before and after the experiment without them having to disclose their name.
     */

    private String getParticipantId(){

        prefs = getSharedPreferences(USER_ID_CACHE_FILE, MODE_PRIVATE);
        editor = prefs.edit();

        //if the unique participant id has not been generated and cached yet,
        if(prefs.getString(USER_ID_RETRIEVE_KEY, null) == null){
            String participant_id = UUID.randomUUID().toString().substring(0,4); //generate the id
            editor.putString(USER_ID_RETRIEVE_KEY, participant_id);
            editor.apply();
            return participant_id;
        }
        //if the participant's unique id has been generated before,
        else{
            return prefs.getString(USER_ID_RETRIEVE_KEY, null);

        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_happiness:
                startSurvey(getString(R.string.uri_happiness));
                break;
            case R.id.btn_loneliness:
                startSurvey(getString(R.string.uri_loneliness_survey));
                break;
            case R.id.btn_post_conversation:
                startSurvey(getString(R.string.uri_post_conversation));
                break;
            case R.id.btn_post_study:
                startSurvey(getString(R.string.uri_post_study));
                break;

        }
    }

    private void startSurvey(String uriString){
        Uri uri = Uri.parse(uriString); // the url for the post-conversation survey.
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }
}
