package com.cmc.fileobserverpoc.service;

import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.ExecutionOptions;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Created by Sahar on 04/06/2016.
 */
public class GoogleDriveManager {

    private static volatile GoogleDriveManager sInstance = null;
    private final String TAG = "GoogleDriveManager";
    private DriveId mDriveId;
    private DriveId mCurrFolderId;
    private String rootDirResId;

    public GoogleDriveManager(){
    }

    public static GoogleDriveManager getInstance(){
        if(sInstance == null){
            synchronized (GoogleDriveManager.class){
                if(sInstance == null){
                    sInstance = new GoogleDriveManager();
                }
            }
        }
        return sInstance;
    }

    public void createFolder(final GoogleApiClient apiClient, final String folderName){
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle(folderName).build();
        Drive.DriveApi.getRootFolder(apiClient).createFolder(
                apiClient, changeSet).setResultCallback(new ResultCallback<DriveFolder.DriveFolderResult>() {
            @Override
            public void onResult(DriveFolder.DriveFolderResult result) {
                if (!result.getStatus().isSuccess()) {
                    Log.e(TAG, "cannot create folder");
                } else {
                    mDriveId = result.getDriveFolder().getDriveId();
                    Log.d(TAG, "created a folder: driverId = " + mDriveId);

                    /*
                     Issue here: resourceId is still null because folder has not yet been comitted to google drive.
                     need to fix this, possible hack:
                     1. create new dummy file with the above driveId as parent (figure out how...?)
                     2. the dummy file creating forces the folder creation
                     3. receive callback for dummy file create and retrieve parent's reource id

                     link: http://stackoverflow.com/questions/34318220/google-drive-android-api-completion-event-for-folder-creation
                      */
                    createDummyFileForResID(apiClient, mDriveId);
                }
            }
        });
    }

    private void createDummyFileForResID(final GoogleApiClient apiClient, final DriveId driveId) {
        Drive.DriveApi.newDriveContents(apiClient)
                .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                    @Override
                    public void onResult(final DriveApi.DriveContentsResult result) {
                        if (!result.getStatus().isSuccess()) {
                            Log.e(TAG, "Error while trying to create new file contents");
                            return;
                        }
                        final DriveContents driveContents = result.getDriveContents();
                        new Thread() {
                            @Override
                            public void run() {
                                DriveFolder folder = Drive.DriveApi.getFolder(apiClient, driveId);

                                // write content to DriveContents
                                OutputStream outputStream = driveContents.getOutputStream();
                                Writer writer = new OutputStreamWriter(outputStream);
                                try {
                                    writer.write("Hello World!");
                                    writer.close();
                                } catch (IOException e) {
                                    Log.e(TAG, e.getMessage());
                                }

                                MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                        .setTitle("Dummy File")
                                        .setMimeType("text/plain")
                                        .setStarred(true).build();

                                // create a file on root folder
                                ExecutionOptions executionOptions = new ExecutionOptions.Builder()
                                        .setNotifyOnCompletion(true)
                                        .build();
                                folder.createFile(apiClient, changeSet, result.getDriveContents(), executionOptions).setResultCallback(new ResultCallback<DriveFolder.DriveFileResult>() {
                                    @Override
                                    public void onResult(DriveFolder.DriveFileResult driveFileResult) {
                                        if (!driveFileResult.getStatus().isSuccess()) {
                                            Log.e(TAG, "Error while trying to create new file contents");
                                            return;
                                        } else {
                                            Log.d(TAG, "file created successfully!");
                                        }
                                    }
                                });


                            }
                        }.start();
                    }
                });
    }

    public void createFileInsideFolder(final GoogleApiClient apiClient, final String newFileName, final boolean isDir){
        Drive.DriveApi.fetchDriveId(apiClient, this.rootDirResId).setResultCallback(new ResultCallback<DriveApi.DriveIdResult>() {
            @Override
            public void onResult(DriveApi.DriveIdResult driveIdResult) {
                if (!driveIdResult.getStatus().isSuccess()) {
                    Log.e(TAG, "Cannot find DriveId. Are you authorized to view this file?");
                    return;
                }
                mCurrFolderId = driveIdResult.getDriveId();
                Drive.DriveApi.newDriveContents(apiClient).setResultCallback(
                        new ResultCallback<DriveApi.DriveContentsResult>() {
                            @Override
                            public void onResult(DriveApi.DriveContentsResult driveContentsResult) {
                                if (!driveContentsResult.getStatus().isSuccess()) {
                                    Log.e(TAG, "Error while trying to create new file contents");
                                    return;
                                }
                                DriveFolder folder = mCurrFolderId.asDriveFolder();

                                // TODO: handle all mime types
                                String fileMimeType = "text/plain";
                                if (isDir) {
                                    fileMimeType = "application/vnd.google-apps.folder";
                                }

                                MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                        .setTitle(newFileName)
                                        .setMimeType(fileMimeType)
                                        .setStarred(true).build();
                                folder.createFile(apiClient, changeSet, driveContentsResult.getDriveContents())
                                        .setResultCallback(new ResultCallback<DriveFolder.DriveFileResult>() {
                                            @Override
                                            public void onResult(DriveFolder.DriveFileResult driveFileResult) {
                                                if (!driveFileResult.getStatus().isSuccess()) {
                                                    Log.e(TAG, "Error while trying to create the file");
                                                    return;
                                                }
                                                Log.d(TAG, "Created a file: " + driveFileResult.getDriveFile().getDriveId());
                                            }
                                        });
                            }
                        });
            }
        });

    }

    public void deleteFile(GoogleApiClient apiClient, DriveId driveId){
        Drive.DriveApi.getFile(apiClient, driveId).delete(apiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if (!status.isSuccess()) {
                    Log.e(TAG, "Error while trying to create new file contents");
                    return;
                }
                Log.d(TAG, "file deleted successfully");
            }
        });
    }

    public void setRootDirResId(String resourceId) {
        this.rootDirResId = resourceId;
    }

    public interface GoogleApiClientListener{
        void onClientConnected();
    }

    public DriveId getDriveId(){
        return mDriveId;
    }
}
