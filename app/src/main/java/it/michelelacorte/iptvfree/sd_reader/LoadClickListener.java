package it.michelelacorte.iptvfree.sd_reader;

import java.io.File;

import android.app.AlertDialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import it.michelelacorte.iptvfree.R;

public class LoadClickListener implements OnClickListener {

	private final FileOperation mOperation;
	private final FileSelector mFileSelector;
	private final Context mContext;


	public LoadClickListener(final FileOperation operation, final FileSelector fileSelector, final Context context) {
		mOperation = operation;
		mFileSelector = fileSelector;
		mContext = context;
	}

	@Override
	public void onClick(final View view) {
		final String text = mFileSelector.getSelectedFileName();
		if (checkFileName(text)) {
			final String filePath = mFileSelector.getCurrentLocation().getAbsolutePath() + File.separator + text;
			final File file = new File(filePath);
			int messageText = 0;
					if (!file.exists()) {
						messageText = R.string.missingFile;
					} else if (!file.canRead()) {
						messageText = R.string.accessDenied;
					}
			if (messageText != 0) {
				final Toast t = Toast.makeText(mContext, messageText, Toast.LENGTH_SHORT);
				t.setGravity(Gravity.CENTER, 0, 0);
				t.show();
			} else {
				mFileSelector.mOnHandleFileListener.handleFile(filePath);
				mFileSelector.dismiss();
			}
		}
	}

	private boolean checkFileName(String text) {
		if (text.length() == 0) {
			final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			builder.setTitle(R.string.information);
			builder.setMessage(R.string.fileNameFirstMessage);
			builder.setNeutralButton(R.string.okButtonText, null);
			builder.show();
			return false;
		}
		return true;
	}
}
