package com.example.android.popularmovies2.data.remote;

import com.example.android.popularmovies2.BuildConfig;
import com.example.android.popularmovies2.data.model.TMDBResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by mika on 2018-04-16.
 */

public interface TMDBService {
    String API_KEY = BuildConfig.THEMOVIEDB_API_KEY;

    @GET("movie/{sort_order}?api_key=" + API_KEY)
    Call<TMDBResponse> getMovies(@Path("sort_order") String sortOrder);

}