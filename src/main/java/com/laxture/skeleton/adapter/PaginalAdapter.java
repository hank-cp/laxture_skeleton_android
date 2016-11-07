package com.laxture.skeleton.adapter;

import android.widget.BaseAdapter;
import android.widget.Toast;

import com.laxture.lib.RuntimeContext;
import com.laxture.lib.java8.Predicate;
import com.laxture.lib.util.Checker;
import com.laxture.lib.util.LLog;
import com.laxture.skeleton.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class PaginalAdapter<T> extends BaseAdapter {

    public static final int PAGE_ITEM_COUNT_FETCH_MORE = 10;
    public static final int PAGE_ITEM_COUNT_REFRESH = 30;

    protected List<T> mItems =
            Collections.synchronizedList(new ArrayList<T>());

    public enum LoadAction {
        Stop, FetchMore, Refresh
    }
    protected LoadAction loadAction = LoadAction.Stop;

    // if false, try load from local and fetch refresh result
    // from server at the mean time. Should be reset to false
    // after refresh successfully
    protected boolean mLoadStubData;

    private boolean mNoMoreFromServer;

    protected boolean mLoading;

    public int paginalRows = PAGE_ITEM_COUNT_FETCH_MORE;

    public int paginalRefreshRows = PAGE_ITEM_COUNT_REFRESH;

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

    public void removeIf(Predicate<T> predicate) {
        T matchedItem = null;
        for (T item : mItems) {
            if (predicate.test(item)) {
                matchedItem = item;
                break;
            }
        }
        if (matchedItem != null) remove(matchedItem);
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

    protected abstract void setLoadingView(LoadAction action);

    protected abstract void stopLoadingView(boolean noMoreData);

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

        // if list goes to end, stop the loading view and return
        if (loadAction == LoadAction.FetchMore && hasNoMoreFromServer()) {
            stopLoadingView(true);
            return;
        }

        // if array is not initialized yet, try to load it from local
        if (!mLoadStubData) {
            // deliver cachedData to UI if it is just loaded from local.
            // then continue to refresh.
            LLog.d("deliver local data, and continue to refresh.");
            reloadData(false);

            mLoadStubData = true;
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
                if (cachedData.size() == paginalRows) {
                    stopLoadingView(false);
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
     * @param afterRefreshFromServer
     */
    public void reloadData(boolean afterRefreshFromServer) {
        clear();
        List<T> cachedData = fetchMoreFromLocal(0);
        appendData(cachedData);

        // check if goes to the end of list
        // this step should be taken for refresh from server only.
        if (afterRefreshFromServer) {
            mNoMoreFromServer = loadAction == LoadAction.Refresh
                    && cachedData.size() < paginalRefreshRows;
        }

        onReloadDataCompleted();
    }

    /**
     * check if there is more data from server that serve pulling
     */
    public void checkHasMoreAfterFetchServerData(List<T> fetchedData) {
        mNoMoreFromServer = loadAction == LoadAction.FetchMore
                && Checker.isEmpty(fetchedData);
    }

    public boolean hasNoMoreFromServer() {
        return mNoMoreFromServer;
    }
}
