package com.himanshubahuguna.android.popularmovieshb;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.himanshubahuguna.android.popularmovieshb.data.MovieContract;
import com.squareup.picasso.Picasso;

/**
 * Created by hbahuguna on 12/27/2015.
 */
public class TrailersAdapter extends CursorAdapter {

    public static final String LOG_TAG = TrailersAdapter.class.getSimpleName();

    public TrailersAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
    }
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.trailer_list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        TextView trailerTitleTextView = (TextView) view.findViewById(R.id.list_item_trailer_title);
        int trailerTitleColumn = cursor.getColumnIndex(MovieContract.TrailerEntry.COLUMN_TITLE);
        String trailerTitle = cursor.getString(trailerTitleColumn);
        trailerTitleTextView.setText(trailerTitle);
        Log.d(LOG_TAG, "#########" + trailerTitle);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int youtubeKeyColumn = cursor.getColumnIndex(MovieContract.TrailerEntry.COLUMN_YOUTUBE_KEY);
                String youtubeKey = cursor.getString(youtubeKeyColumn);
                Uri videoUri = Uri.parse(Config.YOUTUBE_TRAILER_URL + youtubeKey);

                Intent playTrailer = new Intent(Intent.ACTION_VIEW, videoUri);
                context.startActivity(playTrailer);
            }
        });

    }
}
