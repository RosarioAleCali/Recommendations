package com.example.alex.recommendations;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.alex.recommendations.api.Etsy;
import com.example.alex.recommendations.google.GoogleServicesHelper;
import com.example.alex.recommendations.model.ActiveListings;
import com.example.alex.recommendations.model.Listing;
import com.google.android.gms.plus.PlusOneButton;
import com.google.android.gms.plus.PlusShare;
import com.squareup.picasso.Picasso;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ListingAdapter extends RecyclerView.Adapter<ListingAdapter.ListingHolder> implements Callback<ActiveListings>,
        GoogleServicesHelper.GoogleServicesListener {

    private MainActivity mActivity;
    private LayoutInflater inflater;
    private ActiveListings activeListings;

    private boolean isGooglePlayServicesAvailable;

    public static final int REQUEST_CODE_PLUS_ONE = 10;
    public static final int REQUEST_CODE_SHARE = 11;

    public ListingAdapter(MainActivity activity) {
        mActivity = activity;
        inflater = LayoutInflater.from(activity);
        isGooglePlayServicesAvailable = false;
    }

    @Override
    public ListingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ListingHolder(inflater.inflate(R.layout.layout_listing,parent, false));
    }

    @Override
    public void onBindViewHolder(ListingHolder holder, int position) {
        final Listing listing = activeListings.results[position];
        holder.titleView.setText(listing.title);
        holder.priceView.setText(listing.price);
        holder.shopNameView.setText(listing.Shop.shop_name);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openListing = new Intent(Intent.ACTION_VIEW);
                openListing.setData(Uri.parse(listing.url));
                mActivity.startActivity(openListing);
            }
        });

        if(isGooglePlayServicesAvailable) {
            holder.plusOneButton.setVisibility(View.VISIBLE);
            holder.plusOneButton.initialize(listing.url, REQUEST_CODE_PLUS_ONE);
            holder.plusOneButton.setAnnotation(PlusOneButton.ANNOTATION_NONE);

            holder.shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new PlusShare.Builder(mActivity)
                            .setType("text/plain")
                            .setText("Checkout this item on Etsy " + listing.title)
                            .setContentUrl(Uri.parse(listing.url))
                            .getIntent();

                    mActivity.startActivityForResult(intent, REQUEST_CODE_SHARE);
                }
            });
        } else {
            holder.plusOneButton.setVisibility(View.GONE);

            holder.shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_TEXT, "Checkout this item on Etsy " + listing.title + " " + listing.url);
                    intent.setType("text/plain");

                    mActivity.startActivityForResult(Intent.createChooser(intent, "Share"), REQUEST_CODE_SHARE);
                }
            });
        }

        Picasso.with(holder.imageView.getContext()).load(listing.Images[0].url_570xN).into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        if(activeListings == null || activeListings.results == null)
            return 0;

        return activeListings.results.length;
    }

    @Override
    public void success(ActiveListings activeListings, Response response) {
        this.activeListings = activeListings;
        notifyDataSetChanged();
        this.mActivity.showList();
    }

    @Override
    public void failure(RetrofitError error) {
        this.mActivity.showError();
    }

    public ActiveListings getActiveListings() {
        return activeListings;
    }

    @Override
    public void onConnected() {

        if(getItemCount() == 0) {
            Etsy.getActiveListings(this);
        }

        isGooglePlayServicesAvailable = true;
        notifyDataSetChanged();
    }

    @Override
    public void onDisconnected() {

        if(getItemCount() == 0) {
            Etsy.getActiveListings(this);
        }

        isGooglePlayServicesAvailable = false;
        notifyDataSetChanged();
    }

    public class ListingHolder extends RecyclerView.ViewHolder {

        public ImageView imageView;
        public TextView titleView, shopNameView, priceView;
        public PlusOneButton plusOneButton;
        public ImageButton shareButton;

        public ListingHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.listing_image);
            titleView = (TextView) itemView.findViewById(R.id.listing_title);
            shopNameView = (TextView) itemView.findViewById(R.id.listing_shop_name);
            priceView = (TextView) itemView.findViewById(R.id.listing_price);
            plusOneButton = (PlusOneButton) itemView.findViewById(R.id.listing_plus_one_btn);
            shareButton = (ImageButton) itemView.findViewById(R.id.listing_sharing_btn);
        }
    }
}
