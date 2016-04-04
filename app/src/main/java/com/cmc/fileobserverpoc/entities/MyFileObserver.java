package com.cmc.fileobserverpoc.entities;

import android.os.FileObserver;
import android.util.Log;

import com.cmc.fileobserverpoc.FileObserverManager;

import java.io.File;

/**
 * Created by Sahar on 04/04/2016.
 */
public class MyFileObserver extends FileObserver {

    private final String TAG = "MyFileObserver";
    private final FileObserverManager.FileObserverListener mListener;
    private final File mBaseDir;

    public MyFileObserver(File dirObserved, int mask, FileObserverManager.FileObserverListener listener){
        super(dirObserved.getAbsolutePath(), mask);
        this.mListener = listener;
        this.mBaseDir = dirObserved;
    }

    public MyFileObserver(File dirObserved, FileObserverManager.FileObserverListener listener) {
        this(dirObserved, FileObserver.ALL_EVENTS, listener);
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

    public File getBaseDir(){
        return this.mBaseDir;
    }
}
