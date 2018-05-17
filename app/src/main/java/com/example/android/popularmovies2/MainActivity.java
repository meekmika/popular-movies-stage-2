package com.example.android.popularmovies2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

import com.example.android.popularmovies2.data.model.Movie;
import com.example.android.popularmovies2.data.model.TMDBResponse;
import com.example.android.popularmovies2.data.remote.TMDBService;
import com.example.android.popularmovies2.utils.ApiUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener,
        MoviePosterAdapter.MovieAdapterOnClickHandler {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static boolean PREFERENCES_HAS_BEEN_CHANGED = false;
    @Nullable
    @BindView(R.id.rv_movies)
    RecyclerView mRecyclerView;
    @Nullable
    @BindView(R.id.error_message_display)
    LinearLayout mErrorMessageDisplay;
    @Nullable
    @BindView(R.id.pb_loading_indicator)
    ProgressBar mLoadingIndicator;
    private TMDBService mService;
    private MoviePosterAdapter mAdapter;

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
        String prefSortOrder = prefs.getString(getString(R.string.pref_sort_order_key), getString(R.string.pref_sort_order_popular));

        if (isOnline()) {
            mLoadingIndicator.setVisibility(View.VISIBLE);
            loadAnswers(prefSortOrder);
            showData();
        } else showErrorMessage();

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    public void loadAnswers(String sortOrder) {
        mService.getMovies(sortOrder).enqueue(new Callback<TMDBResponse>() {
            @Override
            public void onResponse(Call<TMDBResponse> call, Response<TMDBResponse> response) {
                if (response.isSuccessful()) {
                    mLoadingIndicator.setVisibility(View.INVISIBLE);
                    mAdapter.setMovieData(response.body().getResults());
                    Log.d(TAG, "posts loaded from API");
                } else {
                    int statusCode = response.code();
                    // handle request errors depending on status code
                    Log.v(TAG, "Request error. Status code: " + statusCode);
                }
            }

            @Override
            public void onFailure(Call<TMDBResponse> call, Throwable t) {
                Log.d(TAG, "error loading from API");
            }
        });
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private int numberOfColumns() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        // You can change this divider to adjust the size of the poster
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
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.INVISIBLE);
    }

    private void showData() {
        mErrorMessageDisplay.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
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
        PREFERENCES_HAS_BEEN_CHANGED = true;
        if (key.equals(getString(R.string.pref_sort_order_key))) {
            loadAnswers(sharedPreferences.getString(getString(R.string.pref_sort_order_key), getString(R.string.pref_sort_order_popular)));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (PREFERENCES_HAS_BEEN_CHANGED) {
            PREFERENCES_HAS_BEEN_CHANGED = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }
}
