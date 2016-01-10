package com.himanshubahuguna.android.popularmovieshb;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.himanshubahuguna.android.popularmovieshb.data.MovieContract;
import com.squareup.picasso.Picasso;

/**
 * Created by hbahuguna on 1/9/2016.
 */
public class MovieDetailAdapter extends CursorAdapter {
    public static final String LOG_TAG = MovieDetailAdapter.class.getSimpleName();

    public MovieDetailAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
    }
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item_movie_details, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        TextView detailsReleaseYear = (TextView) view.findViewById(R.id.movie_year);
        ImageView detailsPoster = (ImageView) view.findViewById(R.id.details_movie_poster);
        TextView detailsRating = (TextView) view.findViewById(R.id.movie_rating);
        TextView detailsRuntime = (TextView) view.findViewById(R.id.movie_length);
        final TextView detailsOverview = (TextView) view.findViewById(R.id.movie_description);
        Button markAsFavoriteBtn = (Button) view.findViewById(R.id.favorite_btn);

        final int _ID = cursor.getInt(cursor.getColumnIndex(MovieContract.MovieEntry._ID));
        String title = cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_TITLE));
        String poster = cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER_PATH));
        double rating = cursor.getDouble(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE));
        String releaseDate = cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_RELEASE_DATE));
        int totalVotes = cursor.getInt(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_VOTE_COUNT));
        int movieRuntime = cursor.getInt(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_RUNTIME));
        String overview = cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_OVERVIEW));
        final int IS_FAVORITE = cursor.getInt(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_FAVORITE));
        Activity activity = (Activity) context;
        activity.setTitle(title);

        if (IS_FAVORITE == 1) {
            String unmark = context.getString(R.string.details_button_favorite_remove);
            markAsFavoriteBtn.setText(unmark);
        } else {
            String mark = context.getString(R.string.details_button_favorite_add);
            markAsFavoriteBtn.setText(mark);
        }

        Uri posterUri = Uri.parse(Config.IMAGE_BASE_URL).buildUpon()
                .appendPath(context.getString(R.string.api_image_size_default))
                .appendPath(poster.substring(1)) //remove the heading slash
                .build();

        Picasso.with(context).load(posterUri)
                .placeholder(R.drawable.loading)
                .into(detailsPoster);

        detailsReleaseYear.setText(Utility.getReleaseYear(releaseDate));
        detailsRuntime.setText(movieRuntime + "min");

        detailsOverview.setText(overview);

        detailsRating.setText(
                String.format(context.getString(R.string.format_ratings), rating, totalVotes));


        markAsFavoriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (IS_FAVORITE) {
                    case 0: {
                        // movie is not favorited
                        // mark it
                        ContentValues addFavorite = new ContentValues();
                        addFavorite.put(MovieContract.MovieEntry.COLUMN_FAVORITE, 1); //mark as favorite

                        int updatedRows = context.getContentResolver().update(
                                MovieContract.MovieEntry.CONTENT_URI,
                                addFavorite,
                                MovieContract.MovieEntry._ID + " = ?",
                                new String[]{String.valueOf(_ID)}
                        );

                        if (updatedRows <= 0) {
                            Log.d(LOG_TAG, "Movie not marked as favorite");
                        } else {
                            Log.d(LOG_TAG, "Movie marked as favorite");
                        }
                    }
                    break;

                    case 1: {
                        // movie is favorited
                        // unmark it
                        ContentValues removeFavorite = new ContentValues();
                        removeFavorite.put(MovieContract.MovieEntry.COLUMN_FAVORITE, 0); //unmark as favorite

                        int updatedRows = context.getContentResolver().update(
                                MovieContract.MovieEntry.CONTENT_URI,
                                removeFavorite,
                                MovieContract.MovieEntry._ID + " = ?",
                                new String[]{String.valueOf(_ID)}
                        );

                        if (updatedRows < 0) {
                            Log.d(LOG_TAG, "Movie not unmarked as favorite");
                        } else {
                            Log.d(LOG_TAG, "Movie unmarked as favorite");
                        }
                    }
                    break;

                    default:
                        Log.e(LOG_TAG, "What is this?!");

                }
            }
        });

    }
}
