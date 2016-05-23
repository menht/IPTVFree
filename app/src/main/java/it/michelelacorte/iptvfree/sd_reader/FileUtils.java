package it.michelelacorte.iptvfree.sd_reader;

import java.io.File;

/**
 * This class create an utils for read file
 * Created by Salvatore on 30/04/2016
 */
public class FileUtils {

	public static final String FILTER_ALLOW_ALL = "*.*";

    /**
     * Return true if file is accepted
     * @param file File
     * @param filter String
     * @return boolean
     */
	public static boolean accept(final File file, final String filter) {
		if (filter.compareTo(FILTER_ALLOW_ALL) == 0) {
			return true;
		}
		if (file.isDirectory()) {
			return true;
		}
		int lastIndexOfPoint = file.getName().lastIndexOf('.');
		if (lastIndexOfPoint == -1) {
			return false;
		}
		String fileType = file.getName().substring(lastIndexOfPoint).toLowerCase();
		return fileType.compareTo(filter) == 0;
	}
}
