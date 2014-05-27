import java.io.Serializable;

public class FileContent implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3001497005646175737L;
	String fileName;
	String fileData;

	public FileContent(String fileName, String fileData) {
		this.fileData = fileData;
		this.fileName = fileName;
	}

	public void setFileData(String fileData) {
		this.fileData = fileData;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}

	public String getFileData() {
		return fileData;
	}

}
