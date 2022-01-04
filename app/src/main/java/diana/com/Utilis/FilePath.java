package diana.com.Utilis;

import android.os.Environment;

public class FilePath {
    public String ROOT_DIR= Environment.getExternalStorageDirectory().getPath();

    public String PICTURES=ROOT_DIR +"/Pictures";

    public String CAMERA=ROOT_DIR +"/DCIM/camera";


    public String FIREBASE_IMAGE_STORAGE="photo/users/";
}
