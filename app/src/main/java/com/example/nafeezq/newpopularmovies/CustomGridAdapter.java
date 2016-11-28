package com.example.nafeezq.newpopularmovies;

import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by nafeezq on 11/7/2016.
 * This class is for populating poster bitmaps on the GridView
 */
public class CustomGridAdapter extends BaseAdapter {

    public MainActivityFragment.FetchPosterTask mContext;
    public ArrayList<Bitmap> mThumbIds;

    // Constructor to be used in Main Activity class
    public CustomGridAdapter(MainActivityFragment.FetchPosterTask posterTask, ArrayList<Bitmap> mPoster) {
        this.mContext = posterTask;
        this.mThumbIds = mPoster;

    }

    // Constructor to be used in FavoriteActivity class

    public CustomGridAdapter(ArrayList<Bitmap> mPoster) {

        this.mThumbIds = mPoster;
    }


    @Override
    public int getCount() {
        return mThumbIds.size();
    }

    @Override
    public Object getItem(int position) {
        return mThumbIds.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ImageView imageView;
        imageView = new ImageView(parent.getContext());
        imageView.setImageBitmap(mThumbIds.get(position));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setLayoutParams(new GridView.LayoutParams(300, 300));
        return imageView;
    }

}
