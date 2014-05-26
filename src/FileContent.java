import java.io.Serializable;

public class FileContent implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3001497005646175737L;
	String fileName;
	String fileDate;

	public FileContent(String fileName, String fileDate) {
		this.fileDate = fileDate;
		this.fileName = fileName;
	}

	public void setFileDate(String fileDate) {
		this.fileDate = fileDate;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}

	public String getFileDate() {
		return fileDate;
	}

}
