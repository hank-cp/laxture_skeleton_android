package com.laxture.skeleton.ioc;

import com.laxture.skeleton.controller.FragmentController;
import com.laxture.skeleton.controller.FragmentNavigator;
import com.google.inject.AbstractModule;
import com.laxture.lib.view.dialog.SimpleDialogController;

@SuppressWarnings("unused")
public class CustomInjectionModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(SimpleDialogController.class).toProvider(SimpleDialogControllerProvider.class);
        bind(FragmentNavigator.class).toProvider(FragmentNavigatorProvider.class);
    }

}
