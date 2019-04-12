package com.kenneth.kihanya.download;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    public static final int MY_PERMISSION_CODE = 101;

    AppDownloadUtility appDownloadUtility = new AppDownloadUtility();
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder;
    @BindView(R.id.download)
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        checkPermissions();
    }

    public void checkPermissions() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSION_CODE);
        }
    }

    public boolean initiateNotification() {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.appbar_copia_logo)
                .setContentTitle("Copia Delivery")
                .setContentText("Downloading File")
                .setAutoCancel(true);

        notificationManager.notify(0, notificationBuilder.build());

        return false;
    }

    public void onCompleteAction() {
        if (initiateNotification())
            notificationManager.cancel(0);
        notificationBuilder.setProgress(0, 0, false);
        notificationBuilder.setContentText("File Downloaded");
        notificationManager.notify(0, notificationBuilder.build());

        File apkFile = new File(Environment.getExternalStorageDirectory() + "/DOWNLOAD/" +
                "copia-delivery.apk");
        if (apkFile.exists()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Toast.makeText(this, "Download Complete. Tap to install", Toast.LENGTH_SHORT).show();
                Uri apkUri = FileProvider.getUriForFile(this,
                        this.getString(R.string.file_provider_authority), apkFile);
                Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                intent.setData(apkUri);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                this.startActivity(intent);
            } else {
                Uri apkUri = Uri.fromFile(apkFile);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                this.startActivity(intent);
            }
        }
    }

    @OnClick(R.id.download)
    void downloadAction() {
        Intent intent = this.getIntent();
        if (intent != null) {

            appDownloadUtility.downloadApk();
            //oncomplete
            onCompleteAction();
        }


    }
}