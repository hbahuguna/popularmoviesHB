package com.himanshubahuguna.android.popularmovieshb.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by hbahuguna on 12/21/2015.
 */
public class MovieSyncService extends Service {
    //used as thread-safe lock
    private static final Object sSyncAdapterLock = new Object();
    //"singleton" of the sync adapter
    private static MovieSyncAdapter sMovieSyncAdapter = null;

    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if (sMovieSyncAdapter == null) {
                sMovieSyncAdapter = new MovieSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return sMovieSyncAdapter.getSyncAdapterBinder();
    }

}
