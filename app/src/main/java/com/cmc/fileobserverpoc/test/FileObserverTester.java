package com.cmc.fileobserverpoc.test;

import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * Created by Sahar on 04/04/2016.
 */
public class FileObserverTester {

    private static volatile FileObserverTester sInstance = null;
    private final File mDirObserved;
    private String TEST_FILE_NAME = "test_file.txt";
    private final String TAG = "FileObserverTester";

    public FileObserverTester(File observedDir) {
        this.mDirObserved = observedDir;
    }

    public static FileObserverTester getInstance(File observedDir){
        if(sInstance == null){
            synchronized (FileObserverTester.class){
                if(sInstance == null){
                    sInstance = new FileObserverTester(observedDir);
                }
            }
        }
        return sInstance;
    }

    public void createTestFile(){
        File testFile = new File(this.mDirObserved, TEST_FILE_NAME);
        try {
            testFile.createNewFile();
        } catch (IOException e) {
            Log.e(TAG, "unable to create test file!", e);
        }
    }
}
