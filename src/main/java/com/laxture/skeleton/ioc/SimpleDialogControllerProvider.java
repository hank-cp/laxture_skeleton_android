package com.laxture.skeleton.ioc;

import android.app.Activity;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.laxture.lib.view.dialog.SimpleDialogController;

public class SimpleDialogControllerProvider implements Provider<SimpleDialogController> {

    @Inject
    protected Activity activity;

    public SimpleDialogController get() {
        return new SimpleDialogController(activity);
    }

}
