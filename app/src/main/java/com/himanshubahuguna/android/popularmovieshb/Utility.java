package com.himanshubahuguna.android.popularmovieshb;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.facebook.stetho.okhttp.StethoInterceptor;
import com.himanshubahuguna.android.popularmovieshb.data.MovieContract;
import com.himanshubahuguna.android.popularmovieshb.model.AllComments;
import com.himanshubahuguna.android.popularmovieshb.model.AllTrailers;
import com.himanshubahuguna.android.popularmovieshb.model.Movie;
import com.himanshubahuguna.android.popularmovieshb.model.MovieDBApiService;
import com.himanshubahuguna.android.popularmovieshb.model.SearchResponse;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;

import java.util.ArrayList;
import java.util.List;
import com.facebook.stetho.Stetho;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

/**
 * Created by hbahuguna on 12/20/2015.
 */
public class Utility {

    public static final String LOG = "Log";
    public static final String LOG_TAG = Utility.class.getSimpleName();
    private static final int MOVIE_NOTIFICATION_ID = 1001;

    public static boolean isOneDayLater(long lastTimeStamp) {
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

        bulkInsert(context, cvList, movieListLength, MovieContract.MovieEntry.CONTENT_URI);
    }

    public static void storeCommentsList(Context context, List<AllComments.Comment> comments, int movieId) {
        ArrayList<ContentValues> contentValues = new ArrayList<ContentValues>();
        int commentsLength = comments.size();
        for (int i = 0; i < commentsLength; i++) {
            ContentValues contentValue = new ContentValues();
            AllComments.Comment comment = comments.get(i);
            contentValue.put(MovieContract.ReviewEntry.COLUMN_AUTHOR, comment.getAuthor());
            contentValue.put(MovieContract.ReviewEntry.COLUMN_CONTENT, comment.getContent());
            contentValue.put(MovieContract.ReviewEntry.COLUMN_REVIEW_ID, comment.getId());
            contentValue.put(MovieContract.ReviewEntry.COLUMN_MOVIE_ID, movieId);
            contentValues.add(contentValue);
        }

        bulkInsert(context, contentValues, commentsLength, MovieContract.ReviewEntry.CONTENT_URI);
    }

    public static void storeTrailerList(Context context, List<AllTrailers.MovieTrailer> trailers, int movieId) {
        ArrayList<ContentValues> contentValues = new ArrayList<ContentValues>();
        int trailersLength = trailers.size();
        for (int i = 0; i < trailersLength; i++) {
            ContentValues contentValue = new ContentValues();
            AllTrailers.MovieTrailer trailer = trailers.get(i);
            contentValue.put(MovieContract.TrailerEntry.COLUMN_YOUTUBE_KEY, trailer.getKey());
            contentValue.put(MovieContract.TrailerEntry.COLUMN_TRAILER_ID, trailer.getId());
            contentValue.put(MovieContract.TrailerEntry.COLUMN_MOVIE_ID, movieId);
            contentValue.put(MovieContract.TrailerEntry.COLUMN_TITLE, trailer.getTrailerTitle());
            contentValues.add(contentValue);
        }

        bulkInsert(context, contentValues, trailersLength, MovieContract.TrailerEntry.CONTENT_URI);
    }

    private static void bulkInsert(Context context, ArrayList<ContentValues> contentValues, int length, Uri uri) {
        ContentValues[] contentValuesArray = new ContentValues[contentValues.size()];
        contentValues.toArray(contentValuesArray);

        int itemsAdded = context.getContentResolver().bulkInsert(uri, contentValuesArray);
        if (itemsAdded != length) {
            Log.d(LOG, itemsAdded + "/" + length + " items inserted");
        } else {
            Log.d(LOG, itemsAdded + " records added into the DB");
        }
    }

    public static String formatReleaseDate(String unformattedReleaseDate) {
        if (unformattedReleaseDate.equals(""))
            return "N/A";
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

        int upadted = context.getContentResolver().update(
                MovieContract.MovieEntry.CONTENT_URI,
                values,
                MovieContract.MovieEntry.COLUMN_MOVIE_ID + "= ?",
                new String[]{Integer.toString(movieId)}
        );
        try {
            Thread.sleep(260);
        } catch (InterruptedException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
        return upadted;
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

    public static OkHttpClient httpClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
// set your desired log level
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient httpClient = new OkHttpClient();
// add your other interceptors …
// add logging as last interceptor
        httpClient.networkInterceptors().add(logging);
        return httpClient;
    }

    public static MovieDBApiService movieDBApiService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Config.API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient())
                .build();
        return retrofit.create(MovieDBApiService.class);
    }

    public static void sendNotification(Context context, long lastSyncTime) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean displayNotifications = prefs.getBoolean(context.getString(R.string.prefs_notification_key), true);

        if (!displayNotifications) {
            return;
        }

        String lastNotificationKey = context.getString(R.string.prefs_notification_last_key);
        lastSyncTime = prefs.getLong(lastNotificationKey, 0);

        if (isOneDayLater(lastSyncTime)) {
            //Show notification

            int smallIcon = R.mipmap.ic_launcher;
            Bitmap largeIcon = BitmapFactory.decodeResource(
                    context.getResources(),
                    R.mipmap.ic_launcher);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                    .setSmallIcon(smallIcon)
                    .setLargeIcon(largeIcon)
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText(context.getString(R.string.notification_content));

            Intent notificationIntent = new Intent(context, MainActivity.class);

            // The stack builder object will contain an artificial back stack for the
            // started Activity.
            // This ensures that navigating backward from the Activity leads out of
            // your application to the Home screen.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addNextIntent(notificationIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            builder.setContentIntent(resultPendingIntent);

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(MOVIE_NOTIFICATION_ID, builder.build()); //notify

            SharedPreferences.Editor editor = prefs.edit();
            editor.putLong(lastNotificationKey, System.currentTimeMillis());
            editor.apply();
        }

    }
}
