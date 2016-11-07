package com.laxture.skeleton.adapter;

import android.os.Build;
import android.widget.ArrayAdapter;

import com.laxture.lib.RuntimeContext;
import com.laxture.lib.task.TaskException;
import com.laxture.lib.task.TaskListener;
import com.laxture.lib.task.TaskManager;
import com.laxture.skeleton.request.AbstractApiTask;

import java.util.List;

public abstract class ApiAdapter<T, ApiResult> extends ArrayAdapter<T>
        implements TaskListener.TaskFinishedListener<ApiResult>,
                   TaskListener.TaskFailedListener<ApiResult> {

    public ApiAdapter() {
        super(RuntimeContext.getApplication(), 0);
    }

    @Override
    public void onTaskFinished(ApiResult result) {
        List<T> resultList = convertApiResultToData(result);
        setArrayData(resultList);
    }

    @Override
    public void onTaskFailed(ApiResult result, TaskException ex) {
        onLoadFailed(ex.getMessage());
    }

    void setArrayData(List<T> data) {
        clear();
        if (Build.VERSION.SDK_INT > 10) {
            addAll(data);
        } else {
            for (T item : data) {
                add(item);
            }
        }
    }

    //*************************************************************************
    // Public Method
    //*************************************************************************

    public void loadData() {
        AbstractApiTask<ApiResult> apiTask = createApiTask();
        apiTask.addFinishedListener(this);
        TaskManager.runImmediately(apiTask);
    }

    //*************************************************************************
    // Abstract Method
    //*************************************************************************

    protected abstract AbstractApiTask<ApiResult> createApiTask();

    protected abstract void onLoadFailed(String errorMessage);

    protected abstract List<T> convertApiResultToData(ApiResult result);

}
