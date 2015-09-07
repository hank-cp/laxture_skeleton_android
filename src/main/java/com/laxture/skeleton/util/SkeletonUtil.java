package com.laxture.skeleton.util;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

public class SkeletonUtil {

    public static ActionBar getSupportActionBar(Activity activity) {
        return ((AppCompatActivity) activity).getSupportActionBar();
    }

    public static ActionBar getSupportActionBar(Fragment fragment) {
        return ((AppCompatActivity) fragment.getActivity()).getSupportActionBar();
    }

}
