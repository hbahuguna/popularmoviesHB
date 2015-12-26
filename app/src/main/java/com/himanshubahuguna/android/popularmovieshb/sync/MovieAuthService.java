package com.himanshubahuguna.android.popularmovieshb.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by hbahuguna on 12/21/2015.
 */
public class MovieAuthService extends Service {
    private MovieAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        mAuthenticator = new MovieAuthenticator(getApplicationContext());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }

}
