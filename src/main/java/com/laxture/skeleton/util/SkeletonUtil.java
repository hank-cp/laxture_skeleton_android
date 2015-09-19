package com.laxture.skeleton.util;

import com.laxture.skeleton.R;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextThemeWrapper;

import com.laxture.lib.RuntimeContext;

public class SkeletonUtil {

    public static ActionBar getSupportActionBar(Activity activity) {
        return ((AppCompatActivity) activity).getSupportActionBar();
    }

    public static ActionBar getSupportActionBar(Fragment fragment) {
        return ((AppCompatActivity) fragment.getActivity()).getSupportActionBar();
    }

    public static Context getStyledContext() {
        return new ContextThemeWrapper(RuntimeContext.getApplication(), R.style.AppTheme);
    }

}
