package com.example.android.popularmovies2;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.popularmovies2.data.model.MovieReview;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MovieReviewAdapter extends RecyclerView.Adapter<MovieReviewAdapter.MovieReviewViewHolder> {
    private ArrayList<MovieReview> mMovieReviews;

    @NonNull
    @Override
    public MovieReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.movie_review_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(layoutIdForListItem, parent, false);
        return new MovieReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieReviewViewHolder holder, int position) {
        String movieReviewAuthor = mMovieReviews.get(position).getAuthor();
        String movieReviewContent = mMovieReviews.get(position).getContent();
        holder.mMovieReviewAuthorTextView.setText(movieReviewAuthor);
        holder.mMovieReviewContentTextView.setText(movieReviewContent);
    }

    @Override
    public int getItemCount() {
        if (mMovieReviews == null) return 0;
        return mMovieReviews.size();
    }

    public void setMovieReviewsData(ArrayList<MovieReview> movieReviewData) {
        mMovieReviews = movieReviewData;
        notifyDataSetChanged();
    }

    class MovieReviewViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_movie_review_author)
        TextView mMovieReviewAuthorTextView;
        @BindView(R.id.tv_movie_review_content)
        TextView mMovieReviewContentTextView;

        MovieReviewViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

}
