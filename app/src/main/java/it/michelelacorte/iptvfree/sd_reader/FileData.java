package it.michelelacorte.iptvfree.sd_reader;

/**
 * This class rapresent file data from device.
 *
 * Created by Salvatore on 30/04/2016
 */
public class FileData implements Comparable<FileData> {

	public static final int UP_FOLDER = 0;
	public static final int DIRECTORY = 1;
	public static final int FILE = 2;
	final private String mFileName;
	final private int mFileType;

	/**
	 * Public constructor for create new FileData object
	 * @param fileName String
	 * @param fileType int
     */
	public FileData(final String fileName, final int fileType) {

		if (fileType != UP_FOLDER && fileType != DIRECTORY && fileType != FILE) {
			throw new IllegalArgumentException("Illegel type of file");
		}
		this.mFileName = fileName;
		this.mFileType = fileType;
	}

	/**
	 * Public method to compare FileData object
	 * @param another FileData
	 * @return int
     */
	@Override
	public int compareTo(final FileData another) {
		if (mFileType != another.mFileType) {
			return mFileType - another.mFileType;
		}
		return mFileName.compareTo(another.mFileName);
	}

	public String getFileName() {
		return mFileName;
	}

	public int getFileType() {
		return mFileType;
	}
}
