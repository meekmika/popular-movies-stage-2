package com.example.android.popularmovies2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.example.android.popularmovies2.data.MovieContract;
import com.example.android.popularmovies2.data.model.Movie;
import com.example.android.popularmovies2.data.model.TMDBMoviesResponse;
import com.example.android.popularmovies2.data.remote.TMDBService;
import com.example.android.popularmovies2.utils.ApiUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener,
        MoviePosterAdapter.MoviePosterAdapterOnClickHandler,
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String MOVIES_RESULT = "movies-result";
    private static final int FAVORITE_MOVIE_LOADER_ID = 42;

    @BindView(R.id.rv_movies)
    RecyclerView mRecyclerView;
    @BindView(R.id.error_message_display)
    LinearLayout mErrorMessageDisplay;
    @BindView(R.id.pb_loading_indicator)
    ProgressBar mLoadingIndicator;
    private TMDBService mService;
    private MoviePosterAdapter mAdapter;
    private ArrayList<Movie> mMovies;
    private ArrayList<Movie> mFavoriteMovies;
    private String mPrefSortOrder;
    private boolean mShowFavorites;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mService = ApiUtils.getTMDBService();
        mAdapter = new MoviePosterAdapter(this, this);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, numberOfColumns());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mPrefSortOrder = prefs.getString(getString(R.string.pref_sort_order_key), getString(R.string.pref_sort_order_popular));
        mShowFavorites = prefs.getBoolean(getString(R.string.pref_show_favorites_key), getResources().getBoolean(R.bool.pref_show_favorites_default));

        getSupportLoaderManager().initLoader(FAVORITE_MOVIE_LOADER_ID, null, this);

        if (savedInstanceState != null) {
            mMovies = savedInstanceState.getParcelableArrayList(MOVIES_RESULT);
        }

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    public void loadMovies() {
        mLoadingIndicator.setVisibility(View.VISIBLE);
        if (ApiUtils.isOnline(this)) {
            mService.getMovies(mPrefSortOrder).enqueue(new Callback<TMDBMoviesResponse>() {
                @Override
                public void onResponse(Call<TMDBMoviesResponse> call, Response<TMDBMoviesResponse> response) {
                    if (response.isSuccessful()) {
                        mLoadingIndicator.setVisibility(View.INVISIBLE);
                        mMovies = (ArrayList<Movie>) response.body().getResults();
                        mAdapter.setMovieData(mMovies);
                        Log.d(TAG, "movies loaded from API");
                    } else {
                        int statusCode = response.code();
                        // handle request errors depending on status code
                        Log.v(TAG, "Request error. Status code: " + statusCode);
                    }
                }

                @Override
                public void onFailure(Call<TMDBMoviesResponse> call, Throwable t) {
                    Log.d(TAG, "error loading from API");
                    showErrorMessage();
                }
            });
        } else {
            showErrorMessage();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(MOVIES_RESULT, mMovies);
    }

    private int numberOfColumns() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int widthDivider = 400;
        int width = displayMetrics.widthPixels;
        int nColumns = width / widthDivider;
        if (nColumns < 2) return 2;
        return nColumns;
    }

    @Override
    public void onClick(Movie selectedMovie) {
        Context context = this;
        Class destinationClass = DetailActivity.class;
        Intent intentToStartDetailActivity = new Intent(context, destinationClass);
        intentToStartDetailActivity.putExtra(getString(R.string.movie_key), selectedMovie);
        startActivity(intentToStartDetailActivity);
    }

    private void showErrorMessage() {
        mLoadingIndicator.setVisibility(View.GONE);
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.GONE);
    }

    private void showMovies() {
        mErrorMessageDisplay.setVisibility(View.GONE);
        if (mShowFavorites) {
            if (mFavoriteMovies == null)
                getSupportLoaderManager().restartLoader(FAVORITE_MOVIE_LOADER_ID, null, this);
            mAdapter.setMovieData(mFavoriteMovies);
        } else {
            if (mMovies == null) loadMovies();
            mAdapter.setMovieData(mMovies);
        }
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportLoaderManager().restartLoader(FAVORITE_MOVIE_LOADER_ID, null, this);
        showMovies();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_settings) {
            Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(startSettingsActivity);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_sort_order_key))) {
            mMovies = null;
            mFavoriteMovies = null;
            mPrefSortOrder = sharedPreferences.getString(key, getString(R.string.pref_sort_order_popular));
            showMovies();
        } else if (key.equals(getString(R.string.pref_show_favorites_key))) {
            mShowFavorites = sharedPreferences.getBoolean(key, getResources().getBoolean(R.bool.pref_show_favorites_default));
            showMovies();
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle loaderArgs) {
        return new AsyncTaskLoader<Cursor>(this) {

            Cursor favoriteMovies = null;

            @Override
            protected void onStartLoading() {
                if (favoriteMovies != null) {
                    deliverResult(favoriteMovies);
                } else {
                    forceLoad();
                }
            }

            @Nullable
            @Override
            public Cursor loadInBackground() {

                String sortorder = null;
                switch (mPrefSortOrder) {
                    case "top_rated":
                        sortorder = MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE;
                        break;
                    case "popular":
                        sortorder = MovieContract.MovieEntry.COLUMN_POPULARITY;
                }

                try {
                    return getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI,
                            null,
                            null,
                            null,
                            sortorder);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to load favorite movies");
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public void deliverResult(@Nullable Cursor data) {
                favoriteMovies = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        ArrayList<Movie> favoriteMovies = new ArrayList<Movie>();
        int movieIdIndex = data.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_ID);
        int titleIndex = data.getColumnIndex(MovieContract.MovieEntry.COLUMN_TITLE);
        int originalTitleIndex = data.getColumnIndex(MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE);
        int posterPathIndex = data.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER_PATH);
        int backdropPathIndex = data.getColumnIndex(MovieContract.MovieEntry.COLUMN_BACKDROP_PATH);
        int overviewIndex = data.getColumnIndex(MovieContract.MovieEntry.COLUMN_OVERVIEW);
        int releaseDateIndex = data.getColumnIndex(MovieContract.MovieEntry.COLUMN_RELEASE_DATE);
        int voteAverageIndex = data.getColumnIndex(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE);
        int popularityIndex = data.getColumnIndex(MovieContract.MovieEntry.COLUMN_POPULARITY);

        while (data.moveToNext()) {
            Movie movie = new Movie();
            movie.setId(data.getInt(movieIdIndex));
            movie.setTitle(data.getString(titleIndex));
            movie.setOriginalTitle(data.getString(originalTitleIndex));
            movie.setPosterPath(data.getString(posterPathIndex));
            movie.setBackdropPath(data.getString(backdropPathIndex));
            movie.setOverview(data.getString(overviewIndex));
            movie.setReleaseDate(data.getString(releaseDateIndex));
            movie.setVoteAverage(data.getDouble(voteAverageIndex));
            movie.setPopularity(data.getDouble(popularityIndex));
            favoriteMovies.add(movie);
        }

        mFavoriteMovies = favoriteMovies;
        if (mShowFavorites) mAdapter.setMovieData(mFavoriteMovies);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mAdapter.setMovieData(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }


}
