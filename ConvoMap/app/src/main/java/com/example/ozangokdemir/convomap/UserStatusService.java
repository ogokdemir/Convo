package com.example.ozangokdemir.convomap;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

// TODO: 3/23/2019 Now I'm working on putting the user status check to a background service.


public class UserStatusService extends Service {
    public UserStatusService() {
    }

    @Override
    public IBinder onBind(Intent intent) {return null;}


}
