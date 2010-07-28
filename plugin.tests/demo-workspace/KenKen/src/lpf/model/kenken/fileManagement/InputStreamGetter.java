package lpf.model.kenken.fileManagement;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * A class for constructing InputStreams from other objects. getInputStream()
 * will return a new instance of the input stream every time is it called.
 * 
 * This is compatible with File, and with ZipFile + ZipEntry.
 * 
 * @author Peter Kalauskas
 */
public class InputStreamGetter {
	File file;
	ZipFile zip;
	ZipEntry entry;

	/**
	 * Construct an input stream from a file.
	 * 
	 * @param file
	 */
	public InputStreamGetter(File file) {
		this.file = file;
		this.zip = null;
		this.entry = null;
	}

	/**
	 * Construct an input stream from a ZipFile and ZipEntry
	 * 
	 * @param zip
	 *            the ZipFile in which the entry resides
	 * @param entry
	 *            the ZipEntry inside the ZipFile
	 */
	public InputStreamGetter(ZipFile zip, ZipEntry entry) {
		this.file = null;
		this.zip = zip;
		this.entry = entry;
	}

	/**
	 * Gets the InputStream from the source provided in the constructor.
	 * 
	 * @return a new input stream
	 * @throws IOException
	 */
	InputStream getInputStream() throws IOException {
		if (this.file != null) {
			return new FileInputStream(file);
		} else {
			return this.zip.getInputStream(this.entry);
		}
	}
}
