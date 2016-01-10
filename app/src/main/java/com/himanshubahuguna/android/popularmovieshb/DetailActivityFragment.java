package com.himanshubahuguna.android.popularmovieshb;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.commonsware.cwac.merge.MergeAdapter;

import com.himanshubahuguna.android.popularmovieshb.data.MovieContract;
import com.himanshubahuguna.android.popularmovieshb.sync.ReviewsTrailersSyncAdapter;
import com.squareup.picasso.Picasso;

/**
 * Created by hbahuguna on 11/24/2015.
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    public static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();
    static final String DETAIL_URI = "URI";
    public static final int TRAILER_LOADER = 0;
    public static final int COMMENT_LOADER = 1;
    public static final int DETAIL_LOADER = 2;

    private Uri mUri;
    private View rootView;
    private MergeAdapter mergeAdapter = new MergeAdapter();
    private TrailersAdapter trailersAdapter;
    private CommentsAdapter commentsAdapter;
    private MovieDetailAdapter movieDetailAdapter;
    private ListView detailsListView;
    private int movieId;
    private Cursor trailerCursor;
    private Cursor commentsCursor;
    private Cursor detailsCursor;

    public DetailActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(TRAILER_LOADER, null, this);
        getLoaderManager().initLoader(COMMENT_LOADER, null, this);
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
    }

    private void updateMovie(){
        ReviewsTrailersSyncAdapter.syncImmediately(getActivity(), movieId);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        Intent intent = getActivity().getIntent();

        if (intent == null) {
            return null;
        }

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailActivityFragment.DETAIL_URI);
        }

        if (mUri == null) {
            mUri = intent.getData();
        }

        if (mUri == null) {
            return rootView;
        }

        Log.d(LOG_TAG, "mUri " + mUri);

        detailsCursor = getActivity().getContentResolver()
                .query(mUri, null, null, null, null);
        movieDetailAdapter = new MovieDetailAdapter(getActivity(), detailsCursor, 0);
        mergeAdapter.addAdapter(movieDetailAdapter);

        movieId = Utility.fetchMovieIdFromUri(getActivity(), mUri);

        trailerCursor = getActivity().getContentResolver()
                    .query(MovieContract.TrailerEntry.CONTENT_URI,
                            null,
                            MovieContract.TrailerEntry.COLUMN_MOVIE_ID + " = ?",
                            new String[]{String.valueOf(movieId)},
                            null
                    );
        trailersAdapter = new TrailersAdapter(getActivity(), trailerCursor, 0);
        mergeAdapter.addAdapter(trailersAdapter);

        commentsCursor = getActivity().getContentResolver().query(
                MovieContract.ReviewEntry.CONTENT_URI,
                null, // all columns
                MovieContract.ReviewEntry.COLUMN_MOVIE_ID + " = ?",
                new String[]{String.valueOf(movieId)},
                null);

        commentsAdapter = new CommentsAdapter(getActivity(), commentsCursor, 0);
        mergeAdapter.addAdapter(commentsAdapter);

        detailsListView = (ListView) rootView.findViewById(R.id.details_listview);
        detailsListView.setAdapter(mergeAdapter);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if(mUri != null)
            updateMovie();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = null;
        if(mUri != null) {
            if (id == TRAILER_LOADER) {
                loader = new CursorLoader(getActivity(),
                        MovieContract.TrailerEntry.CONTENT_URI,
                        null,
                        MovieContract.TrailerEntry.COLUMN_MOVIE_ID + " = ?",
                        new String[]{String.valueOf(movieId)},
                        null);
            } else if (id == COMMENT_LOADER) {
                loader = new CursorLoader(getActivity(),
                        MovieContract.ReviewEntry.CONTENT_URI,
                        null,
                        MovieContract.ReviewEntry.COLUMN_MOVIE_ID + " = ?",
                        new String[]{String.valueOf(movieId)},
                        null);
            } else if (id == DETAIL_LOADER) {
                loader = new CursorLoader(getActivity(),
                        mUri,
                        null,
                        null,
                        null,
                        null);
            }
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if(mUri != null) {
            switch (loader.getId()) {
                case TRAILER_LOADER:
                    trailersAdapter.swapCursor(cursor);
                    break;
                case COMMENT_LOADER:
                    commentsAdapter.swapCursor(cursor);
                    break;
                case DETAIL_LOADER:
                    movieDetailAdapter.swapCursor(cursor);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if(mUri != null) {
            switch (loader.getId()) {
                case TRAILER_LOADER:
                    trailersAdapter.swapCursor(null);
                    break;
                case COMMENT_LOADER:
                    commentsAdapter.swapCursor(null);
                    break;
                case DETAIL_LOADER:
                    movieDetailAdapter.swapCursor(null);
                    break;
                default:
                    break;
            }
        }
    }
}
