package com.laxture.skeleton.adapter;

import android.widget.BaseAdapter;
import android.widget.Toast;

import com.laxture.lib.RuntimeContext;
import com.laxture.lib.util.Checker;
import com.laxture.lib.util.LLog;
import com.laxture.skeleton.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class PaginalAdapter<T> extends BaseAdapter {

    public static final int PAGE_ITEM_COUNT_FETCH_MORE = 20;
    public static final int PAGE_ITEM_COUNT_REFRESH = 60;

    protected List<T> mItems =
            Collections.synchronizedList(new ArrayList<T>());

    public enum LoadAction {
        Stop, FetchMore, Refresh
    }
    protected LoadAction loadAction = LoadAction.Stop;

    // if false, try load from local and fetch refresh result
    // from server at the mean time. Should be reset to false
    // after refresh successfully
    protected boolean mInitialized;

    private boolean mNoMoreFromServer;

    private boolean mShowNoMoreToast = true;

    protected boolean mLoading;

    //*************************************************************************
    // Adapter Method
    //*************************************************************************

    public T getItem(int position) {
        return mItems.get(position);
    }

    public int getCount() {
        return mItems.size();
    }

    public void remove(int index) {
        mItems.remove(index);
        notifyDataSetChanged();
    }

    public void remove(T item) {
        mItems.remove(item);
        notifyDataSetChanged();
    }

    public void add(T item) {
        mItems.add(item);
        notifyDataSetChanged();
    }

    public void add(int index, T item) {
        mItems.add(index, item);
        notifyDataSetChanged();
    }

    public void clear() {
        mItems.clear();
        notifyDataSetChanged();
    }

    /**
     * Indicate whether a Toast should be shown when no more
     * data from server. Default is true
     *
     * @param value
     */
    public void setShowNoMoreToast(boolean value) {
        mShowNoMoreToast = value;
    }

    /**
     * Insert data to head of Array in this adapter.
     *
     * @param fetchedData
     */
    public void insertData(List<T> fetchedData) {
        if (Checker.isEmpty(fetchedData)) return;
        mItems.addAll(0, fetchedData);
        notifyDataSetChanged();
    }

    /**
     * Append data to end of Array in this adapter.
     *
     * @param fetchedData
     */
    public void appendData(List<T> fetchedData) {
        if (Checker.isEmpty(fetchedData)) return;
        mItems.addAll(fetchedData);
        notifyDataSetChanged();
    }

    //*************************************************************************
    // Abstract Method
    //*************************************************************************

    protected abstract List<T> fetchMoreFromLocal(int offset);

    protected abstract void fetchMoreFromServer();

    protected abstract void refresh();

    public abstract void onLoadFailed(String errorMessage);

    protected abstract void setLoadingView(LoadAction action);

    protected abstract void stopLoadingView();

    protected void preFetchMore() {}

    protected void onReloadDataCompleted() {}

    //*************************************************************************
    // Public Method
    //*************************************************************************

    public synchronized void loadData(LoadAction action) {
        if (mLoading) {
            LLog.i("Loading Task running, wait a while");
            return;
        }

        loadAction = action;

        // reset noMoreFromServer flag when refresh
        if (loadAction == LoadAction.Refresh) {
            mNoMoreFromServer = false;
        }

        if (loadAction == LoadAction.FetchMore && getNoMoreFromServer()) {
            // stop refreshing if list goes to end
            if (mShowNoMoreToast) {
                Toast.makeText(RuntimeContext.getApplication(),
                        R.string.msg_no_more_data, Toast.LENGTH_SHORT).show();
            }
            stopLoadingView();
            return;
        }

        // if array is not initialized yet, try to load it from local
        if (!mInitialized) {
            // deliver cachedData to UI if it is just loaded from local.
            // then continue to refresh.
            LLog.d("deliver local data, and continue to refresh.");
            reloadData(false);

            mInitialized = true;
        }

        // array might be empty here for two cases:
        // 1. no data saved to local before.
        // 2. no result on either server or local.
        // either case should refresh from server.
        if (getCount() == 0) loadAction = LoadAction.Refresh;

        switch (loadAction) {
        case Stop:
            LLog.d("No need to load any data, return.");
            break;

        case Refresh:
            LLog.d("Fetch refresh data from server");
            mLoading = true;
            refresh();
            setLoadingView(loadAction);
            break;

        case FetchMore:
            preFetchMore();
            List<T> cachedData = fetchMoreFromLocal(getCount());

            // local fetched result is not empty, merge and return
            if (!Checker.isEmpty(cachedData)) {
                LLog.d("Fetch more local data, size=%d", cachedData.size());
                appendData(cachedData);

                // reach end, no need to fetch server data
                if (cachedData.size() == PAGE_ITEM_COUNT_FETCH_MORE) {
                    stopLoadingView();
                    break;
                }
            }

            LLog.d("Fetch more data from server");
            mLoading = true;
            fetchMoreFromServer();
            setLoadingView(loadAction);
            break;
        }
    }

    //*************************************************************************
    // Array Data Methods
    //*************************************************************************

    /**
     * reload data from local database.
     *
     * @param fromServer
     */
    public void reloadData(boolean fromServer) {
        clear();
        List<T> cachedData = fetchMoreFromLocal(0);
        appendData(cachedData);

        // check if goes to the end of list
        // this step should be taken for refresh from server only.
        if (fromServer) {
            mNoMoreFromServer = loadAction == LoadAction.Refresh
                    && cachedData.size() < PAGE_ITEM_COUNT_FETCH_MORE
                    && getCount() == 0;
        }

        onReloadDataCompleted();
    };

    /**
     * check if there is more data from server that serve pulling
     *
     * @param fetchedData
     */
    public void checkHasMoreAfterFetchServerData(List<T> fetchedData) {
        mNoMoreFromServer = loadAction == LoadAction.FetchMore
                && Checker.isEmpty(fetchedData);
    }

    public boolean getNoMoreFromServer() {
        return mNoMoreFromServer;
    }
}
