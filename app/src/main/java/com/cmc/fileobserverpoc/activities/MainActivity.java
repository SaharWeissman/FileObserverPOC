package com.cmc.fileobserverpoc.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cmc.fileobserverpoc.FileObserverManager;
import com.cmc.fileobserverpoc.R;
import com.cmc.fileobserverpoc.activities.base.BaseActivity;
import com.cmc.fileobserverpoc.config.FileObserverConfig;
import com.cmc.fileobserverpoc.entities.MyFileObserver;
import com.cmc.fileobserverpoc.service.FileObserverService;
import com.cmc.fileobserverpoc.service.GoogleDriveManager;
import com.cmc.fileobserverpoc.test.FileObserverTester;
import com.cmc.fileobserverpoc.views.listView.adapters.FilesListAdapter;
import com.cmc.fileobserverpoc.views.listView.entities.FileItem;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Sahar on 04/04/2016.
 */
public class MainActivity extends BaseActivity implements FileObserverManager.FileObserverListener, GoogleDriveManager.GoogleApiClientListener {

    private final String TAG = "MainActivity";
    private MyFileObserver mFileObserver = null;
    private final int REQ_CODE_WRITE_EXTERNAL_STORAGE = 0;
    private final String TEST_DIR = "FileObserverTester";
    private File mDirObserved;

    private final int MENU_ITEM_TEST_CREATE_FILE = 0;
    private final int MENU_ITEM_CREATE_INITIAL_DRIVE_FOLDER = 1;

    private TextView mTxtDirObserved;
    private ListView mListViewFiles;
    private TextView mTxtDriveOpertain;
    private ArrayList<FileItem> mDirFilesArray;
    private FilesListAdapter mFileListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.main);
        initUIComponents();
        initObservedDirectory();
        super.addGoogleApiClientListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_ITEM_TEST_CREATE_FILE, MENU_ITEM_TEST_CREATE_FILE, "create test file");
        menu.add(0, MENU_ITEM_CREATE_INITIAL_DRIVE_FOLDER, MENU_ITEM_CREATE_INITIAL_DRIVE_FOLDER, "create drive init. folder");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case MENU_ITEM_TEST_CREATE_FILE:{
                Log.d(TAG, "onOptionsItemSelected: case MENU_ITEM_TEST_CREATE_FILE");
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
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    private void initObservedDirectory() {
        getDirToObserve();
    }

    private void initUIComponents() {
        mTxtDirObserved = (TextView)findViewById(R.id.txtV_dir_observed);
        mListViewFiles = (ListView)findViewById(R.id.lstV_dir_files);
        mTxtDriveOpertain = (TextView)findViewById(R.id.txtV_google_drive_opertaions);

        mTxtDirObserved.setText("not set");
        mTxtDriveOpertain.setText("empty");
        initListViewFiles(mListViewFiles);
    }

    private void initListViewFiles(ListView mListViewFiles) {
        mDirFilesArray = new ArrayList<>();
        mFileListAdapter = new FilesListAdapter(this, R.layout.item_file, mDirFilesArray);
        mListViewFiles.setAdapter(mFileListAdapter);
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
        mTxtDirObserved.setText("Dir. Observed:\n" + mDirObserved.getAbsolutePath());
        initFileObserver();
    }

    private void initFileObserver() {
        Intent startServiceIntent = new Intent(this, FileObserverService.class);
        startServiceIntent.putExtra(FileObserverService.EXTRA_ACTION, FileObserverService.EXTRA_ACTION_START_MONITORING);
        startServiceIntent.putExtra(FileObserverService.EXTRA_DIR_FILE, mDirObserved);
        startServiceIntent.putExtra(FileObserverService.EXTRA_MASK, FileObserverConfig.FILE_OBSERVER_MASK);

        startService(startServiceIntent);
        FileObserverManager.getInstance().addListener(this);
    }



    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    /**
     *
     * @param event
     * @param path relative to observed directory.
     */
    @Override
    public void onEvent(final int event, final String path) { // remember path is relative to root monitored dir.
        if((event & FileObserver.CREATE) != 0){ // new file or dir created
            File newFile = new File(mDirObserved, path);
            if(newFile.exists()){
                GoogleDriveManager.getInstance().createFileInsideFolder(mGoogleApiClient, path, newFile.isDirectory());
            }
        }else if ((event & FileObserver.MODIFY) != 0) { // file was modified

        }else if ((event & FileObserver.MOVE_SELF) != 0) { // the directory monitored was moved (continues monitoring)

        }else if ((event & FileObserver.MOVED_FROM) != 0) { // file / subdir. moved from dir

        }else if ((event & FileObserver.MOVED_TO) != 0) { // file / subdir. moved to dir

        }else if ((event & FileObserver.DELETE) != 0) { // file / subdir. deleted

        }else if ((event & FileObserver.DELETE_SELF) != 0) { // monitored dir was deleted (monitoring stops)

        }
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
                    Log.d(TAG, "write ext. storage permission was denied!");
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

    @Override
    public void onClientConnected() {
        String rootFolderName = getPackageName() + "_" + System.currentTimeMillis();
        Log.d(TAG, "onClientConnected - creating root dir: " + rootFolderName);
        GoogleDriveManager.getInstance().createFolder(mGoogleApiClient, rootFolderName);
    }
}
