package com.laxture.skeleton.updater;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import androidx.core.app.NotificationCompat;

import com.laxture.lib.RuntimeContext;
import com.laxture.lib.task.TaskException;
import com.laxture.lib.task.TaskListener;
import com.laxture.lib.task.TaskManager;
import com.laxture.lib.util.IntentUtil;
import com.laxture.lib.util.LLog;
import com.laxture.lib.util.UnHandledException;
import com.laxture.skeleton.PrefKeys;
import com.laxture.skeleton.R;
import com.laxture.skeleton.view.dialog.DialogController;
import com.laxture.skeleton.view.dialog.DialogController.DialogActionHandler;

import org.joda.time.DateTime;

import java.io.File;

public class VersionUpdater {

    public static class VersionInfo {
        public String mainVersion;
        public int buildNum;
        public String url;
        @Deprecated
        public boolean force;
        // all version code under this version should be force updated
        public int forceUpdateUnderBuildNum;
        public String[] features;

        // this field is not from JSON, but used to keep the version
        // comparing result.
        public boolean hasUpdate;

        public String getVersionName() {
            return mainVersion+"."+buildNum;
        }

        public String getFeaturesWords() {
            if (features == null) return "";
            StringBuilder sb = new StringBuilder();
            for (String feature : features) {
                sb.append(feature).append("\n");
            }
            return sb.toString();
        }
    }

    private static VersionUpdater sInstance;

