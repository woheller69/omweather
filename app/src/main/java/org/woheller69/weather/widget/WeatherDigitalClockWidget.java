package org.woheller69.weather.widget;


import static androidx.core.app.JobIntentService.enqueueWork;
import static org.woheller69.weather.database.SQLiteHelper.getWidgetCityID;
import static org.woheller69.weather.services.UpdateDataService.SKIP_UPDATE_INTERVAL;
import static java.lang.Boolean.TRUE;

import android.Manifest;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import org.woheller69.weather.R;
import org.woheller69.weather.activities.ForecastCityActivity;
import org.woheller69.weather.database.CityToWatch;
import org.woheller69.weather.database.CurrentWeatherData;
import org.woheller69.weather.database.HourlyForecast;
import org.woheller69.weather.database.QuarterHourlyForecast;
import org.woheller69.weather.database.SQLiteHelper;
import org.woheller69.weather.database.WeekForecast;
import org.woheller69.weather.services.UpdateDataService;
import org.woheller69.weather.ui.Help.StringFormatUtils;
import org.woheller69.weather.ui.UiResourceProvider;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class WeatherDigitalClockWidget extends AppWidgetProvider {
    private static LocationListener locationListenerGPS;
    private LocationManager locationManager;

    public void updateAppWidget(Context context, final int appWidgetId) {
        SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        SQLiteHelper db = SQLiteHelper.getInstance(context);
        if (!db.getAllCitiesToWatch().isEmpty()) {

            int cityID = getWidgetCityID(context);
            if(prefManager.getBoolean("pref_GPS", false) && !prefManager.getBoolean("pref_GPS_manual", false)) updateLocation(context, cityID,false);
            Intent intent = new Intent(context, UpdateDataService.class);
            intent.setAction(UpdateDataService.UPDATE_SINGLE_ACTION);
            intent.putExtra("cityId", cityID);
            intent.putExtra(SKIP_UPDATE_INTERVAL, true);
            enqueueWork(context, UpdateDataService.class, 0, intent);
        }
    }

    public static void updateLocation(final Context context, int cityID, boolean manual) {
        SQLiteHelper db = SQLiteHelper.getInstance(context);
        List<CityToWatch> cities = db.getAllCitiesToWatch();

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (locationGPS != null) {
                CityToWatch city;
                double lat = locationGPS.getLatitude();
                double lon = locationGPS.getLongitude();
                for (int i=0; i<cities.size();i++){
                    if (cities.get(i).getCityId()==cityID) {
                        city = cities.get(i);
                        city.setLatitude((float) lat);
                        city.setLongitude((float) lon);
                        city.setCityName(String.format(Locale.getDefault(),"%.2f° / %.2f°", lat, lon));
                        db.updateCityToWatch(city);

                        break;
                    }
                }
            } else {
                if (manual) Toast.makeText(context.getApplicationContext(), R.string.error_no_position,Toast.LENGTH_SHORT).show(); //show toast only if manual update by refresh button
            }

        }
    }

    public static void updateView(Context context, AppWidgetManager appWidgetManager, RemoteViews views, int appWidgetId, CityToWatch city, CurrentWeatherData weatherData, List<WeekForecast> weekforecasts) {
        SQLiteHelper dbHelper = SQLiteHelper.getInstance(context);

        SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        views.setInt(R.id.widget_background,"setAlpha",  (int) ((100.0f - prefManager.getInt("pref_WidgetTransparency", 0)) * 255 / 100.0f));
        if (prefManager.getBoolean("pref_TimeFormat", true)==TRUE)
            views.setCharSequence(R.id.textClockTime,"setFormat12Hour","HH:mm");
        else
            views.setCharSequence(R.id.textClockTime,"setFormat12Hour","hh:mm aa");

        DateFormat df = DateFormat.getDateInstance(DateFormat.LONG);
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        String pattern = ((SimpleDateFormat) df).toPattern();
        views.setCharSequence(R.id.textClockDate,"setFormat12Hour",pattern);
        views.setCharSequence(R.id.textClockDate,"setFormat24Hour",pattern);

        if(prefManager.getBoolean("pref_GPS", false) && !prefManager.getBoolean("pref_GPS_manual", false)) views.setViewVisibility(R.id.location_on,View.VISIBLE); else views.setViewVisibility(R.id.location_on,View.INVISIBLE);
        views.setTextViewText(R.id.widget_city_name, city.getCityName());

        views.setTextViewText(R.id.widget_UVindex,"UV");
        views.setInt(R.id.widget_UVindex,"setBackgroundResource",StringFormatUtils.widgetColorUVindex(context,Math.round(weekforecasts.get(0).getUv_index())));

        boolean isDay = weatherData.isDay(context);

        if (!dbHelper.hasQuarterHourly(weatherData.getCity_id())){
            HourlyForecast nowCast = new HourlyForecast();
            List<HourlyForecast> hourlyForecasts = dbHelper.getForecastsByCityId(weatherData.getCity_id());
            for (HourlyForecast f : hourlyForecasts) {
                if (Math.abs(f.getForecastTime() - System.currentTimeMillis()) <= 30 * 60 * 1000) {
                    nowCast = f;
                    break;
                }
            }
            views.setImageViewResource(R.id.widget_image_view, UiResourceProvider.getIconResourceForWeatherCategory(nowCast.getWeatherID(), isDay));
            views.setTextViewText(R.id.widget_temperature, " "+StringFormatUtils.formatTemperature(context, nowCast.getTemperature())+" ");
            views.setImageViewResource(R.id.widget_windicon,StringFormatUtils.colorWindSpeedWidget(nowCast.getWindSpeed()));
            views.setTextViewText(R.id.widget_precipitation_forecast,"");
            views.setViewVisibility(R.id.widget_precipitation_forecast,View.INVISIBLE);
        } else {
            QuarterHourlyForecast next = new QuarterHourlyForecast();
            List<QuarterHourlyForecast> quarterHourlyForecasts = dbHelper.getQuarterHourlyForecastsByCityId(weatherData.getCity_id());
            for (QuarterHourlyForecast f : quarterHourlyForecasts) {
                if (f.getForecastTime() > System.currentTimeMillis()) { //take first 15 min instant after now
                    next = f;
                    break;
                }
            }
            views.setTextViewText(R.id.widget_precipitation_forecast,"");
            views.setViewVisibility(R.id.widget_precipitation_forecast,View.INVISIBLE);

            if (next.getPrecipitation()>0){ //raining now
                QuarterHourlyForecast nextWithoutPrecipitation = null;
                int count=0;
                for (QuarterHourlyForecast f : quarterHourlyForecasts) {
                    if (f.getForecastTime() > System.currentTimeMillis() && f.getPrecipitation()==0) {
                        if (count == 0) nextWithoutPrecipitation = f;  //set when first event without precipitation is found
                        count++;
                        if (count >= 2) break;            //stop if 2 quarter-hours without precipitation
                    } else count=0;                       //reset counter if quarter-hour with precipitation is found
                }
                if (nextWithoutPrecipitation!=null && (nextWithoutPrecipitation.getForecastTime()-System.currentTimeMillis()) <= 12* 60 * 60 * 1000)  {  //if rain stops within 12 hours show closed umbrella
                    views.setTextViewText(R.id.widget_precipitation_forecast,"🌂 "+StringFormatUtils.formatTimeWithoutZone(context, nextWithoutPrecipitation.getLocalForecastTime(context)-15*60*1000)); //forecast is for preceding 15min
                    views.setViewVisibility(R.id.widget_precipitation_forecast,View.VISIBLE);
                }
            } else {
                QuarterHourlyForecast nextPrecipitation = null;
                for (QuarterHourlyForecast f : quarterHourlyForecasts) {
                    if (f.getForecastTime() > System.currentTimeMillis() && f.getPrecipitation()>0) {
                        nextPrecipitation = f;
                        break;
                    }
                }
                if (nextPrecipitation!=null && (nextPrecipitation.getForecastTime()-System.currentTimeMillis()) <= 12* 60 * 60 * 1000)  {  //if rain starts within 12 hours show umbrella
                    views.setTextViewText(R.id.widget_precipitation_forecast,"☔ "+StringFormatUtils.formatTimeWithoutZone(context, nextPrecipitation.getLocalForecastTime(context)-15*60*1000)); //forecast is for preceding 15min
                    views.setViewVisibility(R.id.widget_precipitation_forecast,View.VISIBLE);
                }
            }

            views.setImageViewResource(R.id.widget_image_view, UiResourceProvider.getIconResourceForWeatherCategory(next.getWeatherID(), isDay));
            views.setTextViewText(R.id.widget_temperature, " "+StringFormatUtils.formatTemperature(context, next.getTemperature())+" ");
            views.setImageViewResource(R.id.widget_windicon,StringFormatUtils.colorWindSpeedWidget(next.getWindSpeed()));
        }

        Intent intent2 = new Intent(context, ForecastCityActivity.class);
        intent2.putExtra("cityId", getWidgetCityID(context));
        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent2, PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent2, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        views.setOnClickPendingIntent(R.id.widget_digital_clock_layout, pendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    @Override
    public void onUpdate(final Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        if (locationManager==null) locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        Log.d("GPS", "Widget onUpdate");
            if(prefManager.getBoolean("pref_GPS", false) && !prefManager.getBoolean("pref_GPS_manual", false) && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && !powerManager.isPowerSaveMode()) {
                if (locationListenerGPS==null) {
                    Log.d("GPS", "Listener null");
                    locationListenerGPS = new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            // There may be multiple widgets active, so update all of them
                            Log.d("GPS", "Location changed");
                            int[] appWidgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, WeatherDigitalClockWidget.class)); //IDs Might have changed since last call of onUpdate
                            for (int appWidgetId : appWidgetIds) {
                                updateAppWidget(context, appWidgetId);
                            }
                        }

                        @Deprecated
                        @Override
                        public void onStatusChanged(String provider, int status, Bundle extras) {
                        }

                        @Override
                        public void onProviderEnabled(String provider) {
                        }

                        @Override
                        public void onProviderDisabled(String provider) {
                        }
                    };
                    Log.d("GPS", "Request Updates");
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 600000, 3000, locationListenerGPS);  //Update every 10 min, min distance 5km
                }
            }else {
                Log.d("GPS","Remove Updates");
                if (locationListenerGPS!=null) locationManager.removeUpdates(locationListenerGPS);
                locationListenerGPS=null;
            }

        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        SQLiteHelper dbHelper = SQLiteHelper.getInstance(context);

        int widgetCityID=getWidgetCityID(context);

        CurrentWeatherData currentWeather=dbHelper.getCurrentWeatherByCityId(widgetCityID);
        List<WeekForecast> weekforecasts=dbHelper.getWeekForecastsByCityId(widgetCityID);

        int[] widgetIDs = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, WeatherDigitalClockWidget.class));

        for (int widgetID : widgetIDs) {

                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weather_digital_clock_widget);
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

                CityToWatch city=dbHelper.getCityToWatch(widgetCityID);

                WeatherDigitalClockWidget.updateView(context, appWidgetManager, views, widgetID, city, currentWeather, weekforecasts);

        }
     }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        Log.d("GPS", "Last widget removed");
        if (locationManager==null) locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationListenerGPS!=null) locationManager.removeUpdates(locationListenerGPS);
        locationListenerGPS=null;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getBooleanExtra("Manual", false)) {
            int cityID = getWidgetCityID(context);
            SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
            if(prefManager.getBoolean("pref_GPS", false) && !prefManager.getBoolean("pref_GPS_manual", false)) updateLocation(context, cityID,true);
        }
        super.onReceive(context,intent);
    }
}

