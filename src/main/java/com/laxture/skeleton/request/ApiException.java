package com.laxture.skeleton.request;

import com.laxture.lib.task.TaskException;

public class ApiException extends TaskException {

    public static final int SUCCESSFUL = TaskException.ERROR_CODE_SUCCESSFUL;

    public static final int RESPONSE_DATA_FORMAT_ERROR = 7000001;

    public ApiException(int errorCode, String detailMessage,
                        Throwable throwable) {
        super(errorCode, detailMessage, throwable);
    }

    public ApiException(int errorCode, String detailMessage) {
        super(errorCode, detailMessage);
    }

    public ApiException(int errorCode, Throwable throwable) {
        super(errorCode, throwable);
    }

}
