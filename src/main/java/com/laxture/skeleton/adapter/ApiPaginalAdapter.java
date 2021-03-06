package com.laxture.skeleton.adapter;

import android.annotation.SuppressLint;

import com.laxture.lib.java8.Consumer;
import com.laxture.lib.task.TaskException;
import com.laxture.lib.task.TaskListener;
import com.laxture.lib.task.TaskManager;
import com.laxture.skeleton.request.AbstractApiTask;

import java.util.List;

@SuppressLint("ViewConstructor")
public abstract class ApiPaginalAdapter<T, ApiResult> extends PaginalAdapter<T> implements
        TaskListener.TaskStartListener,
        TaskListener.TaskFinishedListener<ApiResult>,
        TaskListener.TaskFailedListener<ApiResult> {

    private Consumer<Long> mTotalConsumer;

    @Override
    public void onTaskStart() {
        setLoadingView(LoadAction.Refresh);
    }

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

    protected abstract void onLoadFailed(String errorMessage);

    protected void onRefreshFromServerStart() {}

    protected void onRefreshFromServerFinished(ApiResult result) {}

    public void setTotalConsumer(Consumer<Long> totalConsumer) {
        this.mTotalConsumer = totalConsumer;
    }

    //*************************************************************************
    // Internal Stuff
    //*************************************************************************

    @Override
    protected void refresh() {
        final AbstractApiTask<ApiResult> apiTask = createRefreshApiTask();
        apiTask.addStartListener(this);
        apiTask.addFinishedListener(this);
        if (mTotalConsumer != null) {
            apiTask.addFinishedListener(new TaskListener.TaskFinishedListener<ApiResult>() {
                @Override
                public void onTaskFinished(ApiResult apiResult) {
                    mTotalConsumer.consume(apiTask.getTotal());
                }
            });
        }
        apiTask.addFailedListener(this);
        onRefreshFromServerStart();
        TaskManager.runImmediately(apiTask);
    }

    @Override
    protected void fetchMoreFromServer() {
        final AbstractApiTask<ApiResult> apiTask = createFetchMoreApiTask();
        apiTask.addStartListener(this);
        apiTask.addFinishedListener(this);
        if (mTotalConsumer != null) {
            apiTask.addFinishedListener(new TaskListener.TaskFinishedListener<ApiResult>() {
                @Override
                public void onTaskFinished(ApiResult apiResult) {
                    mTotalConsumer.consume(apiTask.getTotal());
                }
            });
        }
        apiTask.addFailedListener(this);
        TaskManager.runImmediately(apiTask);
    }

}
