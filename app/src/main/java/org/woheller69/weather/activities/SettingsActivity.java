package org.woheller69.weather.activities;


import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SeekBarPreference;

import org.woheller69.weather.R;
import org.woheller69.weather.database.SQLiteHelper;

import static java.lang.Boolean.TRUE;

public class SettingsActivity extends NavigationActivity implements SharedPreferences.OnSharedPreferenceChangeListener{

    @Override
    protected void onRestart() {
        super.onRestart();

        recreate();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                        String message = getString(R.string.rationale_background_location);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            message = message + ": \n\n >> " + getPackageManager().getBackgroundPermissionOptionLabel().toString() +" <<";
                        }

                        alertDialogBuilder.setMessage(message);
                        alertDialogBuilder.setPositiveButton(getString(R.string.dialog_OK_button), (dialog, which) -> requestBackgroundLocation());
                        alertDialogBuilder.setNegativeButton(getString(R.string.dialog_NO_button), (dialog, which) -> {
                        });

                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();

                    }
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void requestBackgroundLocation() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 2);
    }

    @Override
    protected void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

    }

    @Override
    protected int getNavigationDrawerID() {
        return R.id.nav_settings;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

        if (s.equals("pref_GPS")) {
            if (sharedPreferences.getBoolean("pref_GPS", false) == TRUE) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                    } else {
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 1);
                    }

                }
            }
        } else if (s.equals("pref_apparentTemp") || s.equals("pref_showPressure") || s.equals("pref_showHourlyUvIndex") || s.equals("pref_snow")) {
            SQLiteHelper database = SQLiteHelper.getInstance(getApplicationContext().getApplicationContext());
            database.deleteAllForecasts();
        }
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    public static class GeneralPreferenceFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
            public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
                setPreferencesFromResource(R.xml.pref_general, rootKey);
        }
        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
            super.onPause();
        }
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals("pref_number_days")){
                SeekBarPreference numberDays = findPreference("pref_number_days");
                if (numberDays.getValue()<3) numberDays.setValue(3);
            }
        }
    }
}
