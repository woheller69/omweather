package org.woheller69.weather.widget;


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
import static org.woheller69.weather.database.SQLiteHelper.getWidgetCityID;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static androidx.core.app.JobIntentService.enqueueWork;

import static org.woheller69.weather.services.UpdateDataService.SKIP_UPDATE_INTERVAL;

public class WeatherWidget extends AppWidgetProvider {
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
                if (manual) Toast.makeText(context.getApplicationContext(),R.string.error_no_position,Toast.LENGTH_SHORT).show(); //show toast only if manual update by refresh button
            }

        }
    }



    public static void updateView(Context context, AppWidgetManager appWidgetManager, RemoteViews views, int appWidgetId, CityToWatch city, CurrentWeatherData weatherData, List<WeekForecast> weekforecasts, List<HourlyForecast> hourlyforecasts) {
        SQLiteHelper dbHelper = SQLiteHelper.getInstance(context);
        long time = weatherData.getTimestamp();
        int zoneseconds = weatherData.getTimeZoneSeconds();
        int [] forecastIDs = {R.id.widget_hour12,R.id.widget_hour1, R.id.widget_hour2,R.id.widget_hour3,R.id.widget_hour4,R.id.widget_hour5,R.id.widget_hour6,R.id.widget_hour7, R.id.widget_hour8,R.id.widget_hour9,R.id.widget_hour10,R.id.widget_hour11};
        int [] windIDs = {R.id.widget_windicon_hour12,R.id.widget_windicon_hour1,R.id.widget_windicon_hour2,R.id.widget_windicon_hour3,R.id.widget_windicon_hour4,R.id.widget_windicon_hour5,R.id.widget_windicon_hour6,R.id.widget_windicon_hour7,R.id.widget_windicon_hour8,R.id.widget_windicon_hour9,R.id.widget_windicon_hour10,R.id.widget_windicon_hour11};
        long updateTime = (time + zoneseconds) * 1000;

        long riseTime = (weatherData.getTimeSunrise() + zoneseconds) * 1000;
        long setTime = (weatherData.getTimeSunset() + zoneseconds) * 1000;

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
            views.setViewVisibility(R.id.widget_attribution,View.VISIBLE);
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

            views.setViewVisibility(R.id.widget_attribution,View.VISIBLE);
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
                    views.setViewVisibility(R.id.widget_attribution,View.INVISIBLE);
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
                    views.setViewVisibility(R.id.widget_attribution,View.INVISIBLE);
                    views.setViewVisibility(R.id.widget_precipitation_forecast,View.VISIBLE);
                }
            }
            views.setImageViewResource(R.id.widget_image_view, UiResourceProvider.getIconResourceForWeatherCategory(next.getWeatherID(), isDay));
            views.setTextViewText(R.id.widget_temperature, " "+StringFormatUtils.formatTemperature(context, next.getTemperature())+" ");
            views.setImageViewResource(R.id.widget_windicon,StringFormatUtils.colorWindSpeedWidget(next.getWindSpeed()));
        }

        SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        if(prefManager.getBoolean("pref_GPS", false) && !prefManager.getBoolean("pref_GPS_manual", false)) views.setViewVisibility(R.id.location_on,View.VISIBLE); else views.setViewVisibility(R.id.location_on,View.GONE);
        views.setTextViewText(R.id.widget_updatetime, String.format("(%s)", StringFormatUtils.formatTimeWithoutZone(context, updateTime)));
        views.setViewPadding(R.id.widget_temperature,1,1,1,1);
        views.setTextViewText(R.id.widget_max_Temp,StringFormatUtils.formatTemperature(context, weekforecasts.get(0).getMaxTemperature()));
        views.setTextViewText(R.id.widget_min_Temp,StringFormatUtils.formatTemperature(context, weekforecasts.get(0).getMinTemperature()));
        views.setTextViewText(R.id.widget_city_name, city.getCityName());
        views.setInt(R.id.widget_background,"setAlpha", (int) ((100.0f - prefManager.getInt("pref_WidgetTransparency", 0)) * 255 / 100.0f));

        if ((riseTime - setTime) % 86400 == 0) views.setTextViewText(R.id.widget_sunrise_sunset,"\u2600\u25b2 --:--" + " \u25bc --:--");
        else  {
            views.setTextViewText(R.id.widget_sunrise_sunset,"\u2600\u25b2\u2009" + StringFormatUtils.formatTimeWithoutZone(context, riseTime) + " \u25bc\u2009" + StringFormatUtils.formatTimeWithoutZone(context, setTime));
        }

        if (weekforecasts.get(0).getUv_index()==-1f) {
            views.setViewVisibility(R.id.widget_UVindex,View.GONE);
        } else {
            views.setViewVisibility(R.id.widget_UVindex,View.VISIBLE);
            views.setTextViewText(R.id.widget_UVindex,"UV");
            views.setInt(R.id.widget_UVindex,"setBackgroundResource",StringFormatUtils.widgetColorUVindex(context,Math.round(weekforecasts.get(0).getUv_index())));
        }



        for (int i=0;i<forecastIDs.length;i++){
            views.setImageViewBitmap(forecastIDs[i],null);
            views.setImageViewBitmap(windIDs[i],null);
        }

        if (hourlyforecasts!=null&&!hourlyforecasts.isEmpty())
        {
            List<HourlyForecast> templist = new ArrayList<>();  //remove outdated forecasts
            for (HourlyForecast f : hourlyforecasts) {
                if (f.getForecastTime()>=System.currentTimeMillis() - (1 * 60 * 60 * 1000)) templist.add(f);
            }
            hourlyforecasts = templist;
            for (int i=1;i<forecastIDs.length;i++){
                Calendar forecastTime = Calendar.getInstance();
                forecastTime.setTimeZone(TimeZone.getTimeZone("GMT"));
                forecastTime.setTimeInMillis(hourlyforecasts.get(i).getLocalForecastTime(context));
                int hour = forecastTime.get(Calendar.HOUR) % 12;
                if (weatherData.getTimeSunrise()==0 || weatherData.getTimeSunset()==0){
                    if ((dbHelper.getCityToWatch(hourlyforecasts.get(i).getCity_id()).getLatitude())>0){  //northern hemisphere
                        isDay= forecastTime.get(Calendar.DAY_OF_YEAR) >= 80 && forecastTime.get(Calendar.DAY_OF_YEAR) <= 265; //from March 21 to September 22 (incl)
                    }else{ //southern hemisphere
                        isDay= forecastTime.get(Calendar.DAY_OF_YEAR) < 80 || forecastTime.get(Calendar.DAY_OF_YEAR) > 265;
                    }
                }else {
                    Calendar sunSetTime = Calendar.getInstance();
                    sunSetTime.setTimeZone(TimeZone.getTimeZone("GMT"));
                    sunSetTime.setTimeInMillis(weatherData.getTimeSunset() * 1000 + weatherData.getTimeZoneSeconds() * 1000L);
                    sunSetTime.set(Calendar.DAY_OF_YEAR, forecastTime.get(Calendar.DAY_OF_YEAR));
                    sunSetTime.set(Calendar.YEAR, forecastTime.get(Calendar.YEAR));

                    Calendar sunRiseTime = Calendar.getInstance();
                    sunRiseTime.setTimeZone(TimeZone.getTimeZone("GMT"));
                    sunRiseTime.setTimeInMillis(weatherData.getTimeSunrise() * 1000 + weatherData.getTimeZoneSeconds() * 1000L);
                    sunRiseTime.set(Calendar.DAY_OF_YEAR, forecastTime.get(Calendar.DAY_OF_YEAR));
                    sunRiseTime.set(Calendar.YEAR, forecastTime.get(Calendar.YEAR));

                    isDay = forecastTime.after(sunRiseTime) && forecastTime.before(sunSetTime);
                }
                views.setImageViewResource(forecastIDs[hour],UiResourceProvider.getIconResourceForWeatherCategory(hourlyforecasts.get(i).getWeatherID(), isDay));
                views.setImageViewResource(windIDs[hour],StringFormatUtils.colorWindSpeedWidget(hourlyforecasts.get(i).getWindSpeed()));
            }
        }

        Intent intentUpdate = new Intent(context, WeatherWidget.class);
        intentUpdate.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] idArray = new int[]{appWidgetId};
        intentUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, idArray);
        intentUpdate.putExtra("Manual",true);

        PendingIntent pendingUpdate;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingUpdate = PendingIntent.getBroadcast(context, appWidgetId, intentUpdate, PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingUpdate = PendingIntent.getBroadcast(context, appWidgetId, intentUpdate, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        views.setOnClickPendingIntent(R.id.widget_update, pendingUpdate);

        Intent intent2 = new Intent(context, ForecastCityActivity.class);
        intent2.putExtra("cityId", getWidgetCityID(context));
        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent2, PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent2, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);

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
                        public void onLocationChanged(android.location.Location location) {
                            // There may be multiple widgets active, so update all of them
                            Log.d("GPS", "Location changed");
                            int[] appWidgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, WeatherWidget.class)); //IDs Might have changed since last call of onUpdate
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
        List<HourlyForecast> hourlyforecasts=dbHelper.getForecastsByCityId(widgetCityID);

        int[] widgetIDs = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, WeatherWidget.class));

        for (int widgetID : widgetIDs) {

                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weather_widget);
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

                CityToWatch city=dbHelper.getCityToWatch(widgetCityID);

                WeatherWidget.updateView(context, appWidgetManager, views, widgetID, city, currentWeather,weekforecasts, hourlyforecasts);

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

