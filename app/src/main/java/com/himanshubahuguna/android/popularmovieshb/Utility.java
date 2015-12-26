package com.himanshubahuguna.android.popularmovieshb;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import com.himanshubahuguna.android.popularmovieshb.data.MovieContract;
import com.himanshubahuguna.android.popularmovieshb.model.Movie;
import com.himanshubahuguna.android.popularmovieshb.model.Result;
import com.himanshubahuguna.android.popularmovieshb.model.SearchResponse;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hbahuguna on 12/20/2015.
 */
public class Utility {

    public static final String LOG = "Log";

    public static boolean isOneDayLater(long lastTimeStamp){
        final long ONE_DAY = 24 * 60 * 60 * 1000;
        long now = System.currentTimeMillis();
        return (now - lastTimeStamp > ONE_DAY);
    }

    public static String getPreferredSortOrder(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(
                context.getString(R.string.pref_sorting_key),
                context.getString(R.string.pref_sorting_default_value)
        );
    }

    public static void storeMovieList(Context context, List<SearchResponse.MovieModel> movies) {
        ArrayList<ContentValues> cvList = new ArrayList<>();
        int movieListLength = movies.size();
        for (int i = 0; i < movieListLength; i++) {
            SearchResponse.MovieModel movie = movies.get(i);
            ContentValues cValues = new ContentValues();

            cValues.put(MovieContract.MovieEntry.COLUMN_TITLE, movie.getTitle());
            cValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, movie.getMovieId());
            cValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, movie.getDescription());
            cValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, movie.getPosterPath());
            cValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, formatReleaseDate(movie.getReleaseDate()));
            cValues.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, movie.getRating());
            cValues.put(MovieContract.MovieEntry.COLUMN_VOTE_COUNT, movie.getVoteCount());
            cValues.put(MovieContract.MovieEntry.COLUMN_POPULARITY, movie.getPopularity());
            cvList.add(cValues);
        }

        ContentValues[] contentValues = new ContentValues[cvList.size()];
        cvList.toArray(contentValues);
        int itemsAdded = context.getContentResolver().bulkInsert(MovieContract.MovieEntry.CONTENT_URI, contentValues);

        if (itemsAdded != movieListLength) {
            Log.d(LOG, itemsAdded + "/" + movieListLength + " movies inserted");
        } else {
            Log.d(LOG, itemsAdded + " records added into the DB");
        }

    }

    public static String formatReleaseDate(String unformattedReleaseDate) {
        StringBuilder sb = new StringBuilder();
        String[] dateContents = unformattedReleaseDate.split("-");
        sb.append(dateContents[2])
                .append("/").append(dateContents[1])
                .append("/").append(dateContents[0]);

        return sb.toString();
    }

    public static int updateMovieWithRuntime(Context context, int movieId, int runtime) {
        ContentValues values = new ContentValues();
        values.put(MovieContract.MovieEntry.COLUMN_RUNTIME, runtime);

        return context.getContentResolver().update(
                MovieContract.MovieEntry.CONTENT_URI,
                values,
                MovieContract.MovieEntry.COLUMN_MOVIE_ID + "= ?",
                new String[]{Integer.toString(movieId)}
        );
    }

    public static int fetchMovieIdFromUri(Context context, Uri movieUri) {
        long _id = MovieContract.MovieEntry.getIdFromUri(movieUri);

        Cursor c = context.getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,
                new String[]{MovieContract.MovieEntry._ID, MovieContract.MovieEntry.COLUMN_MOVIE_ID},
                MovieContract.MovieEntry._ID + " = ?",
                new String[]{String.valueOf(_id)},
                null);

        if (c.moveToFirst()) {
            int movieIdIndex = c.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_ID);
            return c.getInt(movieIdIndex);
        } else {
            return -1;
        }
    }

    public static String getReleaseYear(String releaseDate) {
        String[] explodedDate = releaseDate.split("/");
        return explodedDate[2];
    }

    public static String releaseDateFormatter(String unformattedDate) {
        StringBuilder sb = new StringBuilder();
        String[] explodedDate = unformattedDate.split("-");

        sb.append(explodedDate[2])                      //day of month
                .append("/").append(explodedDate[1])    //month
                .append("/").append(explodedDate[0]);   //year

        return sb.toString();
    }

    public static OkHttpClient httpClient () {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
// set your desired log level
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient httpClient = new OkHttpClient();
// add your other interceptors …
// add logging as last interceptor
        httpClient.interceptors().add(logging);
        return httpClient;
    }
}
