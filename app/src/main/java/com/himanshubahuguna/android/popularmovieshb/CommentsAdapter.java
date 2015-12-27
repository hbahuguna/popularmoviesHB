package com.himanshubahuguna.android.popularmovieshb;

import android.content.Context;
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
public class CommentsAdapter extends CursorAdapter {

    public static final String LOG_TAG = CommentsAdapter.class.getSimpleName();

    public CommentsAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.comment_list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView authorTextView = (TextView) view.findViewById(R.id.comment_author);
        TextView contentTextView = (TextView) view.findViewById(R.id.comment_content);

        int authorColumnIndex = cursor.getColumnIndex(MovieContract.ReviewEntry.COLUMN_AUTHOR);
        int contentColumnIndex = cursor.getColumnIndex(MovieContract.ReviewEntry.COLUMN_CONTENT);

        String author = cursor.getString(authorColumnIndex);
        String content = cursor.getString(contentColumnIndex);

        authorTextView.setText(author);
        contentTextView.setText(content);
    }

}
