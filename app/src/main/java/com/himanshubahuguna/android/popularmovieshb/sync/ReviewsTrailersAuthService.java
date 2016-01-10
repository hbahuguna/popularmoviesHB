package com.himanshubahuguna.android.popularmovieshb.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by hbahuguna on 1/10/2016.
 */
public class ReviewsTrailersAuthService extends Service {
    private ReviewsTrailersAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        mAuthenticator = new ReviewsTrailersAuthenticator(getApplicationContext());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }

}
