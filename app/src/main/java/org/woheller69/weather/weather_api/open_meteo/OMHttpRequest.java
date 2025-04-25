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
                    "%sforecast?latitude=%s&longitude=%s&forecast_days=%s&hourly=%s,uv_index,relative_humidity_2m,rain,showers,snowfall,weather_code,pressure_msl,wind_speed_10m,wind_direction_10m&minutely_15=%s,rain,showers,snowfall,weather_code,wind_speed_10m&forecast_minutely_15=60&daily=weather_code,%s,%s,sunrise,sunset,uv_index_max,rain_sum,showers_sum,snowfall_sum,wind_speed_10m_max,sunshine_duration&current_weather=true&wind_speed_unit=ms&timeformat=unixtime&timezone=auto",
                    BuildConfig.BASE_URL,
                    lat,
                    lon,
                    sharedPreferences.getInt("pref_number_days",7),
                    sharedPreferences.getBoolean("pref_apparentTemp",false) ? "apparent_temperature" : "temperature_2m",
                    sharedPreferences.getBoolean("pref_apparentTemp",false) ? "apparent_temperature" : "temperature_2m",
                    sharedPreferences.getBoolean("pref_apparentTemp",false) ? "apparent_temperature_max" : "temperature_2m_max",
                    sharedPreferences.getBoolean("pref_apparentTemp",false) ? "apparent_temperature_min" : "temperature_2m_min"
            );
        } else {
            return String.format(
                    "%sforecast?latitude=%s&longitude=%s&forecast_days=%s&hourly=%s,uv_index,relative_humidity_2m,precipitation,weather_code,pressure_msl,wind_speed_10m,wind_direction_10m&minutely_15=%s,precipitation,weather_code,wind_speed_10m&forecast_minutely_15=60&daily=weather_code,%s,%s,sunrise,sunset,uv_index_max,precipitation_sum,wind_speed_10m_max,sunshine_duration&current_weather=true&wind_speed_unit=ms&timeformat=unixtime&timezone=auto",
                    BuildConfig.BASE_URL,
                    lat,
                    lon,
                    sharedPreferences.getInt("pref_number_days",7),
                    sharedPreferences.getBoolean("pref_apparentTemp",false) ? "apparent_temperature" : "temperature_2m",
                    sharedPreferences.getBoolean("pref_apparentTemp",false) ? "apparent_temperature" : "temperature_2m",
                    sharedPreferences.getBoolean("pref_apparentTemp",false) ? "apparent_temperature_max" : "temperature_2m_max",
                    sharedPreferences.getBoolean("pref_apparentTemp",false) ? "apparent_temperature_min" : "temperature_2m_min"
            );
        }


    }

}
