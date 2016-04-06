package com.cmc.fileobserverpoc.service;

import android.app.Service;
import android.content.Intent;
import android.os.FileObserver;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.cmc.fileobserverpoc.FileObserverManager;
import com.cmc.fileobserverpoc.entities.MyFileObserver;

import java.io.File;

/**
 * Created by Sahar on 04/04/2016.
 */
public class FileObserverService extends Service implements FileObserverManager.FileObserverListener {

    public static final String EXTRA_ACTION = "action";
    public static final String EXTRA_DIR_FILE = "dirFile";
    public static final String EXTRA_MASK = "mask";

    public static final int EXTRA_ACTION_START_MONITORING = 0;
    public static final int EXTRA_ACTION_STOP = 1;
    private MyFileObserver mFileObserver = null;

    private final String TAG = "FileObserverService";
    private File mRootDirFile = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null){
            int action = intent.getIntExtra(EXTRA_ACTION, -1);
            switch (action){
                case EXTRA_ACTION_START_MONITORING:{
                    mRootDirFile = (File) intent.getExtras().get(EXTRA_DIR_FILE);

                    final int mask = intent.getIntExtra(EXTRA_MASK, FileObserver.ALL_EVENTS);
                    Thread startThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            handleStartFileObserver(mRootDirFile, mask, FileObserverService.this);
                        }
                    });
                    startThread.start();
                    break;
                }
                case EXTRA_ACTION_STOP:{
                    Thread stopThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            handleStopFileObserver();
                        }
                    });
                    stopThread.start();
                    break;
                }
                default:{
                    break;
                }
            }
        }
        return START_REDELIVER_INTENT; // if killed while starting, re-start with same intent
    }

    private void handleStopFileObserver() {
        if(mFileObserver != null){
            Log.d(TAG, "handleStopFileObserver: stopping..");
            mFileObserver.stopWatching();
        }
    }

    private void handleStartFileObserver(File dirFile, int mask, FileObserverManager.FileObserverListener listener) {
        if(mFileObserver == null){
            mFileObserver = new MyFileObserver(dirFile, mask, listener);
            mFileObserver.startWatching();
            Log.d(TAG, "handleStartFileObserver: starting..");
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onEvent(int event, String path) {
        Log.d(TAG, "onEvent: event = " + event + ", path = " + path);
        FileObserverManager.getInstance().notifyListeners(path, event);
    }
}
