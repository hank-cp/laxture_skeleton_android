package com.laxture.skeleton.updater;

import com.laxture.skeleton.request.AbstractApiTask;
import com.laxture.skeleton.updater.VersionUpdater.VersionInfo;
import com.laxture.skeleton.util.GsonUtil;

public class CheckUpdateTask extends AbstractApiTask<VersionInfo> {

    public CheckUpdateTask(String checkUpdateUrl) {
        super(checkUpdateUrl);
    }

    @Override
    public void init() {}

    @Override
    protected VersionInfo generateResult() {
        return GsonUtil.fromJson(getResponseText(), VersionInfo.class);
    }

}
