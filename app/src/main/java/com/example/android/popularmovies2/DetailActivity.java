package com.example.android.popularmovies2;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.popularmovies2.data.model.Movie;
import com.example.android.popularmovies2.data.model.MovieVideo;
import com.example.android.popularmovies2.data.model.TMDBMovieReviewsResponse;
import com.example.android.popularmovies2.data.model.TMDBMovieVideosResponse;
import com.example.android.popularmovies2.data.remote.TMDBService;
import com.example.android.popularmovies2.utils.ApiUtils;
import com.squareup.picasso.Picasso;

import net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailActivity extends AppCompatActivity implements MovieVideoAdapter.MovieVideoAdapterOnClickHandler {

    private static final String TAG = MainActivity.class.getSimpleName();
    @BindView(R.id.iv_movie_backdrop)
    ImageView mMovieBackdropImageView;
    @BindView(R.id.iv_movie_poster)
    ImageView mMoviePosterImageView;
    @BindView(R.id.tv_movie_release_date)
    TextView mMovieReleaseDateTextView;
    @BindView(R.id.tv_movie_rating)
    TextView mMovieRatingTextView;
    @BindView(R.id.tv_movie_original_title)
    TextView mMovieOriginalTitle;
    @BindView(R.id.tv_movie_overview)
    TextView mMovieOverviewTextView;
    @BindView(R.id.collapsing_toolbar_layout)
    CollapsingToolbarLayout mCollapsingToolbarLayout;
    @BindView(R.id.detail_toolbar)
    Toolbar mToolbar;
    @Nullable
    @BindView(R.id.rv_movie_videos)
    RecyclerView mMovieVideosRecyclerView;
    @Nullable
    @BindView(R.id.pb_movie_videos_loading_indicator)
    ProgressBar mMovieVideosLoadingIndicator;
    @Nullable
    @BindView(R.id.movie_videos_error_message_display)
    LinearLayout mMovieVideosErrorMessageDisplay;
    @Nullable
    @BindView(R.id.tv_movie_videos_no_videos_message)
    TextView mNoVideosMessage;
    @Nullable
    @BindView(R.id.rv_movie_reviews)
    RecyclerView mMovieReviewsRecyclerView;
    @Nullable
    @BindView(R.id.pb_movie_reviews_loading_indicator)
    ProgressBar mMovieReviewsLoadingIndicator;
    @Nullable
    @BindView(R.id.movie_reviews_error_message_display)
    LinearLayout mMovieReviewsErrorMessageDisplay;
    @Nullable
    @BindView(R.id.tv_movie_reviews_no_reviews_message)
    TextView mNoReviewsMessage;
    private Movie mMovie;
    private TMDBService mService;
    private MovieVideoAdapter mMovieVideoAdapter;
    private MovieReviewAdapter mMovieReviewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mService = ApiUtils.getTMDBService();

        mMovie = getIntent().getParcelableExtra(getString(R.string.movie_key));

        mCollapsingToolbarLayout.setTitle(mMovie.getTitle());
        mCollapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedTitleTextStyle);
        mCollapsingToolbarLayout.setCollapsedTitleTextColor(Color.WHITE);

        if (mMovie.getBackdropPath() != null) {
            String movieBackdropImageUrl = ApiUtils.getImageStringUrl(mMovie.getBackdropPath(), "w780");
            Picasso.with(this).load(movieBackdropImageUrl).into(mMovieBackdropImageView);
        }

        if (mMovie.getPosterPath() != null) {
            String moviePosterImageUrl = ApiUtils.getImageStringUrl(mMovie.getPosterPath(), "w500");
            Picasso.with(this).load(moviePosterImageUrl).into(mMoviePosterImageView);
        }

        String releaseDate = mMovie.getReleaseDate();
        String releaseYear = releaseDate.substring(0, 4);
        mMovieReleaseDateTextView.setText(releaseYear);

        String rating = Double.toString(mMovie.getVoteAverage());
        rating += "/10";
        mMovieRatingTextView.setText(rating);

        mMovieOriginalTitle.setText(mMovie.getOriginalTitle());
        mMovieOverviewTextView.setText(mMovie.getOverview());

        mMovieVideoAdapter = new MovieVideoAdapter(this, this);
        mMovieVideosRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mMovieVideosRecyclerView.setAdapter(mMovieVideoAdapter);

        mMovieReviewAdapter = new MovieReviewAdapter();
        mMovieReviewsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mMovieReviewsRecyclerView.setAdapter(mMovieReviewAdapter);

        if (ApiUtils.isOnline(this)) {
            mMovieVideosLoadingIndicator.setVisibility(View.VISIBLE);
            mMovieReviewsLoadingIndicator.setVisibility(View.VISIBLE);
            loadVideos();
            loadReviews();
        } else showErrorMessage();

        ViewCompat.setNestedScrollingEnabled(mMovieVideosRecyclerView, false);
        ViewCompat.setNestedScrollingEnabled(mMovieReviewsRecyclerView, false);
    }

    public void loadVideos() {
        mService.getVideos(mMovie.getId()).enqueue(new Callback<TMDBMovieVideosResponse>() {
            @Override
            public void onResponse(Call<TMDBMovieVideosResponse> call, Response<TMDBMovieVideosResponse> response) {
                if (response.isSuccessful()) {
                    mMovieVideosLoadingIndicator.setVisibility(View.INVISIBLE);
                    mMovieVideoAdapter.setMovieVideoData(response.body().getResults());
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

    @Override
    public void onClick(MovieVideo selectedVideo) {
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

    public void loadReviews() {
        mService.getReviews(mMovie.getId()).enqueue(new Callback<TMDBMovieReviewsResponse>() {
            @Override
            public void onResponse(Call<TMDBMovieReviewsResponse> call, Response<TMDBMovieReviewsResponse> response) {
                if (response.isSuccessful()) {
                    mMovieReviewsLoadingIndicator.setVisibility(View.INVISIBLE);
                    mMovieReviewAdapter.setMovieReviewsData(response.body().getResults());
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

    private void showVideos() {
        if (mMovieVideoAdapter.getItemCount() == 0) {
            mMovieVideosRecyclerView.setVisibility(View.GONE);
            mNoVideosMessage.setVisibility(View.VISIBLE);
        } else {
            mMovieVideosRecyclerView.setVisibility(View.VISIBLE);
            mNoVideosMessage.setVisibility(View.GONE);
        }
    }

    private void showReviews() {
        if (mMovieReviewAdapter.getItemCount() == 0) {
            mMovieReviewsRecyclerView.setVisibility(View.GONE);
            mNoReviewsMessage.setVisibility(View.VISIBLE);
        } else {
            mMovieReviewsRecyclerView.setVisibility(View.VISIBLE);
            mNoReviewsMessage.setVisibility(View.GONE);
        }
    }

    private void showErrorMessage() {
        mMovieVideosLoadingIndicator.setVisibility(View.GONE);
        mMovieVideosRecyclerView.setVisibility(View.GONE);
        mMovieVideosErrorMessageDisplay.setVisibility(View.VISIBLE);

        mMovieReviewsLoadingIndicator.setVisibility(View.GONE);
        mMovieReviewsRecyclerView.setVisibility(View.GONE);
        mMovieReviewsErrorMessageDisplay.setVisibility(View.VISIBLE);
    }


}
