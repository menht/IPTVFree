package it.michelelacorte.iptvfree.sd_reader;

/**
 * This interface provide method for handle file on read finish.
 *
 * Created by Salvatore on 30/04/2016
 */
public interface OnHandleFileListener {
	void handleFile(String filePath);
}