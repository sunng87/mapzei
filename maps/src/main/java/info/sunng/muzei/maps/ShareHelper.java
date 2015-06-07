package info.sunng.muzei.maps;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by nsun on 15-6-7.
 */
public class ShareHelper {

    private static final String TAG = ShareHelper.class.getCanonicalName();

    private static final String EXTERNAL_FILE_PATH = "/Android/data/Mapzei/cache/";

    public static Uri createTempFileForSharing(Context context, Uri source) throws IOException {
        File outputDir = new File(Environment.getExternalStorageDirectory(), EXTERNAL_FILE_PATH);
        outputDir.mkdirs();
        File outputFile = new File(outputDir, "share.png");
        //Log.d(TAG, outputFile.getCanonicalPath());

        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFile));

        InputStream fis = context.getContentResolver().openInputStream(source);

        int bs = 1024;
        byte[] buffer = new byte[bs];

        int len = 0;
        while((len = fis.read(buffer)) != -1){
            bos.write(buffer, 0, len);
        }

        bos.close();
        fis.close();

        return Uri.fromFile(outputFile);
    }
}
