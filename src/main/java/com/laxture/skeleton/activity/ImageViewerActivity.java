package com.laxture.skeleton.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.laxture.lib.cache.storage.CacheStorage;
import com.laxture.lib.task.TaskManager;
import com.laxture.lib.util.Checker;
import com.laxture.lib.view.ImageViewPager;
import com.laxture.skeleton.R;
import com.laxture.skeleton.adapter.ImageViewPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class ImageViewerActivity extends AppCompatActivity {

    public static final String EXTRA_STORAGE = "extra_storage";
    public static final String EXTRA_STORAGE_LIST = "extra_storage_list";
    public static final String EXTRA_INDEX = "extra_index";

    public static final String TASK_TAG_PICTURE_VIEW = "TASK_TAG_PICTURE_VIEW";

    private ImageViewPager mImageViewPager;

    protected List<CacheStorage> mCacheStorages;
    protected ImageViewPagerAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        mImageViewPager = (ImageViewPager) findViewById(R.id.pager);
        loadContent();
        setupAdapter();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TaskManager.cancelByTag(TASK_TAG_PICTURE_VIEW);
    }

    protected void loadContent() {
        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_STORAGE)) {
            CacheStorage storage = (CacheStorage) intent.getSerializableExtra(EXTRA_STORAGE);
            if (storage != null) {
                mCacheStorages = new ArrayList<>();
                mCacheStorages.add(storage);
            }
        } else if (intent.hasExtra(EXTRA_STORAGE_LIST)) {
            mCacheStorages = (List<CacheStorage>) intent.getSerializableExtra(EXTRA_STORAGE_LIST);
        } else {
            throw new IllegalArgumentException("Extra EXTRA_STORAGE_LIST or EXTRA_STORAGE must be set!");
        }

        if (Checker.isEmpty(mCacheStorages)) {
            throw new IllegalArgumentException("Extra EXTRA_STORAGE_LIST or EXTRA_STORAGE must be set!");
        }
    }
    
    protected void setupAdapter() {
        int index = getIntent().getIntExtra(EXTRA_INDEX, 0);
        mAdapter = new ImageViewPagerAdapter(getSupportFragmentManager(),
                mCacheStorages, TASK_TAG_PICTURE_VIEW);
        mImageViewPager.setAdapter(mAdapter);
        mImageViewPager.setCurrentItem(index);
    }

}
