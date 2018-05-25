package com.example.android.popularmovies2;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.android.popularmovies2.data.MovieContract;
import com.example.android.popularmovies2.data.model.Movie;
import com.example.android.popularmovies2.data.model.MovieReview;
import com.example.android.popularmovies2.data.model.MovieVideo;
import com.example.android.popularmovies2.data.model.TMDBMovieReviewsResponse;
import com.example.android.popularmovies2.data.model.TMDBMovieVideosResponse;
import com.example.android.popularmovies2.data.remote.TMDBService;
import com.example.android.popularmovies2.databinding.ActivityDetailBinding;
import com.example.android.popularmovies2.utils.ApiUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailActivity extends AppCompatActivity implements MovieVideoAdapter.MovieVideoAdapterOnClickHandler {

    private static final String TAG = DetailActivity.class.getSimpleName();
    private static final String MOVIE_VIDEOS_RESULTS = "movie-videos-results";
    private static final String MOVIE_REVIEWS_RESULTS = "movie-reviews-results";
    ActivityDetailBinding mDetailBinding;
    private Movie mMovie;
    private ArrayList<MovieVideo> mVideos;
    private ArrayList<MovieReview> mReviews;
    private TMDBService mService;
    private MovieVideoAdapter mMovieVideoAdapter;
    private MovieReviewAdapter mMovieReviewAdapter;
    private boolean mIsFavorite;
    private Toast mToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        mDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail);

        mDetailBinding.detailToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        setSupportActionBar(mDetailBinding.detailToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mService = ApiUtils.getTMDBService();

        mMovie = getIntent().getParcelableExtra(getString(R.string.movie_key));

        mDetailBinding.collapsingToolbarLayout.setTitle(mMovie.getTitle());
        mDetailBinding.collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedTitleTextStyle);
        mDetailBinding.collapsingToolbarLayout.setCollapsedTitleTextColor(Color.WHITE);

        populateViews();

        if (savedInstanceState != null) {
            mVideos = savedInstanceState.getParcelableArrayList(MOVIE_VIDEOS_RESULTS);
            mReviews = savedInstanceState.getParcelableArrayList(MOVIE_REVIEWS_RESULTS);
            mMovieVideoAdapter.setMovieVideoData(mVideos);
            showVideos();
            mMovieReviewAdapter.setMovieReviewsData(mReviews);
            showReviews();
        }

        if (ApiUtils.isOnline(this)) {
            if (mVideos == null) {
                loadVideos();
            }
            if (mReviews == null) loadReviews();

        } else showErrorMessage();

        isFavorite();

        ViewCompat.setNestedScrollingEnabled(mDetailBinding.rvMovieReviews, false);
        ViewCompat.setNestedScrollingEnabled(mDetailBinding.rvMovieVideos, false);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(MOVIE_VIDEOS_RESULTS, mVideos);
        outState.putParcelableArrayList(MOVIE_REVIEWS_RESULTS, mReviews);

    }

    private void populateViews() {
        if (mMovie.getBackdropPath() != null) {
            String movieBackdropImageUrl = ApiUtils.getImageStringUrl(mMovie.getBackdropPath(), "w780");
            Picasso.with(this).load(movieBackdropImageUrl).into(mDetailBinding.ivMovieBackdrop);
        }

        if (mMovie.getPosterPath() != null) {
            String moviePosterImageUrl = ApiUtils.getImageStringUrl(mMovie.getPosterPath(), "w500");
            Picasso.with(this).load(moviePosterImageUrl).into(mDetailBinding.ivMoviePoster);
        }

        String releaseDate = mMovie.getReleaseDate();
        String releaseYear = releaseDate.substring(0, 4);
        mDetailBinding.tvMovieReleaseDate.setText(releaseYear);

        String rating = Double.toString(mMovie.getVoteAverage());
        rating += "/10";
        mDetailBinding.tvMovieRating.setText(rating);

        mDetailBinding.tvMovieOriginalTitle.setText(mMovie.getOriginalTitle());
        mDetailBinding.tvMovieOverview.setText(mMovie.getOverview());

        mMovieVideoAdapter = new MovieVideoAdapter(this, this);
        mDetailBinding.rvMovieVideos.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mDetailBinding.rvMovieVideos.setAdapter(mMovieVideoAdapter);

        mMovieReviewAdapter = new MovieReviewAdapter();
        mDetailBinding.rvMovieReviews.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mDetailBinding.rvMovieReviews.setAdapter(mMovieReviewAdapter);
    }

    private void isFavorite() {
        Uri uri = MovieContract.buildMovieUriWithId(mMovie.getId());
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        mIsFavorite = exists;
        setFavoriteIcon();
    }

    public void loadVideos() {
        mDetailBinding.pbMovieVideosLoadingIndicator.setVisibility(View.VISIBLE);
        mService.getVideos(mMovie.getId()).enqueue(new Callback<TMDBMovieVideosResponse>() {
            @Override
            public void onResponse(Call<TMDBMovieVideosResponse> call, Response<TMDBMovieVideosResponse> response) {
                if (response.isSuccessful()) {
                    mDetailBinding.pbMovieVideosLoadingIndicator.setVisibility(View.INVISIBLE);
                    mVideos = (ArrayList<MovieVideo>) response.body().getResults();
                    mMovieVideoAdapter.setMovieVideoData(mVideos);
                    showVideos();
                    Log.d(TAG, "videos loaded from API");
                } else {
                    int statusCode = response.code();
                    // handle request errors depending on status code
                    Log.v(TAG, "Request error. Status code: " + statusCode);
                }
            }

            @Override
            public void onFailure(Call<TMDBMovieVideosResponse> call, Throwable t) {
                Log.d(TAG, "error loading from API");
            }
        });
    }

    public void loadReviews() {
        mDetailBinding.pbMovieReviewsLoadingIndicator.setVisibility(View.VISIBLE);
        mService.getReviews(mMovie.getId()).enqueue(new Callback<TMDBMovieReviewsResponse>() {
            @Override
            public void onResponse(Call<TMDBMovieReviewsResponse> call, Response<TMDBMovieReviewsResponse> response) {
                if (response.isSuccessful()) {
                    mDetailBinding.pbMovieReviewsLoadingIndicator.setVisibility(View.INVISIBLE);
                    mReviews = (ArrayList<MovieReview>) response.body().getResults();
                    mMovieReviewAdapter.setMovieReviewsData(mReviews);
                    showReviews();
                    Log.d(TAG, "reviews loaded from API");
                } else {
                    int statusCode = response.code();
                    // handle request errors depending on status code
                    Log.v(TAG, "Request error. Status code: " + statusCode);
                }
            }

            @Override
            public void onFailure(Call<TMDBMovieReviewsResponse> call, Throwable t) {
                Log.d(TAG, "error loading from API");
            }
        });
    }

    @Override
    public void onClickVideo(MovieVideo selectedVideo) {
        Context context = this;
        String videoId = selectedVideo.getKey();
        Uri uri = Uri.parse(ApiUtils.YOUTUBE_BASE_URL).buildUpon().appendQueryParameter("v", videoId).build();

        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + videoId));
        Intent webIntent = new Intent(Intent.ACTION_VIEW, uri);
        try {
            context.startActivity(appIntent);
        } catch (ActivityNotFoundException e) {
            context.startActivity(webIntent);
        }

    }

    public void onClickToggleFavorite(View view) {
        if (mIsFavorite) {
            Uri uri = MovieContract.buildMovieUriWithId(mMovie.getId());
            int moviesDeleted = getContentResolver().delete(uri, null, null);
            if (moviesDeleted != 0) {
                if(mToast != null) mToast.cancel();
                mToast = Toast.makeText(getBaseContext(), R.string.unmarked_as_favorite, Toast.LENGTH_SHORT);
                mToast.show();
                mIsFavorite = !mIsFavorite;
            }
        } else {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, mMovie.getId());
            contentValues.put(MovieContract.MovieEntry.COLUMN_TITLE, mMovie.getTitle());
            contentValues.put(MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE, mMovie.getOriginalTitle());
            contentValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, mMovie.getPosterPath());
            contentValues.put(MovieContract.MovieEntry.COLUMN_BACKDROP_PATH, mMovie.getBackdropPath());
            contentValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, mMovie.getOverview());
            contentValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, mMovie.getReleaseDate());
            contentValues.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, mMovie.getVoteAverage());

            Uri uri = getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI, contentValues);

            if (uri != null) {
                if(mToast != null) mToast.cancel();
                mToast = Toast.makeText(getBaseContext(), R.string.marked_as_favorite, Toast.LENGTH_SHORT);
                mToast.show();
                mIsFavorite = !mIsFavorite;
            }
        }
        setFavoriteIcon();
    }

    private void setFavoriteIcon() {
        if (mIsFavorite) {
            mDetailBinding.ivFavorite.setImageResource(R.drawable.ic_favorite_white_24dp);
        } else {
            mDetailBinding.ivFavorite.setImageResource(R.drawable.ic_favorite_border_white_24dp);
        }
    }

    private void showVideos() {
        if (mMovieVideoAdapter.getItemCount() == 0) {
            mDetailBinding.rvMovieVideos.setVisibility(View.GONE);
            mDetailBinding.tvMovieVideosNoVideosMessage.setVisibility(View.VISIBLE);
        } else {
            mDetailBinding.rvMovieVideos.setVisibility(View.VISIBLE);
            mDetailBinding.tvMovieVideosNoVideosMessage.setVisibility(View.GONE);
        }
    }

    private void showReviews() {
        if (mMovieReviewAdapter.getItemCount() == 0) {
            mDetailBinding.rvMovieReviews.setVisibility(View.GONE);
            mDetailBinding.tvMovieReviewsNoReviewsMessage.setVisibility(View.VISIBLE);
        } else {
            mDetailBinding.rvMovieReviews.setVisibility(View.VISIBLE);
            mDetailBinding.tvMovieReviewsNoReviewsMessage.setVisibility(View.GONE);
        }
    }

    private void showErrorMessage() {
        mDetailBinding.pbMovieVideosLoadingIndicator.setVisibility(View.GONE);
        mDetailBinding.rvMovieVideos.setVisibility(View.GONE);
        mDetailBinding.movieVideosErrorMessageDisplay.setVisibility(View.VISIBLE);

        mDetailBinding.pbMovieReviewsLoadingIndicator.setVisibility(View.GONE);
        mDetailBinding.rvMovieReviews.setVisibility(View.GONE);
        mDetailBinding.movieReviewsErrorMessageDisplay.setVisibility(View.VISIBLE);
    }


}
