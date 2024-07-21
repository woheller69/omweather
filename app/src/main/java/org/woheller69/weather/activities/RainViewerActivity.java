package org.woheller69.weather.activities;

import static java.lang.Boolean.TRUE;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.config.Configuration;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.TilesOverlay;
import org.woheller69.weather.R;
import org.woheller69.weather.ui.Help.StringFormatUtils;
import org.woheller69.weather.ui.util.TilesOverlayEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class RainViewerActivity extends AppCompatActivity {

    private MapView mapView;
    private MapView mapView2;
    private MapView mapPreload;
    private TextView timeStamp;
    private int timezoneseconds;
    private int animationPosition = 0;
    private int lastPastFramePosition;
    private boolean nightmode;
    private JSONArray radarFrames;
    private JSONArray infraredFrames;
    private String host;
    private ScheduledExecutorService scheduledExecutorService;
    private boolean crossfadeRunning = false;
    private List<TilesOverlayEntry> radarTilesOverlayEntries;

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rain_viewer);
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (getSupportActionBar() == null) {
            setSupportActionBar(toolbar);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        float latitude = getIntent().getFloatExtra("latitude", -1);
        float longitude = getIntent().getFloatExtra("longitude", -1);
        timezoneseconds = getIntent().getIntExtra("timezoneseconds",0);

        nightmode = false;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if(sharedPreferences.getBoolean("pref_DarkMode", false) == TRUE) {
            int nightModeFlags = getApplicationContext().getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
            if (nightModeFlags==android.content.res.Configuration.UI_MODE_NIGHT_YES) nightmode = true;
        }

        timeStamp = findViewById(R.id.timestamp);
        mapView = findViewById(R.id.map);
        mapView2 = findViewById(R.id.map2);
        mapPreload = findViewById(R.id.map_preload);
        mapPreload.setTileSource(TileSourceFactory.MAPNIK);
        mapPreload.setTilesScaledToDpi(true);

        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

        mapView.setMultiTouchControls(true);
        mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.ALWAYS);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setTilesScaledToDpi(true);

        mapView2.setMultiTouchControls(true);
        mapView2.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.ALWAYS);
        mapView2.setTileSource(TileSourceFactory.MAPNIK);
        mapView2.setTilesScaledToDpi(true);

        if (nightmode) {
            //mapView.getOverlayManager().getTilesOverlay().setColorFilter(TilesOverlay.INVERT_COLORS);
            mapView.getOverlayManager().getTilesOverlay().setColorFilter(getNightMatrix());
            mapView2.getOverlayManager().getTilesOverlay().setColorFilter(getNightMatrix());
        } else {
            mapView.getOverlayManager().getTilesOverlay().setColorFilter(null);
            mapView2.getOverlayManager().getTilesOverlay().setColorFilter(null);
        }

        mapView.getController().setZoom(8d);
        mapView2.getController().setZoom(8d);
        mapPreload.getController().setZoom(8d);
        GeoPoint startPoint = new GeoPoint(latitude, longitude);
        mapView.getController().setCenter(startPoint);
        mapView2.getController().setCenter(startPoint);
        mapPreload.getController().setCenter(startPoint);

        Marker positionMarker = new Marker(mapView);
        positionMarker.setPosition(startPoint);
        positionMarker.setInfoWindow(null);
        positionMarker.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_location_48dp));
        positionMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.getOverlays().add(positionMarker);
        Marker positionMarker2 = new Marker(mapView2);
        positionMarker2.setPosition(startPoint);
        positionMarker2.setInfoWindow(null);
        positionMarker2.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_location_48dp));
        positionMarker2.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView2.getOverlays().add(positionMarker2);

        ImageButton btnNext = findViewById(R.id.rainviewer_next);
        ImageButton btnPrev = findViewById(R.id.rainviewer_prev);
        ImageButton btnStartStop = findViewById(R.id.rainviewer_startstop);

        btnNext.setOnClickListener(v -> {
            if (scheduledExecutorService!=null && !scheduledExecutorService.isShutdown()) scheduledExecutorService.shutdownNow();
            showFrame(animationPosition + 1);
        });

        btnPrev.setOnClickListener(v -> {
            if (scheduledExecutorService!=null && !scheduledExecutorService.isShutdown()) scheduledExecutorService.shutdownNow();
            showFrame(animationPosition - 1);
        });

        btnStartStop.setOnClickListener(v -> {
            playStop();
        });

    radarTilesOverlayEntries = new ArrayList<>();
    }

    @Override
    protected void onResume() {
        super.onResume();

        RequestQueue queue = Volley.newRequestQueue(this); // context is your app's context

        String url = "https://api.rainviewer.com/public/weather-maps.json";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    // Parse the JSON response
                    try {
                        if (response != null && response.has("host")) host = response.getString("host");
                        //Store the infrared time frames
                        if (response != null && response.has("satellite") && response.getJSONObject("satellite").has("infrared")) {
                            infraredFrames = response.getJSONObject("satellite").getJSONArray("infrared");
                            List<String> infraredFrameTimes = new ArrayList<>();

                            for (int i = 0; i < infraredFrames.length(); i++) {
                                JSONObject frame = infraredFrames.getJSONObject(i);
                                infraredFrameTimes.add(frame.getString("time"));
                            }
                        }

                        //Store the radar time frames
                        if (response != null && response.has("radar") && response.getJSONObject("radar").has("past")){
                            radarFrames = response.getJSONObject("radar").getJSONArray("past");
                            lastPastFramePosition = radarFrames.length() - 1;
                            if (response.getJSONObject("radar").has("nowcast")) {
                                JSONArray nowcastFrames = response.getJSONObject("radar").getJSONArray("nowcast");
                                for (int i = 0; i < nowcastFrames.length(); i++) {
                                    radarFrames.put(nowcastFrames.get(i));
                                }
                            }
                            showFrame(lastPastFramePosition);
                        }

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                },
                error -> {
                    // Handle the error
                });

        queue.add(request);

    }
    public void playStop(){

        if (scheduledExecutorService == null || scheduledExecutorService.isShutdown()) {

        Handler handler = new Handler(Looper.getMainLooper());
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

        Runnable showFrameRunnable = new Runnable() {
            @Override
            public void run() {
                handler.post(() -> {
                    showFrame(animationPosition + 1);
                });
                long nextDelay;
                if (animationPosition == (radarFrames.length()-2)) {
                    nextDelay = 3000;
                } else if (animationPosition == lastPastFramePosition-1) {
                    nextDelay = 1500;
                } else {
                    nextDelay = 800;
                }
                scheduledExecutorService.schedule(this, nextDelay, TimeUnit.MILLISECONDS);
            }
        };
        scheduledExecutorService.schedule(showFrameRunnable, 0, TimeUnit.MILLISECONDS);
        } else {
            scheduledExecutorService.shutdownNow();
        }
    }

    public void showFrame(int position){
        int preloadingDirection = position - animationPosition > 0 ? 1 : -1;

        //Todo: Add infrared frames

        if (radarFrames == null || crossfadeRunning){
            return;
        }
        try {
            position = (position + radarFrames.length()) % radarFrames.length();
            final TilesOverlay newRadarOverlay = getNewRadarOverlay(position);
            IGeoPoint center;
            double zoom;
            if (mapView.getVisibility() == View.VISIBLE){
                zoom = mapView.getZoomLevelDouble(); //take zoom from visible map
                center = mapView.getMapCenter(); //take center from visible map
                replaceLayer(mapView2, newRadarOverlay, center, zoom);
            } else {
                zoom = mapView2.getZoomLevelDouble(); //take zoom from visible map
                center = mapView2.getMapCenter(); //take center from visible map
                replaceLayer(mapView, newRadarOverlay, center, zoom);
            }


            if (mapView.getVisibility() == View.VISIBLE) {
                crossFade(mapView2, mapView);
            } else {
                crossFade(mapView, mapView2);
            }

            long time = (Long.parseLong(radarFrames.getJSONObject(position).getString("time")) + timezoneseconds) * 1000L;
            timeStamp.setText(StringFormatUtils.formatDate(time) + ", " + StringFormatUtils.formatTimeWithoutZone(this, time));
            if (position > lastPastFramePosition)
                timeStamp.setTextColor(ContextCompat.getColor(this,R.color.red));
            else
                timeStamp.setTextColor(ContextCompat.getColor(this,R.color.colorPrimaryDark));

            animationPosition = position;

            //now preload next frame
            int preloadPosition = (position + preloadingDirection + radarFrames.length()) % radarFrames.length();
            final TilesOverlay newRadarPreloadOverlay = getNewRadarOverlay(preloadPosition);
            replaceLayer(mapPreload, newRadarPreloadOverlay, center, zoom);

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void crossFade(MapView map, MapView map2) {
        int animationDuration = 200; //milliseconds
        map.setAlpha(0f);
        map.setVisibility(View.VISIBLE);
        map.animate()
                .alpha(1f)
                .setDuration(animationDuration)
                .setInterpolator(new DecelerateInterpolator())
                .setListener(null);
        crossfadeRunning = true;
        map2.animate()
                .alpha(0f)
                .setDuration(animationDuration)
                .setInterpolator(new AccelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        map2.setVisibility(View.INVISIBLE);
                        crossfadeRunning = false;
                    }
                });
    }

    private void replaceLayer(MapView map, TilesOverlay newRadarOverlay, IGeoPoint center, double zoom) {
        map.getOverlays().clear();
        map.getOverlays().add(newRadarOverlay);
        map.getController().setZoom(zoom);
        map.getController().setCenter(center);
        map.getController().animateTo(center);
    }

    @NonNull
    private TilesOverlay getNewRadarOverlay(int position) throws JSONException {
        long time = Long.parseLong(radarFrames.getJSONObject(position).getString("time"));

        for (TilesOverlayEntry entry : radarTilesOverlayEntries) {
            if (entry.getTime() == time){
                return entry.getTilesOverlay();
            }
        }

        final MapTileProviderBasic RainViewerTileProvider = new MapTileProviderBasic(this);
        final ITileSource RainViewerTileSource = new XYTileSource(Long.toString(time), 1, 20, 256, "/2/1_1.png", new String[]{host+radarFrames.getJSONObject(position).getString("path")+"/256/"});
        RainViewerTileProvider.setTileSource(RainViewerTileSource);
        final TilesOverlay newRadarOverlay = new TilesOverlay(RainViewerTileProvider, this);
        newRadarOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);
        int transparency = 128; // 128 is 50% transparent
        PorterDuffColorFilter filter = new PorterDuffColorFilter(Color.argb(transparency, 255, 255, 255), PorterDuff.Mode.MULTIPLY);
        newRadarOverlay.setColorFilter(filter);
        TilesOverlayEntry newRadarEntry = new TilesOverlayEntry(newRadarOverlay,time);
        radarTilesOverlayEntries.add(newRadarEntry);
        return newRadarOverlay;
    }

    @NonNull
    private static ColorMatrixColorFilter getNightMatrix() {
        ColorMatrix colorMatrix = new ColorMatrix();

        // Step 1: Invert colors
        ColorMatrix invertMatrix = new ColorMatrix(new float[]{
                -1, 0, 0, 0, 255,
                0, -1, 0, 0, 255,
                0, 0, -1, 0, 255,
                0, 0, 0, 1, 0
        });

        // Step 2: Adjust saturation (200%)
        ColorMatrix saturationMatrix = new ColorMatrix();
        saturationMatrix.setSaturation(2.0f);

        // Step 3: Adjust hue (185 degrees)
        ColorMatrix hueMatrix = new ColorMatrix();
        setHue(hueMatrix, 185);

        // Concatenate all the matrices
        colorMatrix.postConcat(invertMatrix);
        colorMatrix.postConcat(saturationMatrix);
        colorMatrix.postConcat(hueMatrix);
        return new ColorMatrixColorFilter(colorMatrix);
    }

    private static void setHue(ColorMatrix cm, float value) {
        value = cleanValue(value, 180f) / 180f * (float) Math.PI;
        if (value == 0) {
            return;
        }
        float cosVal = (float) Math.cos(value);
        float sinVal = (float) Math.sin(value);
        float lumR = 0.213f;
        float lumG = 0.715f;
        float lumB = 0.072f;
        float[] mat = new float[]{
                lumR + cosVal * (1 - lumR) + sinVal * (-lumR), lumG + cosVal * (-lumG) + sinVal * (-lumG), lumB + cosVal * (-lumB) + sinVal * (1 - lumB), 0, 0,
                lumR + cosVal * (-lumR) + sinVal * 0.143f, lumG + cosVal * (1 - lumG) + sinVal * 0.140f, lumB + cosVal * (-lumB) + sinVal * (-0.283f), 0, 0,
                lumR + cosVal * (-lumR) + sinVal * (-(1 - lumR)), lumG + cosVal * (-lumG) + sinVal * lumG, lumB + cosVal * (1 - lumB) + sinVal * lumB, 0, 0,
                0, 0, 0, 1, 0
        };
        cm.postConcat(new ColorMatrix(mat));
    }

    private static float cleanValue(float p_val, float p_limit) {
        return Math.min(p_limit, Math.max(-p_limit, p_val));
    }
}


