package com.himanshubahuguna.android.popularmovieshb;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.commonsware.cwac.merge.MergeAdapter;

import com.himanshubahuguna.android.popularmovieshb.data.MovieContract;
import com.himanshubahuguna.android.popularmovieshb.model.AllComments;
import com.himanshubahuguna.android.popularmovieshb.model.AllTrailers;
import com.himanshubahuguna.android.popularmovieshb.model.MovieDBApiService;
import com.himanshubahuguna.android.popularmovieshb.model.MovieRuntime;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by hbahuguna on 11/24/2015.
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    public static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();
    static final String DETAIL_URI = "URI";
    public static final int TRAILER_LOADER = 0;
    public static final int COMMENT_LOADER = 1;
    public static final int RUNTIME_LOADER = 2;
    private Uri mUri;
    View rootView;
    MergeAdapter mergeAdapter = new MergeAdapter();
    TrailersAdapter trailersAdapter;
    CommentsAdapter commentsAdapter;
    ListView detailsListView;
    int movieId;
    Cursor trailerCursor;
    Cursor commentsCursor;

    public DetailActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(TRAILER_LOADER, null, this);
        getLoaderManager().initLoader(COMMENT_LOADER, null, this);
        getLoaderManager().initLoader(RUNTIME_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
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
                Log.d(LOG_TAG, "mUri " + mUri);
            }

            if (mUri == null) {
                mUri = intent.getData();
            }

            if (mUri == null) {
                return rootView;
            }

            Cursor detailsCursor = getActivity().getContentResolver()
                .query(mUri, null, null, null, null);

            View detailsView = populateDetailsView(detailsCursor);
            mergeAdapter.addView(detailsView);
            movieId = Utility.fetchMovieIdFromUri(getActivity(), mUri);

            final MovieDBApiService movieDBApiService = Utility.movieDBApiService();

            movieDBApiService.getMovieRuntime(movieId).enqueue(new Callback<MovieRuntime>() {
                @Override
                public void onResponse(Response<MovieRuntime> runtime, Retrofit retrofit) {
                    Utility.updateMovieWithRuntime(getContext(), movieId, runtime.body().getRuntime());
                }

                @Override
                public void onFailure(Throwable t) {
                    Log.e("SyncAdapter", "Error updatiing movie runtime: " + t.getMessage());
                }
            });

            movieDBApiService.getMovieTrailers(movieId).enqueue(new Callback<AllTrailers>() {
                @Override
                public void onResponse(Response<AllTrailers> response, Retrofit retrofit) {
                    List<AllTrailers.MovieTrailer> trailerList = response.body().getTrailerList();
                    Utility.storeTrailerList(getContext(), trailerList, movieId);
                }

                @Override
                public void onFailure(Throwable t) {
                    Log.e("SyncAdapter", "Error inserting trailers: " + t.getMessage());
                }
            });

            trailerCursor = getActivity().getContentResolver()
                .query(MovieContract.TrailerEntry.CONTENT_URI,
                        null,
                        MovieContract.TrailerEntry.COLUMN_MOVIE_ID + " = ?",
                        new String[]{String.valueOf(movieId)},
                        null
                );
        trailersAdapter = new TrailersAdapter(getActivity(), trailerCursor, 0);
        mergeAdapter.addAdapter(trailersAdapter);

        movieDBApiService.getMovieReviews(movieId).enqueue(new Callback<AllComments>() {
            @Override
            public void onResponse(Response<AllComments> response, Retrofit retrofit) {
                List<AllComments.Comment> commentList = response.body().getCommentList();
                Utility.storeCommentsList(getContext(), commentList, movieId);
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e("SyncAdapter", "Error inserting comments: " + t.getMessage());
            }
        });

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


    private View populateDetailsView(Cursor cursor) {
        if (!cursor.moveToFirst()) {
            return null;
        }

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.list_item_movie_details, null, false);

        if (view == null) {
            return null;
        }

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

        if (IS_FAVORITE == 1) {
            String unmark = getActivity().getString(R.string.details_button_favorite_remove);
            markAsFavoriteBtn.setText(unmark);
        } else {
            String mark = getActivity().getString(R.string.details_button_favorite_add);
            markAsFavoriteBtn.setText(mark);
        }

        Uri posterUri = Uri.parse(Config.IMAGE_BASE_URL).buildUpon()
                .appendPath(getActivity().getString(R.string.api_image_size_default))
                .appendPath(poster.substring(1)) //remove the heading slash
                .build();

        Picasso.with(getActivity()).load(posterUri)
                .placeholder(R.drawable.loading)
                .into(detailsPoster);

        detailsReleaseYear.setText(Utility.getReleaseYear(releaseDate));
        detailsRuntime.setText(movieRuntime + "min");

        detailsOverview.setText(overview);
        getActivity().setTitle(title);

        detailsRating.setText(
                String.format(getActivity().getString(R.string.format_ratings), rating, totalVotes));

        markAsFavoriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (IS_FAVORITE) {
                    case 0: {
                        // movie is not favorited
                        // mark it
                        ContentValues addFavorite = new ContentValues();
                        addFavorite.put(MovieContract.MovieEntry.COLUMN_FAVORITE, 1); //mark as favorite

                        int updatedRows = getActivity().getContentResolver().update(
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

                        int updatedRows = getActivity().getContentResolver().update(
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

        return view;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = null;
        if (id == TRAILER_LOADER) {
            loader = new CursorLoader(getActivity(),
                    MovieContract.TrailerEntry.CONTENT_URI,
                    null,
                    MovieContract.TrailerEntry.COLUMN_MOVIE_ID + " = ?",
                    new String[]{String.valueOf(movieId)},
                    null);
        } else if(id == COMMENT_LOADER) {
            loader = new CursorLoader(getActivity(),
                    MovieContract.ReviewEntry.CONTENT_URI,
                    null,
                    MovieContract.ReviewEntry.COLUMN_MOVIE_ID + " = ?",
                    new String[]{String.valueOf(movieId)},
                    null);
        } else if(id == RUNTIME_LOADER) {
            loader = new CursorLoader(getActivity(),
                    MovieContract.MovieEntry.CONTENT_URI,
                    new String[] {MovieContract.MovieEntry.COLUMN_RUNTIME},
                    MovieContract.ReviewEntry.COLUMN_MOVIE_ID + " = ?",
                    new String[]{String.valueOf(movieId)},
                    null);
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            switch (loader.getId()) {
                case TRAILER_LOADER:
                    trailersAdapter = new TrailersAdapter(getActivity(), trailerCursor, 0);
                    trailersAdapter.swapCursor(cursor);
                    mergeAdapter.addAdapter(trailersAdapter);
                    break;
                case COMMENT_LOADER:
                    commentsAdapter = new CommentsAdapter(getActivity(), commentsCursor, 0);
                    commentsAdapter.swapCursor(cursor);
                    mergeAdapter.addAdapter(commentsAdapter);
                    break;
                default:
                    break;
            }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case TRAILER_LOADER:
                trailersAdapter.swapCursor(null);
                break;
            case COMMENT_LOADER:
                commentsAdapter.swapCursor(null);
                break;
            default:
                break;
        }
    }

}
