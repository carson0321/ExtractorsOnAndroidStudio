package tw.edu.ntu.csie.selab.facebookOutboxExtractor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.os.Environment;

public class FileUtils {
    //Checks if external storage is available for read and write
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    //Write file to external storage
    public static void writeToFile(String fileName, String data){
        //Create the directory for the user's public pictures directory
        String path = Environment.getExternalStorageDirectory().getPath();
//	    File dir = new File(path + "/facebookOutboxextractor");
        File dir = new File(path + "/");
        if (!dir.exists()){
            dir.mkdir();
        }
        try {
//	    	File file = new File(path + "/facebookOutboxextractor/" + fileName);
            File file = new File(path + "/" + fileName);
            FileOutputStream fout = new FileOutputStream(file);
            fout.write(data.getBytes());
            fout.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
