package com.example.alex.recommendations;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.TextView;

import com.example.alex.recommendations.api.Etsy;
import com.example.alex.recommendations.google.GoogleServicesHelper;
import com.example.alex.recommendations.model.ActiveListings;

public class MainActivity extends AppCompatActivity {

    public static final String STATE_ACTIVE_LISTINGS = "StateActiveListings";

    private RecyclerView mRecyclerView;
    private View mProgressBar;
    private TextView mErrorView;
    private ListingAdapter adapter;
    private GoogleServicesHelper mGoogleServicesHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        mProgressBar = findViewById(R.id.progressbar);
        mErrorView = (TextView) findViewById(R.id.error_view);

        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));

        adapter = new ListingAdapter(this);

        mRecyclerView.setAdapter(adapter);

        mGoogleServicesHelper = new GoogleServicesHelper(this, adapter);

        showLoading();

        if(savedInstanceState != null) {
            if(savedInstanceState.containsKey(STATE_ACTIVE_LISTINGS)) {
                adapter.success((ActiveListings) savedInstanceState.getParcelable(STATE_ACTIVE_LISTINGS), null);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleServicesHelper.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleServicesHelper.disconnect();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mGoogleServicesHelper.handleActivityResult(requestCode, resultCode, data);

        if(requestCode == ListingAdapter.REQUEST_CODE_PLUS_ONE) {
            adapter.notifyDataSetChanged();
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ActiveListings activeListings = adapter.getActiveListings();
        if(activeListings != null) {
            outState.putParcelable(STATE_ACTIVE_LISTINGS, activeListings);
        }
    }

    public void showLoading() {
        mProgressBar.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.GONE);
        mErrorView.setVisibility(View.GONE);
    }

    public void showList() {
        mProgressBar.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
        mErrorView.setVisibility(View.GONE);
    }

    public void showError() {
        mProgressBar.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.GONE);
        mErrorView.setVisibility(View.VISIBLE);
    }
}
