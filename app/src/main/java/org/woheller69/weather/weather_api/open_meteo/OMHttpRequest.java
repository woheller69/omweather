package org.woheller69.weather.weather_api.open_meteo;

import android.content.Context;
import androidx.preference.PreferenceManager;

import android.content.SharedPreferences;
import android.text.TextUtils;

import org.woheller69.weather.BuildConfig;
import org.woheller69.weather.database.CityToWatch;
import org.woheller69.weather.preferences.AppPreferencesManager;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class OMHttpRequest {

    protected String getUrlForQueryingOMweatherAPI(Context context, float lat, float lon) {
        AppPreferencesManager prefManager =
                new AppPreferencesManager(PreferenceManager.getDefaultSharedPreferences(context));
        SharedPreferences sharedPreferences=PreferenceManager.getDefaultSharedPreferences(context);

        if (sharedPreferences.getBoolean("pref_snow", false)){
            return String.format(
                    "%sforecast?latitude=%s&longitude=%s&forecast_days=%s&hourly=temperature_2m,relativehumidity_2m,rain,showers,snowfall,weathercode,pressure_msl,windspeed_10m,winddirection_10m&daily=weathercode,temperature_2m_max,temperature_2m_min,sunrise,sunset,uv_index_max,rain_sum,showers_sum,snowfall_sum,windspeed_10m_max&current_weather=true&windspeed_unit=ms&timeformat=unixtime&timezone=auto",
                    BuildConfig.BASE_URL,
                    lat,
                    lon,
                    sharedPreferences.getInt("pref_number_days",7)
            );
        } else {
            return String.format(
                    "%sforecast?latitude=%s&longitude=%s&forecast_days=%s&hourly=temperature_2m,relativehumidity_2m,precipitation,weathercode,pressure_msl,windspeed_10m,winddirection_10m&daily=weathercode,temperature_2m_max,temperature_2m_min,sunrise,sunset,uv_index_max,precipitation_sum,windspeed_10m_max&current_weather=true&windspeed_unit=ms&timeformat=unixtime&timezone=auto",
                    BuildConfig.BASE_URL,
                    lat,
                    lon,
                    sharedPreferences.getInt("pref_number_days",7)
            );
        }


    }

}
