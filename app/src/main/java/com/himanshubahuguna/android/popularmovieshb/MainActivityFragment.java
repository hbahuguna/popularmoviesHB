package com.himanshubahuguna.android.popularmovieshb;

import android.content.Intent;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.himanshubahuguna.android.popularmovieshb.model.Movie;
import com.himanshubahuguna.android.popularmovieshb.model.MovieDBApiService;
import com.himanshubahuguna.android.popularmovieshb.model.Result;
import com.himanshubahuguna.android.popularmovieshb.model.SearchResponse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private ArrayList result = new ArrayList<>();

    public MainActivityFragment() {
    }

    public static final int MAX_PAGES = 50;
    private boolean mIsLoading = false;
    private int mPagesLoaded = 0;
    // private TextView mProgress;
    private MovieAdapter mImages;
    private ProgressBar mProgress;

    private class FetchPageTask extends AsyncTask<Integer, Void, Collection<Movie>> {

        public  final String LOG_TAG = FetchPageTask.class.getSimpleName();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgress = new ProgressBar(getActivity());
            mProgress.findViewById(R.id.progress_bar);
        }

        @Override
        protected Collection<Movie> doInBackground(Integer... params) {
            if (params.length == 0) {
                return null;
            }

            int page = params[0];
            final String API_BASE_URL = "http://api.themoviedb.org/3/movie/";
            final String API_PARAM_PAGE = "page";
            final String API_PARAM_KEY = "api_key";
            final String API_SORTING = PreferenceManager
                    .getDefaultSharedPreferences(getActivity())
                    .getString(
                            getString(R.string.pref_sorting_key),
                            getString(R.string.pref_sorting_default_value)
                    );

            final Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd")
                    .create();
            final Map<String,Object> queryParams = new HashMap<String,Object>();
            queryParams.put(API_PARAM_KEY, R.string.api_key);
            queryParams.put(API_PARAM_PAGE, page);
            final Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(API_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
            final MovieDBApiService movieDBApiService = retrofit.create(MovieDBApiService.class);

            retrofit.Call<SearchResponse> movies = movieDBApiService
                    .listMovies(API_SORTING, getString(R.string.api_key), page);
            movies.enqueue(new Callback<SearchResponse>() {
                @Override
                public void onResponse(Response<SearchResponse> response, Retrofit retrofit) {
                    SearchResponse searchResponse = response.body();
                    for (Result movieResult : searchResponse.getResults()) {
                        Movie movie = new Movie(movieResult.getId(),
                                movieResult.getOriginalTitle(),
                                movieResult.getOverview(),
                                movieResult.getPosterPath(),
                                movieResult.getVoteAverage(),
                                movieResult.getVoteCount(),
                                movieResult.getReleaseDate()
                        );
                        result.add(movie);
                    }
                }

                @Override
                public void onFailure(Throwable t) {

                }
            });

            return result;
        }

        @Override
        protected void onPostExecute(Collection<Movie> xs) {
            if (xs == null) {
                Toast.makeText(
                        getActivity(),
                        getString(R.string.msg_server_error),
                        Toast.LENGTH_SHORT
                ).show();

                stopLoading();
                return;
            }

            mPagesLoaded++;

            stopLoading();

            mImages.addAll(xs);
        }

    }

    private void startLoading() {
        if (mIsLoading) {
            return;
        }

        if (mPagesLoaded >= MAX_PAGES) {
            return;
        }

        mIsLoading = true;

        if (mProgress != null) {
            mProgress.setVisibility(View.VISIBLE);
        }

        new FetchPageTask().execute(mPagesLoaded + 1);
    }

    private void stopLoading() {
        if (!mIsLoading) {
            return;
        }

        mIsLoading = false;

        if (mProgress != null) {
            mProgress.setVisibility(View.GONE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        mImages = new MovieAdapter(getActivity());

        initGrid(view);

        return view;
    }

    private void initGrid(View view) {
        GridView gridview = (GridView) view.findViewById(R.id.grid_view);

        if (gridview == null) {
            return;
        }
        gridview.setAdapter(mImages);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent,
                                    View v,
                                    int position,
                                    long id) {

                MovieAdapter adapter = (MovieAdapter) parent.getAdapter();
                Movie movie = adapter.getItem(position);

                if (movie == null) {
                    return;
                }

                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra(Movie.EXTRA_MOVIE, movie.toBundle());
                getActivity().startActivity(intent);
            }
        });


        gridview.setOnScrollListener(

                new AbsListView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(AbsListView view, int scrollState) {

                    }

                    @Override
                    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                        int lastInScreen = firstVisibleItem + visibleItemCount;
                        if (lastInScreen == totalItemCount) {
                            startLoading();
                        }
                    }
                }

        );
    }

    @Override
    public void onResume() {
        super.onResume();

        startLoading();

    }



}
