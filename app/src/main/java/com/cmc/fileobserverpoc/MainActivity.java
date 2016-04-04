package com.cmc.fileobserverpoc;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.cmc.fileobserverpoc.entities.MyFileObserver;
import com.cmc.fileobserverpoc.test.FileObserverTester;

import java.io.File;

/**
 * Created by Sahar on 04/04/2016.
 */
public class MainActivity extends Activity implements MyFileObserver.FileObserverListener {

    private final String TAG = "MainActivity";
    private MyFileObserver mFileObserver = null;
    private final int REQ_CODE_WRITE_EXTERNAL_STORAGE = 0;
    private final String TEST_DIR = "FileObserverTester";
    private File mDirObserved;

    private final int MENU_ITEM_CREATE_FILE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        initUIComponents();
        initObservedDirectory();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_ITEM_CREATE_FILE, MENU_ITEM_CREATE_FILE, "create test file");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case MENU_ITEM_CREATE_FILE:{
                Log.d(TAG, "onOptionsItemSelected: case MENU_ITEM_CREATE_FILE");
                FileObserverTester.getInstance(mDirObserved).createTestFile();
                break;
            }
            default:{
                Log.d(TAG, "onOptionsItemSelected: default case!");
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if(mFileObserver != null){
            mFileObserver.startWatching();
        }
    }

    private void initObservedDirectory() {
        getDirToObserve();
    }

    private void initUIComponents() {
        //TODO: impl.
    }

    private void getDirToObserve() {
        if(Build.VERSION.SDK_INT >= 23) {// Marshmallow

            // check at runtime if we have permission to write (should enable read implicitly also) to ext. storage
            int extStorageWritePermissionCheck = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if(extStorageWritePermissionCheck != PackageManager.PERMISSION_GRANTED) {

                // if not permission - check if the user has already been prompted to allow permission but chose not to enable,
                // this might require we show an informative dialog to explain the user why he should enable this permission
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Just allow me already!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQ_CODE_WRITE_EXTERNAL_STORAGE);
            }
        } else { // api < 23
            createStorageObserverDir();
        }
    }

    private void createStorageObserverDir() {
        File extStorageObservedDir = new File(Environment.getExternalStorageDirectory(), "/Android/data/" + getPackageName() + "/files");
        if(extStorageObservedDir.mkdirs()){
            if(extStorageObservedDir.exists() && extStorageObservedDir.isDirectory()){
                mDirObserved = extStorageObservedDir;
            }
        } else { // use app internal storage (files dir)

            Log.e(TAG, "cannot create dir at external storage, trying app's file dir...");
            File intStorageObservedDir = new File(this.getFilesDir(), TEST_DIR);
            intStorageObservedDir.mkdirs();
            if(extStorageObservedDir.exists() && extStorageObservedDir.isDirectory()){
                mDirObserved = extStorageObservedDir;
            } else{

                // we have an error
                Log.e(TAG, "cannot create dir at all!");
            }
        }
        Log.i(TAG, "dir path = " + mDirObserved);
        initFileObserver();
    }

    private void initFileObserver() {
        mFileObserver = new MyFileObserver(mDirObserved, this);
        mFileObserver.startWatching();
    }



    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        if(mFileObserver != null){
            mFileObserver.stopWatching();
        }
    }

    /**
     *
     * @param event
     * @param path relative to observed directory.
     */
    @Override
    public void onEvent(final int event, final String path) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "onEvent: event = " + event + ", path = " + path, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQ_CODE_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "write ext. storage permission was granted!");

                } else {
                    Log.d(TAG , "write ext. storage permission was denied!");
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                createStorageObserverDir();

                return;
            }

            default:{
                Log.d(TAG , "default case...");
            }
        }
    }
}
