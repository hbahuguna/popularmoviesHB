package com.himanshubahuguna.android.popularmovieshb.model;

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
    @GET("{sorting}")
    Call<SearchResponse> listMovies(@Path("sorting") String sorting,
                                    @Query("api_key") String apiKey,
                                    @Query("page") int page);
}
