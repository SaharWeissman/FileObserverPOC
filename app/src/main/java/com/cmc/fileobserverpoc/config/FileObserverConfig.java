package com.cmc.fileobserverpoc.config;

import android.os.FileObserver;

/**
 * Created by Sahar on 04/04/2016.
 */
public class FileObserverConfig {

    public static final int FILE_OBSERVER_MASK = (FileObserver.CREATE |
            FileObserver.DELETE |
            FileObserver.DELETE_SELF | // include directory itself
            FileObserver.MODIFY |
            FileObserver.MOVED_FROM |
            FileObserver.MOVED_TO |
            FileObserver.MOVE_SELF);
}
