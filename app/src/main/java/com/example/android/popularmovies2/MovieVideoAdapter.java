package com.example.android.popularmovies2;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.android.popularmovies2.data.model.MovieVideo;
import com.example.android.popularmovies2.utils.ApiUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MovieVideoAdapter extends RecyclerView.Adapter<MovieVideoAdapter.MovieVideoViewHolder> {
    private final MovieVideoAdapterOnClickHandler mClickHandler;
    private ArrayList<MovieVideo> mMovieVideos;
    private Context mContext;

    public MovieVideoAdapter(Context context, MovieVideoAdapter.MovieVideoAdapterOnClickHandler clickHandler) {
        mContext = context;
        mClickHandler = clickHandler;
    }

    @NonNull
    @Override
    public MovieVideoAdapter.MovieVideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.movie_video_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(layoutIdForListItem, parent, false);
        return new MovieVideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieVideoViewHolder holder, int position) {
        String videoId = mMovieVideos.get(position).getKey();
        String url = ApiUtils.getVideoThumbnailStringUrl(videoId);
        Picasso.with(mContext)
                .load(url)
                .into(holder.mMovieVideoThumbnail);
    }

    @Override
    public int getItemCount() {
        if (mMovieVideos == null) return 0;
        return mMovieVideos.size();
    }

    public void setMovieVideoData(ArrayList<MovieVideo> movieVideoData) {
        mMovieVideos = movieVideoData;
        notifyDataSetChanged();
    }

    public interface MovieVideoAdapterOnClickHandler {
        void onClick(MovieVideo selectedVideo);
    }

    class MovieVideoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.iv_movie_video_thumbnail)
        ImageView mMovieVideoThumbnail;

        MovieVideoViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mClickHandler.onClick(mMovieVideos.get(adapterPosition));
        }
    }
}