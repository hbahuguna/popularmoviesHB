package com.himanshubahuguna.android.popularmovieshb.model;

import com.himanshubahuguna.android.popularmovieshb.Config;
import com.himanshubahuguna.android.popularmovieshb.R;

import java.util.List;
import java.util.Map;

import javax.security.auth.callback.Callback;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.http.QueryMap;


/**
 * Created by hbahuguna on 11/26/2015.
 */
public interface MovieDBApiService {

    @GET("discover/movie?api_key=" + Config.API_KEY)
    Call<SearchResponse> getTopMovies(@Query("sort_by") String sortOrder);

    @GET("movie/{id}?api_key=" + Config.API_KEY)
    Call<MovieRuntime>  getMovieRuntime(@Path("id") int id);

}
