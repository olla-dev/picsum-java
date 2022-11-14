package eu.codeplumbers.picsum_java;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Random;

public class PhotoFetcherService extends IntentService {
    public static final String PICSUM_URL = "https://picsum.photos";
    public static final int LOADING = 1;
    public static final int SUCCESS = 0;
    public static final int ERROR = -1;
    public static final String CACHED_PHOTO_PATH = "/data/data/"
            + MainActivity.PACKAGE_NAME
            + "/cache/picsum_cached.jpeg";

    public PhotoFetcherService() {
        super(PhotoFetcherService.class.getSimpleName());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        Intent intent1 = new Intent();
        intent1.setAction(MainActivity.PACKAGE_NAME);
        intent1.putExtra("STATUS", LOADING);
        sendBroadcast(intent1);
        try {
            downloadRandomPhoto(
                    PICSUM_URL + "/" + new Random().nextInt(1000));
            intent1.putExtra("STATUS", SUCCESS);
            sendBroadcast(intent1);
        } catch (Exception e) {
            intent1.putExtra("STATUS", ERROR);
            intent1.putExtra("ERROR_MESSAGE", e.getMessage());
            sendBroadcast(intent1);
        }
    }

    private void downloadRandomPhoto(String urlString) {
        try {
            URL url = new URL(urlString);
            URLConnection conection = url.openConnection();
            conection.connect();

            // this will be useful so that you can show a tipical 0-100%
            // progress bar
            int lenghtOfFile = conection.getContentLength();

            // download the file
            InputStream input = new BufferedInputStream(url.openStream(),
                    8192);

            // Output stream
            OutputStream output = new FileOutputStream(CACHED_PHOTO_PATH);

            byte data[] = new byte[1024];

            long total = 0;
            int count = 0;

            while ((count = input.read(data)) != -1) {
                total += count;

                // writing data to file
                output.write(data, 0, count);
            }

            // flushing output
            output.flush();

            // closing streams
            output.close();
            input.close();
        } catch (Exception e) {
            Log.e("Error: ", e.getMessage());
        }
    }
}
