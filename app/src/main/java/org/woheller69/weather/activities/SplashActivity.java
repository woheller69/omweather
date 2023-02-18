package org.woheller69.weather.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import org.woheller69.weather.firststart.TutorialActivity;
import org.woheller69.weather.preferences.AppPreferencesManager;

/**
 * Created by yonjuni on 24.10.16.
 */

public class SplashActivity extends AppCompatActivity {
    private AppPreferencesManager prefManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefManager = new AppPreferencesManager(PreferenceManager.getDefaultSharedPreferences(this));
        if (prefManager.isFirstTimeLaunch(this)){  //First time got to TutorialActivity
            Intent mainIntent = new Intent(SplashActivity.this, TutorialActivity.class);
            SplashActivity.this.startActivity(mainIntent);
        } else { //otherwise directly start ForecastCityActivity
            Intent mainIntent = new Intent(SplashActivity.this, ForecastCityActivity.class);
            SplashActivity.this.startActivity(mainIntent);
        }
        SplashActivity.this.finish();
    }

}
