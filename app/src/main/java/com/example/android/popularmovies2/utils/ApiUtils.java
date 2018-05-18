package com.example.android.popularmovies2.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.example.android.popularmovies2.data.remote.RetrofitClient;
import com.example.android.popularmovies2.data.remote.TMDBService;

/**
 * Created by mika on 2018-04-16.
 */

public class ApiUtils {
    public static final String API_BASE_URL = "https://api.themoviedb.org/3/";
    private static final String IMAGE_BASE_URL = "http://image.tmdb.org/t/p/";

    public static TMDBService getTMDBService() {
        return RetrofitClient.getClient(API_BASE_URL).create(TMDBService.class);
    }

    public static String getImageUrl(String posterPath, String imageSize) {
        return IMAGE_BASE_URL + "/" + imageSize + posterPath;
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
