package com.cmc.fileobserverpoc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sahar on 04/04/2016.
 */
public class FileObserverManager {

    private static volatile FileObserverManager sInstance = null;
    private List<FileObserverListener> mListeners = null;

    private FileObserverManager(){
        this.mListeners = new ArrayList<>(0);
    }

    public static FileObserverManager getInstance(){
        if(sInstance == null){
            synchronized (FileObserverManager.class){
                if(sInstance == null){
                    sInstance = new FileObserverManager();
                }
            }
        }
        return sInstance;
    }

    public void addListener(FileObserverListener listener){
        mListeners.add(listener);
    }

    public void removeListener(FileObserverListener listener){
        mListeners.remove(listener);
    }

    public void notifyListeners(String path, int mask){
        if(mListeners != null){
            for(int i = 0; i < mListeners.size(); i++){
                mListeners.get(i).onEvent(mask, path);
            }
        }
    }

    public interface FileObserverListener {
        void onEvent(int event, String path);
    }
}
