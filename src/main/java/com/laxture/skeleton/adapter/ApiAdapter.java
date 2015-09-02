package com.laxture.skeleton.adapter;

import android.annotation.SuppressLint;

import com.laxture.lib.task.TaskException;
import com.laxture.lib.task.TaskListener;
import com.laxture.lib.task.TaskManager;
import com.laxture.skeleton.request.AbstractApiTask;

import java.util.List;

@SuppressLint("ViewConstructor")
public abstract class ApiAdapter<T, ApiResult> extends PaginalAdapter<T>
        implements TaskListener.TaskFinishedListener<ApiResult>, TaskListener.TaskFailedListener<ApiResult> {

    @Override
    public void onTaskFinished(ApiResult result) {
        List<T> resultList = convertApiResultToData(result);

        switch (loadAction) {
            case Refresh:
                reloadData(true);
                onRefreshFromServerFinished(result);
                break;

            case FetchMore:
                appendData(resultList);
                checkHasMoreAfterFetchServerData(resultList);
                break;

            default: // do nothing
        }
        stopLoadingView(false);
        mLoading = false;
    }

    @Override
    public void onTaskFailed(ApiResult result, TaskException ex) {
        stopLoadingView(false);
        onLoadFailed(ex.getMessage());
        mLoading = false;
    }

    //*************************************************************************
    // Abstract Method
    //*************************************************************************

    protected abstract AbstractApiTask<ApiResult> createRefreshApiTask();

    protected abstract AbstractApiTask<ApiResult> createFetchMoreApiTask();

    protected abstract List<T> convertApiResultToData(ApiResult result);

    protected void onRefreshFromServerFinished(ApiResult result) {}

    //*************************************************************************
    // Internal Stuff
    //*************************************************************************

    @Override
    protected void refresh() {
        AbstractApiTask<ApiResult> apiTask = createRefreshApiTask();
        apiTask.addFinishedListener(this);
        apiTask.addFailedListener(this);
        TaskManager.runImmediately(apiTask);
    }

    @Override
    protected void fetchMoreFromServer() {
        AbstractApiTask<ApiResult> apiTask = createFetchMoreApiTask();
        apiTask.addFinishedListener(this);
        apiTask.addFailedListener(this);
        TaskManager.runImmediately(apiTask);
    }

}
