package com.cmc.fileobserverpoc.service;

import android.util.Log;

import com.cmc.fileobserverpoc.activities.base.BaseActivity;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.events.CompletionEvent;
import com.google.android.gms.drive.events.DriveEventService;

/**
 * Created by Sahar on 04/06/2016.
 */
public class MyDriveEventService extends DriveEventService {

    private final String TAG = "MyDriveEventService";

    @Override
    public void onCompletion(CompletionEvent event) {
        Log.d(TAG, "Action completed with status: " + event.getStatus());
        Log.d(TAG, "onCompletion: driveId = " + event.getDriveId());
        DriveId parentId = parentID(event.getDriveId());
        Log.d(TAG, "onCompletion: parentDriveId = " + parentId + ", parent ResId = " + parentId.getResourceId());
        GoogleDriveManager.getInstance().deleteFile(BaseActivity.mGoogleApiClient, event.getDriveId());
        GoogleDriveManager.getInstance().setRootDirResId(parentId.getResourceId());
        event.dismiss();
    }

    public DriveId parentID(DriveId dId) {
        MetadataBuffer mdb = null;
        DriveApi.MetadataBufferResult mbRslt = dId.asDriveResource().listParents(BaseActivity.mGoogleApiClient).await();
        if (mbRslt.getStatus().isSuccess()) try {
            mdb = mbRslt.getMetadataBuffer();
            if (mdb.getCount() > 0)
                return mdb.get(0).getDriveId();
        } catch (Exception e) { e.printStackTrace();}
        finally {
            if (mdb != null) mdb.close();
        }
        return null;
    }
}
