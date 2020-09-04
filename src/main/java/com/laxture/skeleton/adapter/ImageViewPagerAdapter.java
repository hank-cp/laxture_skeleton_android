package com.laxture.skeleton.adapter;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import android.view.ViewGroup;

import com.laxture.lib.cache.storage.CacheStorage;
import com.laxture.lib.util.Checker;
import com.laxture.lib.util.UnHandledException;
import com.laxture.skeleton.activity.ImageViewerFragment;

import java.util.List;

public class ImageViewPagerAdapter extends FragmentPagerAdapter {

    private List<CacheStorage> mStorages;
    private FragmentManager mFragmentManger;
    private String mTaskTag;

    public ImageViewPagerAdapter(FragmentManager fragmentManager,
                                 List<CacheStorage> storages,
                                 String taskTag) {
        super(fragmentManager);
        mFragmentManger = fragmentManager;
        mStorages = storages;
        mTaskTag = taskTag;
        if (Checker.isEmpty(mStorages)) {
            throw new IllegalArgumentException("ContentStorage cannot be empty");
        }
    }

    @Override
    public Fragment getItem(int position) {
        ImageViewerFragment fragment;
        try {
            fragment = ImageViewerFragment.class.newInstance();
        } catch (Exception e) {
            throw new UnHandledException("Cannot instansiate ImageViewerFragment with class", e);
        }
        Bundle args = new Bundle();
        args.putSerializable(ImageViewerFragment.EXTRA_CONTENT_STORAGE, mStorages.get(position));
        args.putString(ImageViewerFragment.EXTRA_TASK_TAG, mTaskTag);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() {
        return mStorages.size();
    }

    public List<CacheStorage> getContents() {
        return mStorages;
    }

    public void removeItem(ViewGroup viewGroup, int position) {
        FragmentTransaction fragmentTransaction = mFragmentManger.beginTransaction();
        if (position >= 0 && position < getCount()) {
            Object item = instantiateItem(viewGroup, position);
            if (null != item) {
                fragmentTransaction.remove((Fragment) item);
            }
            fragmentTransaction.commit();
            mFragmentManger.executePendingTransactions();
        }
    }
}
