package com.laxture.skeleton.controller;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.util.SparseArray;

import com.laxture.lib.util.UnHandledException;
import com.laxture.skeleton.Constants;
import com.laxture.skeleton.controller.FragmentController.InterceptionResult;

public class FragmentNavigator {

    public static final String BACK_STACK_HOME = "back_stack_home";
    public static final int REQUEST_CODE_NAVIGATOR = Integer.MAX_VALUE / 2 + 13; // random magic code

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
     */
    private void navigateTo(String name, Fragment fragment, Bundle argument, boolean back, int transition) {
        if (fragment == null) {
            throw new UnHandledException("Fragment cannot be null");
        }

        InterceptionResult interceptionResult = mController.onFragmentWillShow(name, fragment, argument);
        switch (interceptionResult) {
            case Insert: {
                mIntercepted = true;
                mIntercepteeFragmentName = name;
                mIntercepteeFragment = fragment;
                mIntercepteeArgument = argument;
                mInterceptedIsBack = back;
                mIntercepteeTransition = transition;
                return;
            }
            case Interrupted: {
                return;
            }
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

        // show DialogFragment if it is
        if (fragment instanceof DialogFragment) {
            ((DialogFragment) fragment).show(mActivity.getSupportFragmentManager(), name);
            return;
        }

        // push new Fragment
        if (mActivity.getSupportFragmentManager().findFragmentByTag(name) == null)
            ft.add(android.R.id.tabcontent, fragment, name);
        else {
            Fragment managedFragment = mActivity.getSupportFragmentManager().findFragmentByTag(name);
            if (managedFragment == fragment) ft.attach(fragment);
            else {
                ft.replace(managedFragment.getId(), fragment);
//                ft.detach(managedFragment);
//                ft.add(android.R.id.tabcontent, fragment, name);
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
        return goBack(null);
    }

    /**
     * Go back from current to last Fragment if there is any. Allow passing arguments
     *
     * <pre>
         public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (requestCode == REQUEST_CODE_NAVIGATOR && resultCode == Activity.RESULT_OK
                && data != null) {
                // do something here...
            }
         }
     * </pre>
     *
     */
    public boolean goBack(Bundle backArgs) {
        if (mBreadcrumbIndex <= 0) return false;

        Fragment currentFragment = mFragmentStack.get(mBreadcrumbIndex);
        String currentFragmentName = mFragmentNames.get(mBreadcrumbIndex);
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

        if (currentFragment instanceof DialogFragment) {
            ((DialogFragment) currentFragment).dismiss();
        } else {
            try {
                mActivity.getSupportFragmentManager().popBackStackImmediate();
                if (backArgs != null) {
                    Intent intent = new Intent();
                    intent.putExtras(backArgs);
                    intent.putExtra(Constants.ARGUMENT_BACK_FROM_FRAGMENT, currentFragmentName);
                    getTopFragment().onActivityResult(
                        REQUEST_CODE_NAVIGATOR, Activity.RESULT_OK, intent);
                }
            } catch (IllegalStateException ignored) {}
        }
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
            Fragment currentFragment = mFragmentStack.get(mBreadcrumbIndex);
            // pop current Fragment
            mBreadcrumbIndex--;
            mController.onGoBack(mFragmentNames.get(mBreadcrumbIndex), mBreadcrumbIndex);
            if (currentFragment instanceof DialogFragment) {
                ((DialogFragment) currentFragment).dismiss();
            } else {
                mActivity.getSupportFragmentManager().popBackStackImmediate();
            }
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
     * Cancel Interception state. When Activity is launched for interception,
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
