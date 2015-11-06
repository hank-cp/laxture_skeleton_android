package com.laxture.skeleton.updater;

import com.laxture.lib.RuntimeContext;
import com.laxture.lib.cache.storage.TempStorage;
import com.laxture.lib.connectivity.http.HttpDownloadTask;

import java.io.File;

public class ApkDownloadTask extends HttpDownloadTask<File> {

    public ApkDownloadTask(String url, String version) {
        super(url, new TempStorage(RuntimeContext.getPackageName()+"_"+version+".apk").getFile());
        enableResumeFromBreakPoint = true;
    }

    @Override
    protected File generateResult() {
        return getDownloadFile();
    }

}
