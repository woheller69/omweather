package org.woheller69.weather.activities;

import static android.os.Environment.DIRECTORY_DOCUMENTS;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import org.woheller69.weather.database.Backup;
import org.woheller69.weather.R;
import org.woheller69.weather.ui.util.ThemeUtils;

import java.io.File;
import java.util.Objects;


public class BackupRestoreActivity extends NavigationActivity{
    ActivityResultLauncher<Intent> mRestore;

    @RequiresApi(api = Build.VERSION_CODES.O)  //Backup only available if SDK > 26
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backuprestore);
        ThemeUtils.setStatusBarAppearance(this);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mRestore = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    File intData = new File(Environment.getDataDirectory() + "//data//" + this.getPackageName());
                    if (result.getData()!=null && result.getData().getData()!=null) Backup.zipExtract(this, intData, result.getData().getData());
                });
    }

    @Override
    protected int getNavigationDrawerID() {
        return R.id.nav_backuprestore;
    }

    public void performBackup(View view) {
        File extStorage;
        File intData;
        intData = new File(Environment.getDataDirectory()+"//data//" + this.getPackageName() + "//databases//");
        extStorage = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS);
        if (!extStorage.exists()) extStorage.mkdir();
        String filesBackup = getResources().getString(R.string.app_name)+".zip";
        final File dbBackup = new File(extStorage, filesBackup);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.backup_database) +" -> " + dbBackup.toString());
        builder.setPositiveButton(R.string.dialog_OK_button, (dialog, whichButton) -> {
            if (!Backup.checkPermissionStorage(this)) {
                Backup.requestPermission(this);
            } else {
                if (dbBackup.exists()){
                    if (!dbBackup.delete()){
                        Toast.makeText(this,getResources().getString(R.string.toast_delete), Toast.LENGTH_LONG).show();
                    }
                }
                try {
                    new ZipFile(dbBackup).addFolder(intData);
                } catch (ZipException e) {
                    Toast.makeText(this,e.toString(), Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setNegativeButton(R.string.dialog_NO_button, (dialog, whichButton) -> dialog.cancel());
        AlertDialog dialog = builder.create();
        dialog.show();
        Objects.requireNonNull(dialog.getWindow()).setGravity(Gravity.BOTTOM);

    }

    @RequiresApi(api = Build.VERSION_CODES.O) //Restore only available if SDK > 26
    public void performRestore(View view) {
        File extStorage;
        File intData;
        intData = new File(Environment.getDataDirectory() + "//data//" + this.getPackageName());
        extStorage = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS);
        String filesBackup = getResources().getString(R.string.app_name)+".zip";
        final File zipFileBackup = new File(extStorage, filesBackup);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.restore_database_message));
        builder.setPositiveButton(R.string.dialog_OK_button, (dialog, whichButton) -> {
            if (!Backup.checkPermissionStorage(this)) {
                Backup.requestPermission(this);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.setType("application/zip");
                    mRestore.launch(intent);
                } else {
                    Backup.zipExtract(this, intData, Uri.fromFile(zipFileBackup));
                }
            }
        });
        builder.setNegativeButton(R.string.dialog_NO_button, (dialog, whichButton) -> dialog.cancel());
        AlertDialog dialog = builder.create();
        dialog.show();
        Objects.requireNonNull(dialog.getWindow()).setGravity(Gravity.BOTTOM);
    }
}
