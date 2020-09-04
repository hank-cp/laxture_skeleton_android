package com.laxture.skeleton.controller;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

/**
 * FragmentActivity which hosts a group of navigatable Fragment must provide or implement
 * this interface.
 */
public interface FragmentController {

    enum InterceptionResult {
        Insert,
        Interrupted,
        Through
    }

    /**
     * We use this method to tell FragmentManager where to find needed {@link FragmentActivity}
     *
     * @return
     */
    FragmentActivity getActivity();

    /**
     * Get Fragment by name. Returned Fragment could be used to navigation or visit Argument.
     *
     * @param name
     * @return
     */
    Fragment getFragment(String name);

    /**
     * This callback will be fired at the beginning of
     * {@link FragmentNavigator#navigateTo(String, Fragment, Bundle, boolean, int)}.
     * Generally two purposes could be satisfied in this callback.
     * <ul>
     *     <li>Check if interception is needed. If interception is required,
     *     {@link FragmentNavigator#navigateTo(String, Fragment, Bundle, boolean, int)} could be
     *     called recursively to walk to interception Fragment, like authentication Fragment. If that case,
     *     {@link FragmentNavigator#continueFromInterception()} must be called in interception Fragment
     *     to tell FragmentManager where to go next after go back from interception Fragment</li>
     *     <li>Update TabBar by Fragment name</li>
     * </ul>
     *
     * @param name name of Fragment that is going to navigate to.
     * @param fragment Fragment that is going to navigate to.
     * @param arguments Arguments bundles to fragment that is going to navigate to.
     * @return
     */
    InterceptionResult onFragmentWillShow(String name, Fragment fragment, Bundle arguments);

    /**
     * This callback will be fired at the end of
     * {@link FragmentNavigator#navigateTo(String, Fragment, Bundle, boolean, int)}.
     *
     *
     * @param name
     * @param fragment
     * @return
     */
    void onFragmentShown(String name, Fragment fragment);

    /**
     * This callback will be fired when {@link FragmentNavigator#goBack()} is called.
     * Could do things like update TabBar.
     *
     * @param name
     * @param breadcrumbIndex
     */
    void onGoBack(String name, int breadcrumbIndex);

}
