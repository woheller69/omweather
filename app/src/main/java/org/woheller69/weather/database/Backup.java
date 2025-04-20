package org.woheller69.weather.database;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.woheller69.weather.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Backup {
    public static final int PERMISSION_REQUEST_CODE = 123;

    public static boolean checkPermissionStorage (Context context) {
            int result = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
            int result1 = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                return true;
            } else {
                return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
        }
    }

    public static void requestPermission(Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setIcon(R.drawable.ic_warning_amber_black_24dp);
        builder.setTitle(activity.getResources().getString(R.string.permission_required));
        builder.setMessage(activity.getResources().getString(R.string.permission_message,activity.getResources().getString(R.string.app_name)));
        builder.setPositiveButton(R.string.dialog_OK_button, (dialog, which) -> {
            dialog.cancel();
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        });
        builder.setNegativeButton(R.string.dialog_NO_button, (dialog, whichButton) -> dialog.cancel());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)  //menu item is removed if < SDK26
    public static void zipExtract(Context context, File targetDir, Uri zipFile) {
        ZipEntry zipEntry;
        int readLen;
        byte[] readBuffer = new byte[4096];
        try {
            InputStream src = context.getContentResolver().openInputStream(zipFile);
            try {
                try (ZipInputStream zipInputStream = new ZipInputStream(src)) {
                    while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                        File extractedFile = new File(targetDir ,zipEntry.getName());
                        try (OutputStream outputStream = Files.newOutputStream(extractedFile.toPath())) {
                            while ((readLen = zipInputStream.read(readBuffer)) != -1) {
                                outputStream.write(readBuffer, 0, readLen);
                            }
                        }
                    }
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
