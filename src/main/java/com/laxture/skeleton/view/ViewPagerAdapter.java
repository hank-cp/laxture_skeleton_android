package com.laxture.skeleton.view;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class ViewPagerAdapter extends PagerAdapter {

    private List<View> mContents;

    public ViewPagerAdapter(List<View> views) {
        mContents = views;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int pos) {
        container.addView(mContents.get(pos), 0);
        return mContents.get(pos);
    }

    @Override
    public void destroyItem(ViewGroup container, int pos, Object obj) {
        container.removeView(mContents.get(pos));
    }

    @Override
    public int getCount() {
        return null != mContents ? mContents.size() : 0;
    }

    @Override
    public boolean isViewFromObject(View view, Object o) {
        return view == o;
    }
}
