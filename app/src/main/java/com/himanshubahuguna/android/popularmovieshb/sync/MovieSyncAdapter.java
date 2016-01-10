package com.himanshubahuguna.android.popularmovieshb.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.UiAutomation;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.AbstractThreadedSyncAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.himanshubahuguna.android.popularmovieshb.Config;
import com.himanshubahuguna.android.popularmovieshb.MainActivity;
import com.himanshubahuguna.android.popularmovieshb.R;
import com.himanshubahuguna.android.popularmovieshb.Utility;
import com.himanshubahuguna.android.popularmovieshb.model.AllComments;
import com.himanshubahuguna.android.popularmovieshb.model.AllTrailers;
import com.himanshubahuguna.android.popularmovieshb.model.MovieDBApiService;
import com.himanshubahuguna.android.popularmovieshb.model.MovieRuntime;
import com.himanshubahuguna.android.popularmovieshb.model.SearchResponse;

import java.util.List;
import java.util.concurrent.Executors;

import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;


/**
 * Created by hbahuguna on 12/18/2015.
 */
public class MovieSyncAdapter extends AbstractThreadedSyncAdapter {

    public static final String LOG_TAG = MovieSyncAdapter.class.getSimpleName();

    public static final int SYNC_INTERVAL = 60 * 60 * 10; // 10 hours
    private static long lastSyncTime = 0L;

    public MovieSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    public MovieSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
    }

    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(
                getAccount(context),
                context.getString(R.string.content_authority),
                bundle);
    }

    private static Account getAccount(Context context) {
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        Account newAccount = new Account(
                context.getString(R.string.app_name),
                "com.foo");

        if (accountManager.getPassword(newAccount) == null) {
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }

            // schedule the sync adapter
            ContentResolver.addPeriodicSync(newAccount,
                    context.getString(R.string.content_authority),
                    Bundle.EMPTY,
                    SYNC_INTERVAL);

            ContentResolver.setSyncAutomatically(newAccount,
                    context.getString(R.string.content_authority),
                    true);

            syncImmediately(context);
        }

        return newAccount;
    }


    public static void initSyncAdapter(Context context) {
        getAccount(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult result) {
        if (Utility.isOneDayLater(lastSyncTime)) {
            String sortOrder = Utility.getPreferredSortOrder(getContext());
            final MovieDBApiService movieDBApiService = Utility.movieDBApiService();
            retrofit.Call<SearchResponse> movies = movieDBApiService
                    .getTopMovies(sortOrder);
            movies.enqueue(new Callback<SearchResponse>() {
                @Override
                public void onResponse(Response<SearchResponse> response, Retrofit retrofit) {
                    List<SearchResponse.MovieModel> movieList = response.body().getMovieList();
                    Utility.storeMovieList(getContext(), movieList);
                    Utility.sendNotification(getContext(), lastSyncTime);
                }

                @Override
                public void onFailure(Throwable t) {
                    Log.e("SyncAdapter", "Error: " + t.getMessage());
                }
            });
        }
    }

}

