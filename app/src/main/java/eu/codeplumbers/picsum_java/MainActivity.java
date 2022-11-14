package eu.codeplumbers.picsum_java;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    public static final String PACKAGE_NAME = "eu.codeplumbers.picsum_java";
    Button btnRefresh;
    TextView txtError;
    ImageView imageView;
    ProgressBar progressBar;

    private BroadcastReceiver networkStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (null != activeNetwork) {
                if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI
                        || activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                    btnRefresh.setEnabled(true);
                    txtError.setVisibility(View.GONE);
                } else {
                    btnRefresh.setEnabled(false);
                    txtError.setVisibility(View.VISIBLE);
                }
            } else {
                btnRefresh.setEnabled(false);
                txtError.setVisibility(View.VISIBLE);
            }
        }
    };

    BroadcastReceiver serviceBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int status = intent.getIntExtra("STATUS", PhotoFetcherService.SUCCESS);

            switch (status) {
                case PhotoFetcherService.LOADING:
                    txtError.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                    break;
                case PhotoFetcherService.SUCCESS:
                    txtError.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                    loadCachedPhoto();
                    break;
                case PhotoFetcherService.ERROR:
                    txtError.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    String s1 = intent.getStringExtra("ERROR_MESSAGE");
                    txtError.setText(s1);
                    break;
                default:
                    break;
            }
        }
    };

    private void loadCachedPhoto() {
        File imgFile = new File(PhotoFetcherService.CACHED_PHOTO_PATH);
        if(imgFile.exists()){
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            imageView.setImageBitmap(myBitmap);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        btnRefresh = findViewById(R.id.btnRefresh);
        txtError = findViewById(R.id.txtError);
        imageView = findViewById(R.id.imageView);
        progressBar = findViewById(R.id.progressBar);

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startService(new Intent(MainActivity.this, PhotoFetcherService.class));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkStatusReceiver, intentFilter);

        IntentFilter serviceIntentFilter = new IntentFilter();
        serviceIntentFilter.addAction(PACKAGE_NAME);
        registerReceiver(serviceBroadcastReceiver, serviceIntentFilter);

        loadCachedPhoto();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkStatusReceiver);
        unregisterReceiver(serviceBroadcastReceiver);
    }
}