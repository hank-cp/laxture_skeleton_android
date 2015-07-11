package com.laxture.skeleton.adapter;

import android.annotation.SuppressLint;

import com.laxture.lib.task.TaskException;
import com.laxture.lib.task.TaskListener;
import com.laxture.lib.task.TaskManager;
import com.laxture.skeleton.request.AbstractApiTask;

import java.util.List;

@SuppressLint("ViewConstructor")
public abstract class ApiAdapter<T, ApiResult> extends PaginalAdapter<T> {

    private AbstractApiTask<ApiResult> mApiTask;

    public ApiAdapter(AbstractApiTask<ApiResult> apiTask) {
        mApiTask = apiTask;

        mApiTask.addFinishedListener(new TaskListener.TaskFinishedListener<ApiResult>() {
            @Override
            public void onTaskFinished(ApiResult result) {
                List<T> resultList = convertApiResultToData(result);
                onProcessDataCompleted(resultList);

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
                stopLoadingView();
                mLoading = false;
            }
        });

        mApiTask.addFailedListener(new TaskListener.TaskFailedListener() {
            @Override
            public void onTaskFailed(Object o, TaskException ex) {
                stopLoadingView();
                onLoadFailed(ex.getMessage());
                mLoading = false;
            }
        });
    }

    //*************************************************************************
    // Task Delegate Method
    //*************************************************************************

    public void addArgument(String key, Object value) {
        mApiTask.addArgument(key, value.toString());
    }

    public void addArgument(String key, int value) {
        mApiTask.addArgument(key, Integer.toString(value));
    }

    public void addArgument(String key, boolean value) {
        mApiTask.addArgument(key, Boolean.toString(value));
    }

    public void addArgument(String key, long value) {
        mApiTask.addArgument(key, Long.toString(value));
    }

    public void addArgument(String key, float value) {
        mApiTask.addArgument(key, Float.toString(value));
    }

    public void addArgument(String key, double value) {
        mApiTask.addArgument(key, Double.toString(value));
    }

    public void addStartListener(TaskListener.TaskStartListener callback) {
        mApiTask.addStartListener(callback);
    }

    public void addProgressUpdatedListener(TaskListener.TaskProgressUpdatedListener callback) {
        mApiTask.addProgressUpdatedListener(callback);
    }

    public void addFinishedListener(TaskListener.TaskFinishedListener<ApiResult> callback) {
        mApiTask.addFinishedListener(callback);
    }

    public void addCancelledListener(TaskListener.TaskCancelledListener<ApiResult> callback) {
        mApiTask.addCancelledListener(callback);
    }

    public void addFailedListener(TaskListener.TaskFailedListener callback) {
        mApiTask.addFailedListener(callback);
    }

    public void addDataChangedListener(TaskListener.TaskDataChangedListener callback) {
        mApiTask.addDataChangedListener(callback);
    }

    public boolean isCancelled() {
        return mApiTask.isCancelled();
    }

    //*************************************************************************
    // Abstract Method
    //*************************************************************************

    protected abstract void setupRefreshParams(AbstractApiTask<ApiResult> apiTask);

    protected abstract void setupFetchMoreParams(AbstractApiTask<ApiResult> apiTask);

    protected abstract List<T> convertApiResultToData(ApiResult result);

    protected void onRefreshFromServerFinished(ApiResult result) {}

    protected void onProcessDataCompleted(List<T> resultList) {}

    //*************************************************************************
    // Internal Stuff
    //*************************************************************************

    @Override
    protected void refresh() {
        setupRefreshParams(mApiTask);
        TaskManager.runImmediately(mApiTask);
    }

    @Override
    protected void fetchMoreFromServer() {
        setupFetchMoreParams(mApiTask);
        TaskManager.runImmediately(mApiTask);
    }

}
