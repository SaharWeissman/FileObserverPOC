package com.cmc.fileobserverpoc.views.listView.entities;

/**
 * Created by Sahar on 04/06/2016.
 */
public class FileItem {
    public String mFileAbsPath = null;
    public int txtColor = -1;

    public FileItem(String fullPath, int txtHexColor){
        this.mFileAbsPath = fullPath;
        this.txtColor = txtHexColor;
    }
}