    private VersionUpdater() {
        mNotificationManager = RuntimeContext.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static VersionUpdater getInstance() {
        if (sInstance == null) {
            sInstance = new VersionUpdater();
        }
        return sInstance;
    }

    // Config Properties
    public String mUpdateSiteUrl;
    public CheckUpdateTask mCheckUpdateTask;
    public ApkDownloadTask mApkDownloadTask;

    // State Properties
    private VersionInfo mVersionInfo;
    private int mPreviousProgress;
    private volatile String mPendingDialogTag;

    // UI Properties
    private Activity mActvity;
    private ApkUpdaterDialogController mDialogController;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mDownloadNotificationBuilder;
    private CheckUpdateListener mCheckUpdateListener;

    //*************************************************************************
    //  Config Setter
    //*************************************************************************

    public void setUpdateSiteUrl(String updateSiteUrl) {
        mUpdateSiteUrl = updateSiteUrl;
    }

    public void setCheckUpdateListener(CheckUpdateListener listener) {
        mCheckUpdateListener = listener;
    }

    public void bindToActivity(Activity activity) {
        mActvity = activity;
        mDialogController = new ApkUpdaterDialogController(activity);

        if (mPendingDialogTag != null) {
            LLog.d("Show PendingDialog %s", sInstance.mPendingDialogTag);
            mDialogController.showDialog(mPendingDialogTag);
            mPendingDialogTag = null;
        }
    }

    //*************************************************************************
    //  Public Method (all are static)
    //*************************************************************************

    public void checkUpdate(boolean force) {
//        if (BuildConfig.DEBUG) return;

        SharedPreferences pref = RuntimeContext.getSharedPreferences();
        if (!force && pref.getLong(PrefKeys.PREF_KEY_NEXT_UPDATE_DATE, 0) > System.currentTimeMillis()) return;

        if (isCheckingUpdate() || isDownloadingApk()) return;

        // release previous task
        if (mCheckUpdateTask != null)
            mCheckUpdateTask.removeAllTaskListeners();

        mCheckUpdateTask = new CheckUpdateTask(mUpdateSiteUrl);
        mCheckUpdateTask.addStartListener(new TaskListener.TaskStartListener() {
            @Override
            public void onTaskStart() {
                if (mCheckUpdateListener != null)
                    mCheckUpdateListener.onStartCheckingUpdate();
            }
        });
        mCheckUpdateTask.addFinishedListener(new TaskListener.TaskFinishedListener<VersionInfo>() {
            @Override
            public void onTaskFinished(VersionInfo result) {
                mVersionInfo = result;

                LLog.d("Receive latest version %s from server. force=%s", result.getVersionName(), result.forceUpdateUnderBuildNum);

                // has update
                if (result.buildNum > RuntimeContext.getVersionCode()) {
                    Editor editor = RuntimeContext.getSharedPreferences().edit();
                    editor.putString(PrefKeys.PREF_KEY_UPDATE_MAIN_VERSION, mVersionInfo.mainVersion);
                    editor.putInt(PrefKeys.PREF_KEY_UPDATE_BUILD_NUM, mVersionInfo.buildNum);
                    editor.putString(PrefKeys.PREF_KEY_UPDATE_URL, mVersionInfo.url);
                    editor.putBoolean(PrefKeys.PREF_KEY_UPDATE_FORCE, RuntimeContext.getVersionCode() < result.forceUpdateUnderBuildNum);
                    editor.commit();
                    resetCheckUpdate();
                    mDialogController.showDialog(DIALOG_DOWNLOAD, result);

                    if (mCheckUpdateListener != null)
                        mCheckUpdateListener.onCompleteWithHasUpdate();

                    // no update
                } else {
                    if (mCheckUpdateListener != null)
                        mCheckUpdateListener.onCompleteWithNoUpdate();
                    resetCheckUpdate();
                    release();
                }
            }
        });
        mCheckUpdateTask.addFailedListener(new TaskListener.TaskFailedListener<VersionInfo>() {
            @Override
            public void onTaskFailed(VersionInfo versionInfo, TaskException ex) {
                LLog.e("Check update APK version failed.");
                resetCheckUpdate();
                if (mCheckUpdateListener != null)
                    mCheckUpdateListener.onFailedCheckingUpdate();
            }
        });

        TaskManager.runImmediately(sInstance.mCheckUpdateTask);
    }

    public void downloadUpdate() {
        if (isDownloadingApk()) return;

        SharedPreferences pref = RuntimeContext.getSharedPreferences();
        if (!pref.contains(PrefKeys.PREF_KEY_UPDATE_URL)) return;

        mVersionInfo = new VersionInfo();
        mVersionInfo.url = pref.getString(PrefKeys.PREF_KEY_UPDATE_URL, "");
        mVersionInfo.mainVersion = pref.getString(PrefKeys.PREF_KEY_UPDATE_MAIN_VERSION, "");
        mVersionInfo.buildNum = pref.getInt(PrefKeys.PREF_KEY_UPDATE_BUILD_NUM, 0);
        startDownloadApk();
    }


    public boolean isCheckingUpdate() {
        return mCheckUpdateTask != null;
    }

    public boolean isDownloadingApk() {
        return mApkDownloadTask != null;
    }

    private void resetCheckUpdate() {
        mCheckUpdateTask.removeAllTaskListeners();
        mCheckUpdateTask = null;
    }

    private void resetDownloadApk() {
        mApkDownloadTask.removeAllTaskListeners();;
        mApkDownloadTask = null;

    }

    private static void release() {
        sInstance.mDialogController = null;
        sInstance = null;
    }

    /**
     * Call this method when Application start.
     */
    @SuppressLint("CommitPrefEdits")
    public static void postVersionUpdated(AppVersionUpdatedListener listener) {
        SharedPreferences pref = RuntimeContext.getSharedPreferences();
        String currentVersion = RuntimeContext.getVersionName();
        String installedVersion = pref.getString(PrefKeys.PREF_KEY_INSTALLED_VERSION, null);
        Editor editor = pref.edit();
        if (null != installedVersion && !currentVersion.equals(installedVersion)) {
            // Update successfully, clear & update data in Preference.
            editor.remove(PrefKeys.PREF_KEY_UPDATE_MAIN_VERSION);
            editor.remove(PrefKeys.PREF_KEY_UPDATE_BUILD_NUM);
            editor.remove(PrefKeys.PREF_KEY_UPDATE_URL);
            editor.remove(PrefKeys.PREF_KEY_UPDATE_FORCE);
            if (listener != null) listener.onVersionUpdated(installedVersion, currentVersion);
        }
        editor.putString(PrefKeys.PREF_KEY_INSTALLED_VERSION, currentVersion);
        editor.commit();
    }

    public interface AppVersionUpdatedListener {
        void onVersionUpdated(String oldVersionName, String newVersionName);
    }

    //*************************************************************************
    //  APK Download TaskListener
    //*************************************************************************

    private void startDownloadApk() {
        mNotificationManager.cancel(R.id.notification_download_failed);

        mApkDownloadTask = new ApkDownloadTask(mVersionInfo.url, mVersionInfo.getVersionName());
        mApkDownloadTask.addProgressUpdatedListener(new TaskListener.TaskProgressUpdatedListener() {
            @Override
            public void onTaskProgressUpdated(int totalSize, int currentSize) {
                if (currentSize - mPreviousProgress > totalSize*0.02) {
                    mDownloadNotificationBuilder.setProgress(totalSize, currentSize, false);
                    mNotificationManager.notify(R.id.notification_downloading, mDownloadNotificationBuilder.build());
                    mPreviousProgress = currentSize;
                }
            }
        });
        mApkDownloadTask.addFinishedListener(new TaskListener.TaskFinishedListener<File>() {
            @Override
            public void onTaskFinished(File file) {
                // cancel downloading notification
                mNotificationManager.cancel(R.id.notification_downloading);

                // show download done notification
                String title = RuntimeContext.getString(R.string.title_download_done);
                String text = RuntimeContext.getString(R.string.msg_download_done,
                        RuntimeContext.getString(R.string.app_name));
                NotificationCompat.Builder builder = new NotificationCompat.Builder(
                        RuntimeContext.getApplication());
                builder.setAutoCancel(true);
                builder.setSmallIcon(android.R.drawable.stat_sys_download_done);
                builder.setContentTitle(title);
                builder.setContentText(text);
                builder.setTicker(text);
                PendingIntent contentIntent = PendingIntent.getActivity(
                        RuntimeContext.getApplication(), 0,
                        IntentUtil.getApkInstallerIntent(file),
                        PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(contentIntent);
                mNotificationManager.notify(R.id.notification_download_done, builder.build());

                mDialogController.showDialog(DIALOG_INSTALL);
            }
        });
        mApkDownloadTask.addFailedListener(new TaskListener.TaskFailedListener<File>() {
            @Override
            public void onTaskFailed(File file, TaskException ex) {
                LLog.e("Download latest version of APK from failed.");

                // cancel downloading notification
                mNotificationManager.cancel(R.id.notification_downloading);

                // show download failed notification
                String title = RuntimeContext.getString(R.string.title_download_failed);
                String text = RuntimeContext.getString(R.string.msg_download_failed,
                        RuntimeContext.getString(R.string.app_name));
                NotificationCompat.Builder builder = new NotificationCompat.Builder(
                        RuntimeContext.getApplication());
                builder.setAutoCancel(true);
                builder.setSmallIcon(android.R.drawable.stat_sys_warning);
                builder.setContentTitle(title);
                builder.setContentText(text);
                builder.setTicker(text);
                PendingIntent contentIntent = PendingIntent.getActivity(
                        RuntimeContext.getApplication(), 0,
                        IntentUtil.getBrowserIntent(mUpdateSiteUrl),
                        PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(contentIntent);
                mNotificationManager.notify(R.id.notification_download_failed, builder.build());

                mDialogController.showDialog(DIALOG_DOWNLOAD_FAILED);
                resetDownloadApk();
            }
        });

        mDownloadNotificationBuilder = new NotificationCompat.Builder(RuntimeContext.getApplication());
        mDownloadNotificationBuilder.setSmallIcon(android.R.drawable.stat_sys_download);
        mDownloadNotificationBuilder.setContentTitle(mApkDownloadTask.getDownloadFile().getName());
        mDownloadNotificationBuilder.setContentIntent(PendingIntent.getActivity(RuntimeContext.getApplication(), 0, new Intent(), 0));
        mDownloadNotificationBuilder.setProgress(100, 0, false);
        mDownloadNotificationBuilder.setWhen(0);
        mDownloadNotificationBuilder.setOngoing(true);

        mNotificationManager.notify(R.id.notification_downloading, mDownloadNotificationBuilder.build());

        TaskManager.runImmediately(mApkDownloadTask);
    }

    //*************************************************************************
    //  Dialog
    //*************************************************************************

    private static final String DIALOG_DOWNLOAD = "dialog_download";
    private static final String DIALOG_INSTALL = "dialog_install";
    private static final String DIALOG_DOWNLOAD_FAILED = "dialog_downloadFailed";

    private class ApkUpdaterDialogController extends DialogController {

        public ApkUpdaterDialogController(Activity activity) {
            super(activity);
        }

        @Override
        public Dialog prepareDialog(String dialogTag, Object... params) {
            if (DIALOG_DOWNLOAD.equals(dialogTag)) {
                VersionInfo versionInfo = (VersionInfo) params[0];
                String updateMsg = RuntimeContext.getVersionCode() < versionInfo.forceUpdateUnderBuildNum
                        ? RuntimeContext.getString(R.string.msg_forceUpdate)
                        : RuntimeContext.getString(R.string.msg_haveUpdate,
                        versionInfo.getVersionName(), versionInfo.getFeaturesWords());
                return getYesNoDialog(dialogTag,
                        RuntimeContext.getString(R.string.title_haveUpdate),
                        updateMsg,
                        RuntimeContext.getString(R.string.label_download),
                        RuntimeContext.getString(R.string.label_later), true,
                        mDownloadDialogCallback);

            } else if (DIALOG_INSTALL.equals(dialogTag)) {
                return getYesNoDialog(dialogTag,
                        RuntimeContext.getString(R.string.title_haveUpdate),
                        RuntimeContext.getString(R.string.msg_install),
                        RuntimeContext.getString(R.string.label_install),
                        RuntimeContext.getString(R.string.label_later),
                        true, mInstallDialogCallback);

            } else if (DIALOG_DOWNLOAD_FAILED.equals(dialogTag)) {
                return getYesNoCancelDialog(dialogTag,
                        RuntimeContext.getString(R.string.title_haveUpdate),
                        RuntimeContext.getString(R.string.msg_download_failed),
                        RuntimeContext.getString(R.string.label_download_again),
                        RuntimeContext.getString(R.string.label_download_site),
                        RuntimeContext.getString(R.string.label_cancel),
                        true, mDownloadFailedDialogCallback);

            } else throw new UnHandledException("Unknown dialog tag");
        }

        @Override
        public void onShowDialogFailed(String dialogTag) {
            mPendingDialogTag = dialogTag;
        }
    }

    public static DateTime getNextCheckUpdateTime() {
        return DateTime.now().plusDays(3);
    }

    private DialogActionHandler mDownloadDialogCallback = new DialogActionHandler() {
        @Override
        public void onYes(DialogInterface dialog) {
            startDownloadApk();
        }

        @Override
        public void onNo(DialogInterface dialog) {
            SharedPreferences pref = RuntimeContext.getSharedPreferences();
            Editor editor = pref.edit();
            editor.putLong(PrefKeys.PREF_KEY_NEXT_UPDATE_DATE, RuntimeContext.getVersionCode() < mVersionInfo.forceUpdateUnderBuildNum
                    ? 0 : getNextCheckUpdateTime().getMillis());
            editor.commit();
            release();
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            release();
        }
    };

    private DialogActionHandler mInstallDialogCallback = new DialogActionHandler() {
        @Override
        public void onYes(DialogInterface dialog) {
            mNotificationManager.cancel(R.id.notification_download_done);
            if (null != mApkDownloadTask) {
                mActvity.startActivity(IntentUtil.getApkInstallerIntent(
                        mApkDownloadTask.getDownloadFile()));
                resetDownloadApk();
            }
        }

        @Override
        public void onNo(DialogInterface dialog) {
            release();
            resetDownloadApk();
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            release();
            resetDownloadApk();
        }
    };

    private DialogActionHandler mDownloadFailedDialogCallback = new DialogActionHandler() {
        @Override
        public void onYes(DialogInterface dialog) {
            // Restart download
            startDownloadApk();
        }

        @Override
        public void onNo(DialogInterface dialog) {
            mActvity.startActivity(IntentUtil.getBrowserIntent(mUpdateSiteUrl));
        }
    };

    //*************************************************************************
    //  Checking Update Listener
    //*************************************************************************

    public interface CheckUpdateListener {
        void onStartCheckingUpdate();
        void onCompleteWithHasUpdate();
        void onCompleteWithNoUpdate();
        void onFailedCheckingUpdate();
    }

}
