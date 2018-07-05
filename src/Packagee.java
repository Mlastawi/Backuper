import java.io.Serializable;

/**
 * Created by Maciek on 06/11/2016.
 */
public class Packagee implements Serializable {
    private byte[] filesByte;
    private String filename;
    private String additionalInfo;
    private int fileSize;
    private boolean firstPackage;

    Packagee(){
        filesByte = null;
        filename = null;
        additionalInfo = null;
        fileSize = 0;
        firstPackage = true;
    }
    Packagee(byte[] data, String filename){
        filesByte = data;
        this.filename = filename;
        this.fileSize = 0;
        this.additionalInfo = "";
        this.firstPackage = true;
    }

    Packagee(byte[] data, String filename, String additionalInfo){
        filesByte = data;
        this.filename = filename;
        this.additionalInfo = additionalInfo;
        this.fileSize = 0;
        this.firstPackage = true;
    }

    Packagee(byte[] data, String filename, boolean firstPackage){
        filesByte = data;
        this.filename = filename;
        this.additionalInfo = "";
        this.fileSize = 0;
        this.firstPackage = firstPackage;
    }

    Packagee(byte[] data, String filename, int fileSize, boolean firstPackage){
        filesByte = data;
        this.filename = filename;
        this.additionalInfo = "";
        this.fileSize = fileSize;
        this.firstPackage = firstPackage;
    }

    Packagee(byte[] data, String filename, int fileSize, boolean firstPackage, String additionalInfo){
        filesByte = data;
        this.filename = filename;
        this.additionalInfo = additionalInfo;
        this.fileSize = fileSize;
        this.firstPackage = firstPackage;
    }

    public byte[] getFilesByte() {
        return filesByte;
    }

    public String getFilename() {
        return filename;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public int getFileSize() {
        return fileSize;
    }

    public boolean isFirstPackage() {
        return firstPackage;
    }
}
