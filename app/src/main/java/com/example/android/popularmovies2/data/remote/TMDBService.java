package com.example.android.popularmovies2.data.remote;

import com.example.android.popularmovies2.BuildConfig;
import com.example.android.popularmovies2.data.model.TMDBMoviesResponse;
import com.example.android.popularmovies2.data.model.TMDBMovieReviewsResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by mika on 2018-04-16.
 */

public interface TMDBService {
    String API_KEY = BuildConfig.THEMOVIEDB_API_KEY;

    @GET("movie/{sort_order}?api_key=" + API_KEY)
    Call<TMDBMoviesResponse> getMovies(@Path("sort_order") String sortOrder);

    @GET("movie/{movie_id}/reviews?api_key=" + API_KEY)
    Call<TMDBMovieReviewsResponse> getReviews(@Path("movie_id") int movieId);

}