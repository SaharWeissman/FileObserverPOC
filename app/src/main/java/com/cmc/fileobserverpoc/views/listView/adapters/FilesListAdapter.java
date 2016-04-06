package com.cmc.fileobserverpoc.views.listView.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.cmc.fileobserverpoc.R;
import com.cmc.fileobserverpoc.views.listView.entities.FileItem;

import java.util.List;

/**
 * Created by Sahar on 04/06/2016.
 */
public class FilesListAdapter extends ArrayAdapter<FileItem> {

    private final List<FileItem> filesList;
    private final int resId;

    public FilesListAdapter(Context context, int resource, List<FileItem> files) {
        super(context, resource, files);
        this.filesList = files;
        this.resId = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        FileItem file = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.item_file, parent, false);
            viewHolder.path = (TextView) convertView.findViewById(R.id.txtV_file_item_path);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        // Populate the data into the template view using the data object
        viewHolder.path.setText(file.mFileAbsPath);
        viewHolder.path.setTextColor(file.txtColor);
        // Return the completed view to render on screen
        return convertView;
    }

    // View lookup cache
    private static class ViewHolder {
        TextView path;
    }
}
