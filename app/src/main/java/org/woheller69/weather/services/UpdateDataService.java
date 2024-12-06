package org.woheller69.weather.services;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.icu.util.LocaleData;
import android.icu.util.ULocale;
import android.os.Build;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.app.JobIntentService;

import android.util.Log;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.woheller69.weather.BuildConfig;
import org.woheller69.weather.R;
import org.woheller69.weather.activities.NavigationActivity;
import org.woheller69.weather.database.CityToWatch;
import org.woheller69.weather.database.HourlyForecast;
import org.woheller69.weather.database.SQLiteHelper;
import org.woheller69.weather.ui.Help.StringFormatUtils;
import org.woheller69.weather.weather_api.IHttpRequestForWeatherAPI;
import org.woheller69.weather.weather_api.open_meteo.OMHttpRequestForWeatherAPI;
import org.woheller69.weather.widget.RadarWidget;
import org.woheller69.weather.widget.WeatherWidgetAllInOne;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * This class provides the functionality to fetch forecast data for a given city as a background
 * task.
 */
public class UpdateDataService extends JobIntentService {

    public static final String UPDATE_SINGLE_ACTION = "org.woheller69.weather.services.UpdateDataService.UPDATE_SINGLE_ACTION";
    public static final String UPDATE_RADAR = "org.woheller69.weather.services.UpdateDataService.UPDATE_RADAR";
    public static final String SKIP_UPDATE_INTERVAL = "skipUpdateInterval";
    private static final long MIN_UPDATE_INTERVAL = 20;

    private SQLiteHelper dbHelper;

