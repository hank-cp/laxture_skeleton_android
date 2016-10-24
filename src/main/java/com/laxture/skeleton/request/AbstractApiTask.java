package com.laxture.skeleton.request;

import android.widget.Toast;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.laxture.lib.RuntimeContext;
import com.laxture.lib.connectivity.http.HttpTaskConfig;
import com.laxture.lib.connectivity.http.HttpTextTask;
import com.laxture.lib.task.TaskException;
import com.laxture.lib.task.TaskListener;
import com.laxture.lib.util.DeviceUtil;
import com.laxture.lib.util.LLog;
import com.laxture.skeleton.Constants;
import com.laxture.skeleton.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public abstract class AbstractApiTask<Result> extends HttpTextTask<Result> {

    private static JsonParser parser = new JsonParser();

    protected JsonObject resultJson;

    public AbstractApiTask(String url) {
        super(url, defaultHttpTaskConfig);
        init();
    }

    public AbstractApiTask(String url, HttpTaskConfig config) {
        super(url, config);
        init();
    }

    public abstract void init();

    @Override
    protected void processResponse(InputStream inputStream) throws IOException {
        super.processResponse(inputStream);

        JsonObject json = null;
        try {
            String str = getResponseText();
            LLog.d("Response :: %s", str);
            json = (JsonObject) parser.parse(str);
        } catch (JsonSyntaxException e) {
            LLog.w("JSON format error", e);
            setErrorDetails(new ApiException(ApiException.RESPONSE_DATA_FORMAT_ERROR,
                    RuntimeContext.getString(R.string.msg_http_err_server_error), e));
        }

        if (json != null) resultJson = json;
    }

    public JsonObject getResultJson() {
        return resultJson;
    }

    //*************************************************************************
    //  Default Task Listener
    //*************************************************************************

    public static HttpTaskConfig defaultHttpTaskConfig = new HttpTaskConfig();
    static {
        defaultHttpTaskConfig.maxRetryCount = 0;
        defaultHttpTaskConfig.headers = new HashMap<>();
        defaultHttpTaskConfig.headers.put("platform", Integer.toString(Constants.REQ_ARG_PLATFORM_ANDROID));
        defaultHttpTaskConfig.headers.put("appVersion", RuntimeContext.getVersionName());
        defaultHttpTaskConfig.headers.put("device", DeviceUtil.getDeviceId());
        defaultHttpTaskConfig.headers.put("os", android.os.Build.VERSION.RELEASE);
        defaultHttpTaskConfig.headers.put("osVersion", Integer.toString(android.os.Build.VERSION.SDK_INT));
    }

    // TODO show/hide progress indicator

    protected static DefaultTaskFailedListener defaultTaskFailedListener
            = new DefaultTaskFailedListener();
    public static class DefaultTaskFailedListener implements TaskListener.TaskFailedListener {
        @Override
        public void onTaskFailed(Object o, TaskException ex) {
            Toast.makeText(RuntimeContext.getApplication(), ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
