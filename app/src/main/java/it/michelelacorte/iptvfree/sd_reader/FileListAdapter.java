package it.michelelacorte.iptvfree.sd_reader;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import it.michelelacorte.iptvfree.R;

/**
 * This class create an Adapter for FileData object.
 *
 * Created by Salvatore on 30/04/2016
 */
public class FileListAdapter extends BaseAdapter {

	private final ArrayList<FileData> mFileDataArray;
	private final Context mContext;
	public FileListAdapter(Context context, List<FileData> aFileDataArray) {
		mFileDataArray = (ArrayList<FileData>) aFileDataArray;
		mContext = context;
	}

    /**
     * Get size of FileDataArray
     * @return int
     */
	@Override
	public int getCount() {
		return mFileDataArray.size();
	}

    /**
     * Get item from array with position
     * @param position int
     * @return Object
     */
	@Override
	public Object getItem(int position) {
		return mFileDataArray.get(position);
	}

    /**
     * Get item id of array
     * @param position int
     * @return long
     */
	@Override
	public long getItemId(int position) {
		return position;
	}

    /**
     * Get view
     * @param position int
     * @param convertView View
     * @param parent ViewGroup
     * @return View
     */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		FileData tempFileData = mFileDataArray.get(position);
		TextViewWithImage tempView = new TextViewWithImage(mContext);
		tempView.setText(tempFileData.getFileName());
		int imgRes = -1;
		switch (tempFileData.getFileType()) {
		case FileData.UP_FOLDER: {
			imgRes = R.drawable.ic_undo_black_24dp;
			break;
		}
		case FileData.DIRECTORY: {
			imgRes = R.drawable.ic_folder_black_24dp;
			break;
		}
		case FileData.FILE: {
			imgRes = R.drawable.ic_insert_drive_file_black_24dp;
			break;
		}
		default:
			break;
		}
		tempView.setImageResource(imgRes);
		return tempView;
	}

}
