package com.laxture.skeleton.controller;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.SparseArray;

import com.laxture.lib.util.UnHandledException;

public class FragmentNavigator {

    public static final String BACK_STACK_HOME = "back_stack_home";

    private FragmentActivity mActivity;
    private FragmentController mController;

    private SparseArray<Fragment> mFragmentStack = new SparseArray<>();
    private SparseArray<String> mFragmentNames = new SparseArray<>();
    private int mBreadcrumbIndex;

    private boolean mIntercepted;
    private String mInterceptedFragmentName;
    private Fragment mInterceptedFragment;
    private Bundle mInterceptedArgument;
    private boolean mInterceptedIsBack;
    private int mInterceptedTransition;

    public FragmentNavigator(FragmentController controller) {
        if (controller == null) {
            throw new UnHandledException("FragmentController cannot be null");
        }
        mActivity = controller.getActivity();
        mController = controller;
    }

    public void navigateTo(String name) {
        navigateTo(name, null, true, FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
    }

    public void navigateTo(String name, Bundle argument) {
        navigateTo(name, argument, true, FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
    }

    public void navigateTo(String name, Bundle argument, boolean back) {
        navigateTo(name, argument, back, FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
    }

    public void navigateTo(String name, Bundle argument, boolean back, int transition) {
        Fragment nextFragment = mController.getFragment(name);
        navigateTo(name, nextFragment, argument, back, transition);
    }

    /**
     * Navigate to next Fragment.
     *
     * @param name
     * @param argument
     * @param back
     * @param transition
     */
    private void navigateTo(String name, Fragment fragment, Bundle argument, boolean back, int transition) {
        if (fragment == null) {
            throw new UnHandledException("Fragment cannot be null");
        }

        int tempBreadcrumbIndex = mBreadcrumbIndex;
        mIntercepted = mController.onFragmentWillShow(name, fragment);
        // Navigate to a new fragment during interception
        mIntercepted = mIntercepted && tempBreadcrumbIndex != mBreadcrumbIndex;
        if (mIntercepted) {
            mInterceptedFragmentName = name;
            mInterceptedFragment = fragment;
            mInterceptedArgument = argument;
            mInterceptedIsBack = back;
            mInterceptedTransition = transition;
            return;
        }

        FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
        ft.setTransition(transition);

        Fragment currentFragment = mFragmentStack.get(mBreadcrumbIndex);

        // set argument to Fragment, in case Fragment is reused.
        if (fragment.getArguments() == null) fragment.setArguments(new Bundle());
        if (argument != null) fragment.getArguments().putAll(argument);

        // Pop current Fragment
        if (currentFragment != null) {
            if (back) {
                // Add to back stack
                ft.addToBackStack(mBreadcrumbIndex == 0 ? BACK_STACK_HOME : null);
                mBreadcrumbIndex++;
            } else {
                // Reset back stack
                mActivity.getSupportFragmentManager().popBackStackImmediate(
                        BACK_STACK_HOME, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                currentFragment = mFragmentStack.get(0);
                mBreadcrumbIndex = 0;
                mFragmentStack.clear();
            }
            ft.detach(currentFragment);
        }

        // save breadcrumb state
        mFragmentStack.append(mBreadcrumbIndex, fragment);
        mFragmentNames.append(mBreadcrumbIndex, name);

        // push new Fragment
        if (mActivity.getSupportFragmentManager().findFragmentByTag(name) == null)
            ft.add(android.R.id.tabcontent, fragment, name);
        else ft.attach(fragment);
        ft.commit();
        mActivity.getSupportFragmentManager().executePendingTransactions();

        mController.onFragmentShown(name, fragment);
    }

    /**
     * Go back from current to last Fragment if there is any.
     */
    public boolean goBack() {
        if (mBreadcrumbIndex <= 0) return false;

        // pop current Fragment
        mBreadcrumbIndex--;

        mController.onGoBack(mFragmentNames.get(mBreadcrumbIndex), mBreadcrumbIndex);

        // clear interception kept info
        if (mIntercepted) {
            mIntercepted = false;
            mInterceptedFragmentName = null;
            mInterceptedFragment = null;
            mInterceptedArgument = null;
            mInterceptedTransition = FragmentTransaction.TRANSIT_NONE;
        }

        mActivity.getSupportFragmentManager().popBackStackImmediate();
        return true;
    }

    /**
     * reset back stack to root
     *
     * @return
     */
    public void goBackToRoot() {
        if (mBreadcrumbIndex <= 0) return;

        mActivity.getSupportFragmentManager().popBackStackImmediate(
                BACK_STACK_HOME, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    public boolean currentOnRoot() {
        return mBreadcrumbIndex == 0;
    }

    /**
     * Continue to original forward Fragment from Interception Fragment.
     *
     * @return
     */
    public boolean continueFromInterception() {
        if (!mIntercepted) return goBack();

        if (mBreadcrumbIndex > 0) {
            // pop current Fragment
            mBreadcrumbIndex--;
            mController.onGoBack(mFragmentNames.get(mBreadcrumbIndex), mBreadcrumbIndex);
            mActivity.getSupportFragmentManager().popBackStackImmediate();
        }

        String interceptedFragmentName = mInterceptedFragmentName;
        Fragment interceptedFragment = mInterceptedFragment;
        Bundle interceptedArgument = mInterceptedArgument;
        boolean interceptedIsBack = mInterceptedIsBack;
        int interceptedTransition = mInterceptedTransition;

        mIntercepted = false;
        mInterceptedFragmentName = null;
        mInterceptedFragment = null;
        mInterceptedArgument = null;
        mInterceptedTransition = FragmentTransaction.TRANSIT_NONE;

        navigateTo(interceptedFragmentName, interceptedFragment,
                interceptedArgument, interceptedIsBack, interceptedTransition);

        return true;
    }

    public boolean isIntercepted() {
        return mIntercepted;
    }

    public interface FragmentNavigatorGetter {
        FragmentNavigator getFragmentNavigator();
    }
}
