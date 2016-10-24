package com.laxture.skeleton.controller;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
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
    private String mIntercepteeFragmentName;
    private Fragment mIntercepteeFragment;
    private Bundle mIntercepteeArgument;
    private boolean mInterceptedIsBack;
    private int mIntercepteeTransition;

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

        if (fragment instanceof DialogFragment) {
            ((DialogFragment) fragment).show(mActivity.getSupportFragmentManager(), name);
            return;
        }

        mIntercepted = mController.onFragmentWillShow(name, fragment, argument);
        if (mIntercepted) {
            mIntercepteeFragmentName = name;
            mIntercepteeFragment = fragment;
            mIntercepteeArgument = argument;
            mInterceptedIsBack = back;
            mIntercepteeTransition = transition;
            return;
        }

        FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
        ft.setTransition(transition);

        Fragment currentFragment = getTopFragment();

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
        else {
            Fragment managedFragment = mActivity.getSupportFragmentManager().findFragmentByTag(name);
            if (managedFragment == fragment) ft.attach(fragment);
            else {
                ft.detach(managedFragment);
                ft.add(android.R.id.tabcontent, fragment, name);
            }
        }
        ft.commit();
        mActivity.getSupportFragmentManager().executePendingTransactions();

        mController.onFragmentShown(name, fragment);
    }

    public Fragment getTopFragment() {
        return mFragmentStack.get(mBreadcrumbIndex);
    }

    /**
     * Go back from current to last Fragment if there is any.
     */
    public boolean goBack() {
        if (mBreadcrumbIndex <= 0) return false;

        // pop current Fragment
        mBreadcrumbIndex--;
        mFragmentStack.removeAt(mFragmentStack.size()-1);

        mController.onGoBack(mFragmentNames.get(mBreadcrumbIndex), mBreadcrumbIndex);

        // clear interception kept info
        if (mIntercepted) {
            mIntercepted = false;
            mIntercepteeFragmentName = null;
            mIntercepteeFragment = null;
            mIntercepteeArgument = null;
            mIntercepteeTransition = FragmentTransaction.TRANSIT_NONE;
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

        mBreadcrumbIndex = 0;
        while (mFragmentStack.size() > 1) mFragmentStack.removeAt(1);

        mController.onGoBack(mFragmentNames.get(mBreadcrumbIndex), mBreadcrumbIndex);

        mActivity.getSupportFragmentManager().popBackStackImmediate(
                BACK_STACK_HOME, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    public boolean currentOnRoot() {
        return mBreadcrumbIndex == 0;
    }

    /**
     * {@link #continueFromInterception(Bundle)}
     *
     * @return
     */
    public boolean continueFromInterception() {
        return continueFromInterception(null);
    }

    /**
     * Continue to original forward Fragment from Interception Fragment.
     *
     * @param argumentFromInterceptor argument return by interceptor that will be
     *                                injected to interceptee.
     *
     * @return
     */
    public boolean continueFromInterception(Bundle argumentFromInterceptor) {
        if (!mIntercepted) return goBack();

        if (mBreadcrumbIndex > 0) {
            // pop current Fragment
            mBreadcrumbIndex--;
            mController.onGoBack(mFragmentNames.get(mBreadcrumbIndex), mBreadcrumbIndex);
            mActivity.getSupportFragmentManager().popBackStackImmediate();
        }

        String intercepteeFragmentName = mIntercepteeFragmentName;
        Fragment intercepteeFragment = mIntercepteeFragment;
        Bundle intercepteeArgument = mIntercepteeArgument;
        boolean intercepteeIsBack = mInterceptedIsBack;
        int intercepteeTransition = mIntercepteeTransition;

        mIntercepted = false;
        mIntercepteeFragmentName = null;
        mIntercepteeFragment = null;
        mIntercepteeArgument = null;
        mIntercepteeTransition = FragmentTransaction.TRANSIT_NONE;

        if (argumentFromInterceptor != null) {
            intercepteeArgument.putAll(argumentFromInterceptor);
        }

        navigateTo(intercepteeFragmentName, intercepteeFragment,
                intercepteeArgument, intercepteeIsBack, intercepteeTransition);

        return true;
    }

    /**
     * Cancel Interception state. When Activity is luanched for interception,
     * and result with cancel, call this method to clear interception state.
     *
     * @return
     */
    public void cancelInterceptionState() {
        mIntercepted = false;
        mIntercepteeFragmentName = null;
        mIntercepteeFragment = null;
        mIntercepteeArgument = null;
        mIntercepteeTransition = FragmentTransaction.TRANSIT_NONE;
    }

    public boolean isIntercepted() {
        return mIntercepted;
    }

    public interface FragmentNavigatorGetter {
        FragmentNavigator getFragmentNavigator();
    }
}
