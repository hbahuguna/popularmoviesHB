package com.himanshubahuguna.android.popularmovieshb.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.himanshubahuguna.android.popularmovieshb.R;
import com.himanshubahuguna.android.popularmovieshb.Utility;
import com.himanshubahuguna.android.popularmovieshb.model.AllComments;
import com.himanshubahuguna.android.popularmovieshb.model.AllTrailers;
import com.himanshubahuguna.android.popularmovieshb.model.MovieDBApiService;
import com.himanshubahuguna.android.popularmovieshb.model.MovieRuntime;

import java.util.List;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by hbahuguna on 1/9/2016.
 */
public class ReviewsTrailersSyncAdapter extends AbstractThreadedSyncAdapter {
    public static final String LOG_TAG = ReviewsTrailersSyncAdapter.class.getSimpleName();

    public static final int SYNC_INTERVAL = 60 * 60 * 10; // 10 hours
    private static long lastSyncTime = 0L;

    public ReviewsTrailersSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    public ReviewsTrailersSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
    }

    public static void syncImmediately(Context context, int movieId) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        bundle.putInt("movieId", movieId);
        ContentResolver.requestSync(
                getAccount(context, movieId),
                context.getString(R.string.content_authority),
                bundle);
    }

    private static Account getAccount(Context context, int movieId) {
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        Account newAccount = new Account(
                context.getString(R.string.app_name),
                "com.bar");

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

            syncImmediately(context, movieId);
        }

        return newAccount;
    }


    public static void initSyncAdapter(Context context, int movieId) {
        getAccount(context, movieId);
    }

    @Override
    public void onPerformSync(Account account, final Bundle extras, String authority, ContentProviderClient provider, SyncResult result) {
        if (extras.getInt("movieId") != 0   ) {
            final MovieDBApiService movieDBApiService = Utility.movieDBApiService();
            movieDBApiService.getMovieReviews(extras.getInt("movieId")).enqueue(new Callback<AllComments>() {
                @Override
                public void onResponse(Response<AllComments> response, Retrofit retrofit) {
                    List<AllComments.Comment> commentList = response.body().getCommentList();
                    Utility.storeCommentsList(getContext(), commentList, extras.getInt("movieId"));
                }

                @Override
                public void onFailure(Throwable t) {
                    Log.e("SyncAdapter", "Error inserting trailers: " + t.getMessage());
                }
            });
            movieDBApiService.getMovieTrailers(extras.getInt("movieId")).enqueue(new Callback<AllTrailers>() {
                @Override
                public void onResponse(Response<AllTrailers> response, Retrofit retrofit) {
                    List<AllTrailers.MovieTrailer> trailers = response.body().getTrailerList();
                    Utility.storeTrailerList(getContext(), trailers, extras.getInt("movieId"));
                }

                @Override
                public void onFailure(Throwable t) {
                    Log.e("SyncAdapter", "Error inserting comments: " + t.getMessage());
                }
            });
            movieDBApiService.getMovieRuntime(extras.getInt("movieId")).enqueue(new Callback<MovieRuntime>() {
                @Override
                public void onResponse(Response<MovieRuntime> runtime, Retrofit retrofit) {
                    Utility.updateMovieWithRuntime(getContext(), extras.getInt("movieId"), runtime.body().getRuntime());
                }

                @Override
                public void onFailure(Throwable t) {
                    Log.e("SyncAdapter", "Error updating movie runtime: " + t.getMessage());
                }
            });
            Utility.sendNotification(getContext(), lastSyncTime);
        }
    }

}
