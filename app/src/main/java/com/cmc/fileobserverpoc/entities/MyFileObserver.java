package com.cmc.fileobserverpoc.entities;

import android.os.FileObserver;
import android.util.Log;

import java.io.File;

/**
 * Created by Sahar on 04/04/2016.
 */
public class MyFileObserver extends FileObserver {

    private final String TAG = "MyFileObserver";
    private final FileObserverListener mListener;

    public MyFileObserver(File dirObserved, int mask, FileObserverListener listener){
        super(dirObserved.getAbsolutePath(), mask);
        this.mListener = listener;
    }

    public MyFileObserver(File dirObserved, FileObserverListener listener) {
        super(dirObserved.getAbsolutePath());
        this.mListener = listener;
    }

    /**
     * Notice: called on a "special fileObserver thread" which is independent of any other threads -
     * which means we should take care of synchronization ourselves / post msg to main thread to handle.
     * @param event
     * @param path
     */
    @Override
    public void onEvent(int event, String path) {
        Log.d(TAG, "onEvent: event = " + event + ", path = " + path);
        if(mListener != null){
            mListener.onEvent(event, path);
        }
    }

    public interface FileObserverListener {
        void onEvent(int event, String path);
    }
}
