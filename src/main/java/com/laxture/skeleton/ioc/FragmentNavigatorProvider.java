package com.laxture.skeleton.ioc;

import android.app.Activity;

import com.laxture.skeleton.controller.FragmentController;
import com.laxture.skeleton.controller.FragmentNavigator;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class FragmentNavigatorProvider implements Provider<FragmentNavigator> {

    @Inject
    protected Activity activity;

    public FragmentNavigator get() {
        return ((FragmentNavigator.FragmentNavigatorGetter)activity).getFragmentNavigator();
    }

}
