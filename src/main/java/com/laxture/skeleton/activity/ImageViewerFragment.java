package com.laxture.skeleton.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.laxture.lib.cache.storage.CacheStorage;
import com.laxture.lib.view.AsyncImageAdapter;
import com.laxture.lib.view.AsyncTouchImageView;
import com.laxture.skeleton.R;

public class ImageViewerFragment extends Fragment {

    public static final String EXTRA_CONTENT_STORAGE = "extra_content_storage";
    public static final String EXTRA_TASK_TAG = "extra_task_tag";

    private AsyncTouchImageView mImageView;
    protected CacheStorage mContentStorage;
    private String mTaskTag;

    @Override
    public void onCreate(Bundle arg0) {
        super.onCreate(arg0);

        mContentStorage = (CacheStorage) getArguments().getSerializable(EXTRA_CONTENT_STORAGE);
        mTaskTag = getArguments().getString(EXTRA_TASK_TAG);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mImageView = new AsyncTouchImageView(getActivity());
        mImageView.setMaxZoom(4f);
        mImageView.getAdapter().setDensity(AsyncImageAdapter.DENSITY_XHIGH);
        mImageView.getAdapter().setFailedImageRes(R.drawable.img_loading_full_failed);
        mImageView.getAdapter().setLoadingImageRes(R.drawable.img_loading_full);
        return mImageView;
    }

    @Override
    public void onStart() {
        super.onStart();
        mImageView.setImage(mTaskTag, mContentStorage);
    }

}
