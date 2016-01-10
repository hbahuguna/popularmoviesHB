package com.himanshubahuguna.android.popularmovieshb.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by hbahuguna on 1/10/2016.
 */
public class ReviewsTrailersSyncService extends Service {
    //used as thread-safe lock
    private static final Object sSyncAdapterLock = new Object();
    //"singleton" of the sync adapter
    private static ReviewsTrailersSyncAdapter sReviewSyncAdapter = null;

    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if (sReviewSyncAdapter == null) {
                sReviewSyncAdapter = new ReviewsTrailersSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return sReviewSyncAdapter.getSyncAdapterBinder();
    }

}
