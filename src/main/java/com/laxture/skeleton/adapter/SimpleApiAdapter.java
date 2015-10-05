package com.laxture.skeleton.adapter;

import android.os.Build;
import android.widget.ArrayAdapter;

import com.laxture.lib.RuntimeContext;
import com.laxture.lib.task.AbstractTask;
import com.laxture.lib.task.TaskListener;
import com.laxture.lib.task.TaskManager;
import com.laxture.skeleton.request.AbstractApiTask;

import java.util.List;

public abstract class SimpleApiAdapter<T, ApiResult> extends ArrayAdapter<T> {

    private AbstractApiTask<ApiResult> mApiTask;

    public SimpleApiAdapter(AbstractApiTask<ApiResult> apiTask) {
        super(RuntimeContext.getApplication(), 0);
        mApiTask = apiTask;

        mApiTask.addFinishedListener(new TaskListener.TaskFinishedListener<ApiResult>() {
            @Override
            public void onTaskFinished(ApiResult result) {
                List<T> resultList = convertApiResultToData(result);

                clear();
                if (Build.VERSION.SDK_INT > 10) {
                    addAll(resultList);
                } else {
                    for (T item : resultList) {
                        add(item);
                    }
                }
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
        mApiTask.addArgument(key, value);
    }

    public void addArgument(String key, boolean value) {
        mApiTask.addArgument(key, value);
    }

    public void addArgument(String key, long value) {
        mApiTask.addArgument(key, value);
    }

    public void addArgument(String key, float value) {
        mApiTask.addArgument(key, value);
    }

    public void addArgument(String key, double value) {
        mApiTask.addArgument(key, value);
    }

    public void addArgument(String key, long[] value) {
        mApiTask.addArgument(key, value);
    }

    public void addArgument(String key, String[] value) {
        mApiTask.addArgument(key, value);
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

    public AbstractTask.State getState() {
        return mApiTask.getState();
    }

    //*************************************************************************
    // Public Method
    //*************************************************************************

    public void loadData() {
        TaskManager.runImmediately(mApiTask);
    }

    //*************************************************************************
    // Abstract Method
    //*************************************************************************

    protected abstract List<T> convertApiResultToData(ApiResult result);

}
