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
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.icu.util.LocaleData;
import android.icu.util.ULocale;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.woheller69.weather.R;
import org.woheller69.weather.activities.RainViewerActivity;
import org.woheller69.weather.database.CityToWatch;
import org.woheller69.weather.database.CurrentWeatherData;
import org.woheller69.weather.database.SQLiteHelper;
import org.woheller69.weather.services.UpdateDataService;
import org.woheller69.weather.ui.Help.StringFormatUtils;
import java.util.List;
import java.util.Locale;

public class RadarWidget extends AppWidgetProvider {
    private static LocationListener locationListenerGPS;
    private LocationManager locationManager;

    public void updateAppWidget(Context context, final int appWidgetId) {
        SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        SQLiteHelper db = SQLiteHelper.getInstance(context);
        if (!db.getAllCitiesToWatch().isEmpty()) {

            int cityID = getWidgetCityID(context);
            if(prefManager.getBoolean("pref_GPS", true) && !prefManager.getBoolean("pref_GPS_manual", false)) updateLocation(context, cityID,false);
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


    public static void updateView(Context context, AppWidgetManager appWidgetManager, RemoteViews views, int appWidgetId, CityToWatch city, CurrentWeatherData weatherData) {
        long time = weatherData.getTimestamp();
        int zoneseconds = weatherData.getTimeZoneSeconds();

        SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        if(prefManager.getBoolean("pref_GPS", true) && !prefManager.getBoolean("pref_GPS_manual", false)) views.setViewVisibility(R.id.location_on,View.VISIBLE); else views.setViewVisibility(R.id.location_on,View.GONE);
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

        RequestQueue queue = Volley.newRequestQueue(context);

        String url = "https://api.rainviewer.com/public/weather-maps.json";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    // Parse the JSON response
                    String host = "";
                    JSONArray radarFrames;
                    int lastPastFramePosition;
                    int zoom = 10;
                    try {
                        if (response != null && response.has("host")) host = response.getString("host");

                        //Store the radar frames and show current frame
                        if (response != null && response.has("radar") && response.getJSONObject("radar").has("past")){
                            radarFrames = response.getJSONObject("radar").getJSONArray("past");
                            lastPastFramePosition = radarFrames.length() - 1;
                            String radarUrl = host + radarFrames.getJSONObject(lastPastFramePosition).getString("path")+"/256/" + zoom +"/"+ city.getLatitude() +"/" + city.getLongitude() + "/2/1_1.png";
                            long radarTime = (Long.parseLong(radarFrames.getJSONObject(lastPastFramePosition).getString("time")) + zoneseconds) * 1000L;

                            // Download the image
                            ImageRequest imageRequest = new ImageRequest(radarUrl,
                                    response1 -> {

                                        // Create a new bitmap with the text
                                        Bitmap textBitmap = Bitmap.createBitmap(response1.getWidth(), response1.getHeight(), response1.getConfig());
                                        Canvas canvas = new Canvas(textBitmap);
                                        canvas.drawBitmap(response1, 0, 0, null); // draw the original image
                                        Paint paint = new Paint();
                                        paint.setColor(ContextCompat.getColor(context, R.color.lightgrey));
                                        paint.setTextSize(16);

                                        int widthTotalDistance = (int) (2 * 3.14 * 6378 * Math.abs(Math.cos(city.getLatitude() / 180 * 3.14)) / (Math.pow(2, zoom) * 256) * 256); ;
                                        String distanceUnit = context.getString(R.string.units_km);;
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                            if (LocaleData.getMeasurementSystem(ULocale.forLocale(Locale.getDefault())) != LocaleData.MeasurementSystem.SI){
                                                distanceUnit = context.getString(R.string.units_mi);
                                                widthTotalDistance = (int) (2 * 3.14 * 6378 * 0.6214 * Math.abs(Math.cos(city.getLatitude() / 180 * 3.14)) / (Math.pow(2, zoom) * 256) * 256);
                                            }
                                        }

                                        int widthDistanceMarker = getClosestMarker(widthTotalDistance / 10);
                                        int widthDistanceMarkerPixel = widthDistanceMarker * 256 / widthTotalDistance;

                                        paint.setStyle(Paint.Style.FILL);
                                        paint.setTextAlign(Paint.Align.LEFT);
                                        canvas.drawText(widthDistanceMarker + " " + distanceUnit, 10 + widthDistanceMarkerPixel + 10, 240 + 5, paint); // draw the text

                                        paint.setTextAlign(Paint.Align.RIGHT);
                                        canvas.drawText(StringFormatUtils.formatTimeWithoutZone(context, radarTime), 240, 240 + 5, paint);

                                        paint.setStyle(Paint.Style.STROKE);
                                        canvas.drawLine(10, 240, 10 + widthDistanceMarkerPixel, 240, paint);

                                        canvas.drawCircle(128, 128, widthDistanceMarkerPixel, paint);
                                        canvas.drawCircle(128, 128, 2 * widthDistanceMarkerPixel, paint);
                                        canvas.drawCircle(128, 128, 3 * widthDistanceMarkerPixel, paint);
                                        canvas.drawCircle(128, 128, 4 * widthDistanceMarkerPixel, paint);
                                        canvas.drawCircle(128, 128, 5 * widthDistanceMarkerPixel, paint);
                                        paint.setStyle(Paint.Style.FILL);
                                        canvas.drawCircle(128, 128, 2, paint);

                                        //Round off corners
                                        Paint clearPaint = new Paint();
                                        clearPaint.setStyle(Paint.Style.STROKE);
                                        clearPaint.setStrokeWidth(20.0f);
                                        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                                        canvas.drawRoundRect(-10, -10,265, 265, 30, 30, clearPaint);

                                        views.setImageViewBitmap(R.id.widget_radar_view, textBitmap);

                                        appWidgetManager.updateAppWidget(appWidgetId, views);
                                    },
                                    0, 0, ImageView.ScaleType.CENTER_CROP, Bitmap.Config.RGB_565,
                                    error1 -> {
                                        // Handle the error
                                    });
                            queue.add(imageRequest);

                        }

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                },
                error -> {
                    // Handle the error
                });

        queue.add(request);


        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static int getClosestMarker(int value) {
        int[] markers = {1, 2, 3, 5, 10, 20, 30, 50, 100};
        int closest = markers[0];
        int minDiff = Math.abs(value - closest);
        for (int i = 1; i < markers.length; i++) {
            int diff = Math.abs(value - markers[i]);
            if (diff < minDiff) {
                minDiff = diff;
                closest = markers[i];
            }
        }
        return closest;
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
            if(prefManager.getBoolean("pref_GPS", true) && !prefManager.getBoolean("pref_GPS_manual", false) && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && !powerManager.isPowerSaveMode()) {
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
        // Enter relevant functionality for when the first widget is created
        SQLiteHelper dbHelper = SQLiteHelper.getInstance(context);

        int widgetCityID=getWidgetCityID(context);

        CurrentWeatherData currentWeather=dbHelper.getCurrentWeatherByCityId(widgetCityID);

        int[] widgetIDs = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, RadarWidget.class));

        for (int widgetID : widgetIDs) {

                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.radar_widget);
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

                CityToWatch city=dbHelper.getCityToWatch(widgetCityID);

                RadarWidget.updateView(context, appWidgetManager, views, widgetID, city, currentWeather);
                appWidgetManager.updateAppWidget(widgetID, views);

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
            if(prefManager.getBoolean("pref_GPS", true) && !prefManager.getBoolean("pref_GPS_manual", false)) updateLocation(context, cityID,true);
        }
        super.onReceive(context,intent);
    }
}

