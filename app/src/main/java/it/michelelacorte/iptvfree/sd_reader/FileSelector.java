package it.michelelacorte.iptvfree.sd_reader;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import it.michelelacorte.iptvfree.R;


public class FileSelector {

	private ListView mFileListView;
	private Button mLoadButton;
	private Button mCancelButton;
	private Button mNewFolderButton;
	private Spinner mFilterSpinner;
	private File mCurrentLocation;
	private final Dialog mDialog;
	private Context mContext;
	private final OnHandleFileListener mOnHandleFileListener;

	public FileSelector(final Context context, final FileOperation operation,
			final OnHandleFileListener onHandleFileListener, final String[] fileFilters) {
		mContext = context;
		mOnHandleFileListener = onHandleFileListener;

		final File sdCard = Environment.getExternalStorageDirectory();
		if (sdCard.canRead()) {
			mCurrentLocation = sdCard;
		} else {
			mCurrentLocation = Environment.getRootDirectory();
		}

		mDialog = new Dialog(context);
		mDialog.setContentView(R.layout.dialog);
		mDialog.setTitle(mCurrentLocation.getAbsolutePath());

		prepareFilterSpinner(fileFilters);
		prepareFilesList();

		setSaveLoadButton(operation);
		setNewFolderButton(operation);
		setCancelButton();
	}

	private void prepareFilterSpinner(String[] fitlesFilter) {
		mFilterSpinner = (Spinner) mDialog.findViewById(R.id.fileFilter);
		if (fitlesFilter == null || fitlesFilter.length == 0) {
			fitlesFilter = new String[] { FileUtils.FILTER_ALLOW_ALL };
			mFilterSpinner.setEnabled(false);
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext, R.layout.spinner_item, fitlesFilter);

		mFilterSpinner.setAdapter(adapter);
		OnItemSelectedListener onItemSelectedListener = new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> aAdapter, View aView, int arg2, long arg3) {
				TextView textViewItem = (TextView) aView;
				String filtr = textViewItem.getText().toString();
				makeList(mCurrentLocation, filtr);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}
		};
		mFilterSpinner.setOnItemSelectedListener(onItemSelectedListener);
	}


	private void prepareFilesList() {
		mFileListView = (ListView) mDialog.findViewById(R.id.fileList);

		mFileListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
				// Check if "../" item should be added.
				((EditText) mDialog.findViewById(R.id.fileName)).setText("");
				if (id == 0) {
					final String parentLocation = mCurrentLocation.getParent();
					if (parentLocation != null) { // text == "../"
						String fileFilter = ((TextView) mFilterSpinner.getSelectedView()).getText().toString();
						mCurrentLocation = new File(parentLocation);
						makeList(mCurrentLocation, fileFilter);
					} else {
						onItemSelect(parent, position);
					}
				} else {
					onItemSelect(parent, position);
				}
			}
		});
		String filtr = mFilterSpinner.getSelectedItem().toString();
		makeList(mCurrentLocation, filtr);
	}


	private void makeList(final File location, final String fitlesFilter) {
		final ArrayList<FileData> fileList = new ArrayList<FileData>();
		final String parentLocation = location.getParent();
		if (parentLocation != null) {
			// First item on the list.
			fileList.add(new FileData("../", FileData.UP_FOLDER));
		}
		File listFiles[] = location.listFiles();
		if (listFiles != null) {
			ArrayList<FileData> fileDataList = new ArrayList<FileData>();
			for (int index = 0; index < listFiles.length; index++) {
				File tempFile = listFiles[index];
				if (FileUtils.accept(tempFile, fitlesFilter)) {
					int type = tempFile.isDirectory() ? FileData.DIRECTORY : FileData.FILE;
					fileDataList.add(new FileData(listFiles[index].getName(), type));
				}
			}
			fileList.addAll(fileDataList);
			Collections.sort(fileList);
		}
		if (mFileListView != null) {
			FileListAdapter adapter = new FileListAdapter(mContext, fileList);
			mFileListView.setAdapter(adapter);
		}
	}


	private void onItemSelect(final AdapterView<?> parent, final int position) {
		final String itemText = ((FileData) parent.getItemAtPosition(position)).getFileName();
		final String itemPath = mCurrentLocation.getAbsolutePath() + File.separator + itemText;
		final File itemLocation = new File(itemPath);

		if (!itemLocation.canRead()) {
			Toast.makeText(mContext, "Access denied!!!", Toast.LENGTH_SHORT).show();
		} else if (itemLocation.isDirectory()) {
			mCurrentLocation = itemLocation;
			String fileFilter = ((TextView) mFilterSpinner.getSelectedView()).getText().toString();
			makeList(mCurrentLocation, fileFilter);
		} else if (itemLocation.isFile()) {
			final EditText fileName = (EditText) mDialog.findViewById(R.id.fileName);
			fileName.setText(itemText);
		}
	}


	private void setSaveLoadButton(final FileOperation operation) {
		mLoadButton = (Button) mDialog.findViewById(R.id.fileSaveLoad);
			mLoadButton.setText(R.string.loadButtonText);
		mLoadButton.setOnClickListener(new LoadClickListener(operation, this, mContext));
	}


	private void setNewFolderButton(final FileOperation operation) {
		mNewFolderButton = (Button) mDialog.findViewById(R.id.newFolder);
		OnClickListener newFolderListener = new OnClickListener() {
			@Override
			public void onClick(final View v) {
				openNewFolderDialog();
			}
		};
			mNewFolderButton.setVisibility(View.GONE);
	}

	private void openNewFolderDialog() {
		AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
		alert.setTitle(R.string.newFolderButtonText);
		alert.setMessage(R.string.newFolderDialogMessage);
		final EditText input = new EditText(mContext);
		alert.setView(input);
		alert.setPositiveButton(R.string.createButtonText, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int whichButton) {
				File file = new File(mCurrentLocation.getAbsolutePath() + File.separator + input.getText().toString());
				if (file.mkdir()) {
					Toast t = Toast.makeText(mContext, R.string.folderCreationOk, Toast.LENGTH_SHORT);
					t.setGravity(Gravity.CENTER, 0, 0);
					t.show();
				} else {
					Toast t = Toast.makeText(mContext, R.string.folderCreationError, Toast.LENGTH_SHORT);
					t.setGravity(Gravity.CENTER, 0, 0);
					t.show();
				}
				String fileFilter = ((TextView) mFilterSpinner.getSelectedView()).getText().toString();
				makeList(mCurrentLocation, fileFilter);
			}
		});
		alert.show();
	}

	private void setCancelButton() {
		mCancelButton = (Button) mDialog.findViewById(R.id.fileCancel);
		mCancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View view) {
				mDialog.cancel();
			}
		});
	}

	public String getSelectedFileName() {
		final EditText fileName = (EditText) mDialog.findViewById(R.id.fileName);
		return fileName.getText().toString();
	}

	public File getCurrentLocation() {
		return mCurrentLocation;
	}

	public void show() {
		mDialog.show();
	}
	public void dismiss() {
		mDialog.dismiss();
	}
}