    /**
     * Constructor.
     */
    public UpdateDataService() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper = SQLiteHelper.getInstance(getApplicationContext());
    }

    @Override
    protected void onHandleWork(Intent intent) {
        if (!isOnline(2000)) {
            Handler h = new Handler(getApplicationContext().getMainLooper());
            h.post(() -> {
                if (NavigationActivity.isVisible) Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_no_internet), Toast.LENGTH_LONG).show();
            });
            return;
        }

        if (intent != null) {
            if (UPDATE_SINGLE_ACTION.equals(intent.getAction())) {
                handleUpdateSingle(intent);
            }else if (UPDATE_RADAR.equals(intent.getAction())){
                int cityId = intent.getIntExtra("cityId", -1);
                if (cityId == SQLiteHelper.getWidgetCityID(getApplicationContext())) {
                    int numRadarWidgets = AppWidgetManager.getInstance(getApplicationContext()).getAppWidgetIds(new ComponentName(getApplicationContext(), RadarWidget.class)).length;
                    int numAllInOneWidgets = AppWidgetManager.getInstance(getApplicationContext()).getAppWidgetIds(new ComponentName(getApplicationContext(), WeatherWidgetAllInOne.class)).length;
                    if (numRadarWidgets + numAllInOneWidgets > 0) handleUpdateRadar(intent);
                }
            }
        }
    }

    private void handleUpdateRadar(Intent intent) {
        int cityId = intent.getIntExtra("cityId",-1);
        CityToWatch city = dbHelper.getCityToWatch(cityId);
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

        String url = "https://api.rainviewer.com/public/weather-maps.json";
        Log.d("DownloadRadarTimes", "Start");
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
                            long radarTimeGMT = Long.parseLong(radarFrames.getJSONObject(lastPastFramePosition).getString("time")) * 1000L;

                            // Download the image
                            Log.d("DownloadRadarTile", "Start");
                            ImageRequest imageRequest = new ImageRequest(radarUrl,
                                    response1 -> {
                                        //Save image and data for full widget update
                                        RadarWidget.radarBitmap = response1;
                                        WeatherWidgetAllInOne.radarBitmap = response1;
                                        RadarWidget.radarTimeGMT = radarTimeGMT;
                                        WeatherWidgetAllInOne.radarTimeGMT = radarTimeGMT;
                                        RadarWidget.radarZoom = zoom;
                                        WeatherWidgetAllInOne.radarZoom = zoom;
                                        int zoneseconds = dbHelper.getCurrentWeatherByCityId(cityId).getTimeZoneSeconds();

                                        //Partial update for radar view only
                                        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
                                        int[] widgetIDs = appWidgetManager.getAppWidgetIds(new ComponentName(getApplicationContext(), RadarWidget.class));
                                        if (widgetIDs.length > 0 ) {
                                            RemoteViews views = new RemoteViews(getApplicationContext().getPackageName(), R.layout.radar_widget);
                                            views.setImageViewBitmap(R.id.widget_radar_view, UpdateDataService.prepareRadarWidget(getApplicationContext(), city, zoom, radarTimeGMT + zoneseconds *1000L, response1));
                                            appWidgetManager.partiallyUpdateAppWidget(widgetIDs, views);
                                        }

                                        widgetIDs = appWidgetManager.getAppWidgetIds(new ComponentName(getApplicationContext(), WeatherWidgetAllInOne.class));
                                        if (widgetIDs.length > 0 ) {
                                            RemoteViews views = new RemoteViews(getApplicationContext().getPackageName(), R.layout.weather_widget_all_in_one);
                                            views.setImageViewBitmap(R.id.widget_radar_view, UpdateDataService.prepareAllInOneWidget(getApplicationContext(), city, zoom, radarTimeGMT + zoneseconds *1000L, response1));
                                            appWidgetManager.partiallyUpdateAppWidget(widgetIDs, views);
                                        }

                                    },
                                    0, 0, ImageView.ScaleType.CENTER_CROP, Bitmap.Config.RGB_565,
                                    error1 -> {
                                        // Handle the error
                                        Log.d("DownloadRadarTile", error1.toString());
                                    });
                            queue.add(imageRequest);

                        }

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                },
                error -> {
                    // Handle the error
                    Log.d("DownloadRadarTimes", error.toString());
                });

        queue.add(request);
    }

    @NonNull
    public static Bitmap prepareAllInOneWidget(Context context, CityToWatch city, int zoom, long radarTime, Bitmap response1) {
        Bitmap textBitmap = Bitmap.createBitmap(response1.getWidth(), response1.getHeight(), response1.getConfig());
        Canvas canvas = new Canvas(textBitmap);
        canvas.drawBitmap(response1, 0, 0, null); // draw the original image

        Paint paint = new Paint();
        paint.setColor(ContextCompat.getColor(context, R.color.lightgrey));
        paint.setTextSize(30);
        paint.setStrokeWidth(3.0f);

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
        canvas.drawText(widthDistanceMarker + " " + distanceUnit, 7 + widthDistanceMarkerPixel + 5, 238 + 8, paint); // draw the text

        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(StringFormatUtils.formatTimeWithoutZone(context, radarTime), 248, 238 + 8, paint);

        paint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(7, 238, 7 + widthDistanceMarkerPixel, 238, paint);

        int maxI = 100 / widthDistanceMarkerPixel;
        for (int i = 1; i <= maxI; i++) {
            int radius = i * widthDistanceMarkerPixel;
            canvas.drawCircle(128, 128, radius, paint);
        }

        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(128, 128, 2, paint);

        //Round off corners
        Paint clearPaint = new Paint();
        clearPaint.setStyle(Paint.Style.STROKE);
        clearPaint.setStrokeWidth(20.0f);
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawRoundRect(-10, -10,265, 265, 30, 30, clearPaint);
        return textBitmap;
    }

    @NonNull
    public static Bitmap prepareRadarWidget(Context context, CityToWatch city, int zoom, long radarTime, Bitmap response1) {
        Bitmap textBitmap = Bitmap.createBitmap(response1.getWidth(), response1.getHeight(), response1.getConfig());
        Canvas canvas = new Canvas(textBitmap);
        canvas.drawBitmap(response1, 0, 0, null); // draw the original image
        Paint paint = new Paint();
        paint.setColor(ContextCompat.getColor(context, R.color.lightgrey));
        paint.setTextSize(16);

        int widthTotalDistance = (int) (2 * 3.14 * 6378 * Math.abs(Math.cos(city.getLatitude() / 180 * 3.14)) / (Math.pow(2, zoom) * 256) * 256);

        String distanceUnit = context.getString(R.string.units_km);

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

        int maxI = 100 / widthDistanceMarkerPixel;
        for (int i = 1; i <= maxI; i++) {
            int radius = i * widthDistanceMarkerPixel;
            canvas.drawCircle(128, 128, radius, paint);
        }

        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(128, 128, 2, paint);

        //Round off corners
        Paint clearPaint = new Paint();
        clearPaint.setStyle(Paint.Style.STROKE);
        clearPaint.setStrokeWidth(20.0f);
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawRoundRect(-10, -10,265, 265, 30, 30, clearPaint);
        return textBitmap;
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

    private void handleUpdateSingle(Intent intent) {
        int cityId = intent.getIntExtra("cityId",-1);
        CityToWatch city = dbHelper.getCityToWatch(cityId);
        boolean skipUpdateInterval = intent.getBooleanExtra(SKIP_UPDATE_INTERVAL, false);

        long timestamp = 0;
        long systemTime = System.currentTimeMillis() / 1000;

        long updateInterval = (long) (0.25 * 60 * 60);

        List<HourlyForecast> hourlyForecasts = dbHelper.getForecastsByCityId(cityId);
        if (hourlyForecasts.size() > 0) {             // check timestamp of the current forecasts
            timestamp = hourlyForecasts.get(0).getTimestamp();
        }

        if (skipUpdateInterval) {
            // check timestamp of the current forecasts
                if ((timestamp+MIN_UPDATE_INTERVAL-systemTime)>0) skipUpdateInterval=false;  //even if skipUpdateInterval is true, never update if less than MIN_UPDATE_INTERVAL s
        }

        // Update if update forced or if a certain time has passed
        if (skipUpdateInterval || timestamp + updateInterval - systemTime <= 0) {
            IHttpRequestForWeatherAPI omHttpRequestForWeatherAPI = new OMHttpRequestForWeatherAPI(getApplicationContext());
            omHttpRequestForWeatherAPI.perform(city.getLatitude(),city.getLongitude(), cityId);
        }
    }

    private boolean isOnline(int timeOut) { //https://stackoverflow.com/questions/9570237/android-check-internet-connection
        InetAddress inetAddress = null;
        try {
            Future<InetAddress> future = Executors.newSingleThreadExecutor().submit(() -> {
                try {
                    URL url = new URL(BuildConfig.BASE_URL);
                    return InetAddress.getByName(url.getHost());
                } catch ( IOException e) {
                    return null;
                }
            });
            inetAddress = future.get(timeOut, TimeUnit.MILLISECONDS);
            future.cancel(true);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
        }
        return inetAddress!=null && !inetAddress.toString().isEmpty();
    }

}
