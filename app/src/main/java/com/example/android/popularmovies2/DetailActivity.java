package com.example.android.popularmovies2;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.popularmovies2.data.model.Movie;
import com.example.android.popularmovies2.utils.ApiUtils;
import com.squareup.picasso.Picasso;

import net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity {

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
    private Movie mMovie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mMovie = getIntent().getParcelableExtra(getString(R.string.movie_key));

        mCollapsingToolbarLayout.setTitle(mMovie.getTitle());
        mCollapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedTitleTextStyle);
        mCollapsingToolbarLayout.setCollapsedTitleTextColor(Color.WHITE);

        if (mMovie.getBackdropPath() != null) {
            String movieBackdropImageUrl = ApiUtils.getImageUrl(mMovie.getBackdropPath(), "w780");
            Picasso.with(this).load(movieBackdropImageUrl).into(mMovieBackdropImageView);
        }

        if (mMovie.getPosterPath() != null) {
            String moviePosterImageUrl = ApiUtils.getImageUrl(mMovie.getPosterPath(), "w500");
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
    }
}
