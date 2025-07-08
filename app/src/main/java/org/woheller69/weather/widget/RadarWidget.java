package org.woheller69.weather.widget;


import static androidx.core.app.JobIntentService.enqueueWork;
import static org.woheller69.weather.database.SQLiteHelper.getWidgetCityID;
import static org.woheller69.weather.services.UpdateDataService.SKIP_UPDATE_INTERVAL;

import android.Manifest;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import org.woheller69.weather.R;
import org.woheller69.weather.activities.RainViewerActivity;
import org.woheller69.weather.database.CityToWatch;
import org.woheller69.weather.database.CurrentWeatherData;
import org.woheller69.weather.database.SQLiteHelper;
import org.woheller69.weather.services.UpdateDataService;
import org.woheller69.weather.services.WidgetUpdater;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class RadarWidget extends AppWidgetProvider {
    private static LocationListener locationListenerGPS;
    private LocationManager locationManager;
    public static Bitmap radarBitmap;
    public static long radarTimeGMT;
    public static int radarZoom;

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


    public static void updateView(Context context, int appWidgetId) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.radar_widget);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        int cityID = getWidgetCityID(context);
        SQLiteHelper dbHelper = SQLiteHelper.getInstance(context);
        CityToWatch city=dbHelper.getCityToWatch(cityID);
        CurrentWeatherData weatherData = dbHelper.getCurrentWeatherByCityId(cityID);

        int zoneseconds = weatherData.getTimeZoneSeconds();

        SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        if(prefManager.getBoolean("pref_GPS", false) && !prefManager.getBoolean("pref_GPS_manual", false)) views.setViewVisibility(R.id.location_on,View.VISIBLE); else views.setViewVisibility(R.id.location_on,View.GONE);
        views.setTextViewText(R.id.widget_city_name, city.getCityName());
        views.setInt(R.id.widget_background,"setAlpha", (int) ((100.0f - prefManager.getInt("pref_WidgetTransparency", 0)) * 255 / 100.0f));

        Intent intentUpdate = new Intent(context, RadarWidget.class);
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

        Intent intent2 = new Intent(context, RainViewerActivity.class);
        intent2.putExtra("latitude", city.getLatitude());
        intent2.putExtra("longitude", city.getLongitude());
        intent2.putExtra("timezoneseconds", zoneseconds);

        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent2, PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent2, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);

        if (radarBitmap != null) views.setImageViewBitmap(R.id.widget_radar_view, UpdateDataService.prepareRadarWidget(context, city, radarZoom, radarTimeGMT + zoneseconds *1000L, radarBitmap));

        // Now update radar
        Intent intent3 = new Intent(context, UpdateDataService.class);
        intent3.setAction(UpdateDataService.UPDATE_RADAR);
        intent3.putExtra("cityId", getWidgetCityID(context));
        enqueueWork(context, UpdateDataService.class, 0, intent3);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);

    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    @Override
    public void onUpdate(final Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        PeriodicWorkRequest widgetUpdateRequest =
                new PeriodicWorkRequest.Builder(WidgetUpdater.class,
                        20, TimeUnit.MINUTES)
                        .build();
        WorkManager
                .getInstance(context)
                .enqueueUniquePeriodicWork("widgetUpdateWork", ExistingPeriodicWorkPolicy.KEEP, widgetUpdateRequest);

        SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        if (locationManager==null) locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

        if(prefManager.getBoolean("pref_GPS", false) && !prefManager.getBoolean("pref_GPS_manual", false) && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && !powerManager.isPowerSaveMode()) {
            if (locationListenerGPS==null) {
                Log.d("GPS", "Listener null");
                locationListenerGPS = new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        // There may be multiple widgets active, so update all of them
                        Log.d("GPS", "Location changed");
                        int[] appWidgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, RadarWidget.class)); //IDs Might have changed since last call of onUpdate
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
        SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        prefManager.edit().remove("battery_optimization_prompt_count").apply();
        // Enter relevant functionality for when the first widget is created
        int[] widgetIDs = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, RadarWidget.class));

        for (int widgetID : widgetIDs) {
                RadarWidget.updateView(context, widgetID);
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

