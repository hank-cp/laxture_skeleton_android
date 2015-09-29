package com.laxture.skeleton.controller;

import android.app.Activity;
import android.view.KeyEvent;

/**
 * Handle key pressed event in Fragment.
 */
public interface FragmentKeyPressedListener {

    /**
     * {@link Activity#onKeyDown(int, KeyEvent)}
     */
    boolean onKeyDown(int keyCode, KeyEvent event);

    /**
     * {@link Activity#onBackPressed()}
     *
     * @return true - block back key event propagating to Activity <br/>
     *         false - continue key event propagating to Activity
     */
    boolean onBackPressed();
}
