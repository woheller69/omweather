package org.woheller69.weather.activities;

import static java.lang.Boolean.TRUE;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsetsController;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.TextView;
import android.widget.Toast;

import org.woheller69.weather.R;
import org.woheller69.weather.database.CityToWatch;
import org.woheller69.weather.database.CurrentWeatherData;
import org.woheller69.weather.database.HourlyForecast;
import org.woheller69.weather.database.SQLiteHelper;
import org.woheller69.weather.database.WeekForecast;
import org.woheller69.weather.ui.updater.IUpdateableCityUI;
import org.woheller69.weather.ui.updater.ViewUpdater;
import org.woheller69.weather.ui.util.ThemeUtils;
import org.woheller69.weather.ui.viewPager.WeatherPagerAdapter;
import static org.woheller69.weather.database.SQLiteHelper.getWidgetCityID;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;

public class ForecastCityActivity extends NavigationActivity implements IUpdateableCityUI {
    private WeatherPagerAdapter pagerAdapter;
    private static LocationListener locationListenerGPS;
    private LocationManager locationManager;
    private static MenuItem updateLocationButton;
    private static MenuItem refreshActionButton;
    private MenuItem rainviewerButton;

    private int cityId = -1;
    private ViewPager2 viewPager2;
    private TabLayout tabLayout;
    private TextView noCityText;
    private static Boolean isRefreshing = false;
    Context context;

    @Override
    protected void onPause() {
        super.onPause();

        ViewUpdater.removeSubscriber(this);
        ViewUpdater.removeSubscriber(pagerAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        SQLiteHelper db = SQLiteHelper.getInstance(this);
        if (db.getAllCitiesToWatch().isEmpty()) {
            // no cities selected.. don't show the viewPager - rather show a text that tells the user that no city was selected
            viewPager2.setVisibility(View.GONE);
            noCityText.setVisibility(View.VISIBLE);

        } else {
            noCityText.setVisibility(View.GONE);
            viewPager2.setVisibility(View.VISIBLE);
            pagerAdapter.loadCities();
            viewPager2.setAdapter(pagerAdapter);
            TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager2,false,false, (tab, position) -> tab.setText(pagerAdapter.getPageTitle(position)));
            tabLayoutMediator.attach();
        }

        ViewUpdater.addSubscriber(this);
        ViewUpdater.addSubscriber(pagerAdapter);

        if (pagerAdapter.getItemCount()>0) {  //only if at least one city is watched
             //if pagerAdapter has item with current cityId go there, otherwise use cityId from current item
            if (pagerAdapter.getPosForCityID(cityId)==-1) cityId=pagerAdapter.getCityIDForPos(viewPager2.getCurrentItem());
            if (viewPager2.getCurrentItem()!=pagerAdapter.getPosForCityID(cityId)) viewPager2.setCurrentItem(pagerAdapter.getPosForCityID(cityId),false);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=this;
        setContentView(R.layout.activity_forecast_city);
        ThemeUtils.setStatusBarAppearance(this);
        initResources();

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                //Update current tab if outside update interval, show animation
                SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SQLiteHelper database = SQLiteHelper.getInstance(getApplicationContext().getApplicationContext());
                CurrentWeatherData currentWeather = database.getCurrentWeatherByCityId(pagerAdapter.getCityIDForPos(position));

                long timestamp = currentWeather.getTimestamp();
                long systemTime = System.currentTimeMillis() / 1000;
                //long updateInterval = (long) (Float.parseFloat(prefManager.getString("pref_updateInterval", "2")) * 60 * 60);
                long updateInterval = (long) (0.25 * 60 * 60);
                if (timestamp + updateInterval - systemTime <= 0) {
                    if (pagerAdapter.getCityIDForPos(position)!=getWidgetCityID(context)||locationListenerGPS==null) { //do not update first TAB while location is updating
                        WeatherPagerAdapter.refreshSingleData(getApplicationContext(),true, pagerAdapter.getCityIDForPos(position));
                        ForecastCityActivity.startRefreshAnimation();
                    }
                }
                //post method needed to avoid Illegal State Exception: Cannot call this method in a scroll callback.
                viewPager2.post(() -> {
                    pagerAdapter.notifyItemChanged(position);  //fix crash with StaggeredGridLayoutManager when moving back and forth between items
                });
                cityId=pagerAdapter.getCityIDForPos(viewPager2.getCurrentItem());  //save current cityId for next resume
            }

        });

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (intent.hasExtra("cityId")) {
            cityId = intent.getIntExtra("cityId",-1);
            if (pagerAdapter.getItemCount()>0) viewPager2.setCurrentItem(pagerAdapter.getPosForCityID(cityId),false);
        }
    }

    private void initResources() {
        viewPager2 = findViewById(R.id.viewPager2);
        viewPager2.setUserInputEnabled(false);
        //reduceViewpager2DragSensitivity(viewPager2,2);
        tabLayout = findViewById(R.id.tab_layout);
        pagerAdapter = new WeatherPagerAdapter(this, getSupportFragmentManager(),getLifecycle());
        noCityText = findViewById(R.id.noCitySelectedText);
    }

    @Override
    protected int getNavigationDrawerID() {
        return R.id.nav_weather;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_forecast_city, menu);

        final Menu m = menu;
        SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        updateLocationButton = menu.findItem(R.id.menu_update_location);
        SQLiteHelper db = SQLiteHelper.getInstance(this);
        if(prefManager.getBoolean("pref_GPS", false)==TRUE && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
            updateLocationButton.setVisible(true);
            updateLocationButton.setActionView(R.layout.menu_update_location_view);
            updateLocationButton.getActionView().clearAnimation();
            if (locationListenerGPS!=null) {  //GPS still trying to get new location -> stop and restart to get around problem with tablayout not updating
                removeLocationListener();
                if (!db.getAllCitiesToWatch().isEmpty()) {  //if city has not been removed continue location update
                    locationListenerGPS=getNewLocationListener();
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListenerGPS);
                    if (updateLocationButton != null && updateLocationButton.getActionView() != null) {
                        startUpdateLocatationAnimation();
                    }
                }
            }
            updateLocationButton.getActionView().setOnClickListener(v -> m.performIdentifierAction(updateLocationButton.getItemId(), 0));
        }else{
            removeLocationListener();
            if (updateLocationButton != null && updateLocationButton.getActionView() != null) {
                updateLocationButton.getActionView().clearAnimation();
            }
            SharedPreferences.Editor editor = prefManager.edit();
            editor.putBoolean("pref_GPS",false);  //if GPS permission has been revoked also switch off in settings
            editor.apply();
        }

        refreshActionButton = menu.findItem(R.id.menu_refresh);
        refreshActionButton.setActionView(R.layout.menu_refresh_action_view);
        refreshActionButton.getActionView().setOnClickListener(v -> m.performIdentifierAction(refreshActionButton.getItemId(), 0));
        if (isRefreshing) startRefreshAnimation();

        rainviewerButton = menu.findItem(R.id.menu_rainviewer);
        rainviewerButton.setActionView(R.layout.menu_rainviewer_view);
        rainviewerButton.getActionView().setOnClickListener(v -> m.performIdentifierAction(rainviewerButton.getItemId(), 0));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        SQLiteHelper db = SQLiteHelper.getInstance(this);
        if (id==R.id.menu_rainviewer) {
            SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            if (!prefManager.getBoolean("pref_rainviewer",false)) {

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setMessage(R.string.nonFreeNet);
                alertDialogBuilder.setPositiveButton(getString(R.string.dialog_OK_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(context, SettingsActivity.class));
                    }
                });
                alertDialogBuilder.setNegativeButton(getString(R.string.dialog_NO_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();


            } else {
                if (!db.getAllCitiesToWatch().isEmpty()) {  //only if at least one city is watched, otherwise crash
                    Intent intent = new Intent(this, RainViewerActivity.class);
                    intent.putExtra("latitude", pagerAdapter.getLatForPos((viewPager2.getCurrentItem())));
                    intent.putExtra("longitude", pagerAdapter.getLonForPos((viewPager2.getCurrentItem())));
                    CurrentWeatherData currentWeather = db.getCurrentWeatherByCityId(pagerAdapter.getCityIDForPos(viewPager2.getCurrentItem()));
                    intent.putExtra("timezoneseconds", currentWeather.getTimeZoneSeconds());
                    startActivity(intent);
                }
            }
        }else if (id==R.id.menu_refresh){
            if (!db.getAllCitiesToWatch().isEmpty()) {  //only if at least one city is watched, otherwise crash
                WeatherPagerAdapter.refreshSingleData(getApplicationContext(),true, pagerAdapter.getCityIDForPos(viewPager2.getCurrentItem()));
                ForecastCityActivity.startRefreshAnimation();
            }
        } else if (id==R.id.menu_update_location) {
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                Toast.makeText(this,R.string.error_no_gps,Toast.LENGTH_LONG).show();
            } else {
                if (db.getAllCitiesToWatch().isEmpty()) {
                    CityToWatch newCity = new CityToWatch(db.getMaxRank() + 1, -1, -1, 0, 0, "--°/--°");
                    cityId = (int) db.addCityToWatch(newCity);
                    initResources();
                    noCityText.setVisibility(View.GONE);
                    viewPager2.setVisibility(View.VISIBLE);
                    viewPager2.setAdapter(pagerAdapter);
                    TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager2, false, false, (tab, position) -> tab.setText(pagerAdapter.getPageTitle(position)));
                    tabLayoutMediator.attach();
                }
                SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                if (prefManager.getBoolean("pref_GPS", false) == TRUE && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    if (locationListenerGPS == null) {
                        Log.d("GPS", "Listener null");
                        locationListenerGPS = getNewLocationListener();
                        ForecastCityActivity.startUpdateLocatationAnimation();
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListenerGPS);
                    }
                }
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

    }

    @Override
    public void processNewCurrentWeatherData(CurrentWeatherData data) {
        stopRefreshAnimation();
    }

    @Override
    public void processNewWeekForecasts(List<WeekForecast> forecasts) {
        stopRefreshAnimation();
    }

    @Override
    public void processNewForecasts(List<HourlyForecast> hourlyForecasts) {
        stopRefreshAnimation();
    }

    public static void stopRefreshAnimation(){
        if (refreshActionButton != null && refreshActionButton.getActionView() != null) {
            refreshActionButton.getActionView().clearAnimation();
        }
        isRefreshing = false;
    }

    public static void startRefreshAnimation(){
        isRefreshing = true;
        if(refreshActionButton !=null && refreshActionButton.getActionView() != null) {
            RotateAnimation rotate = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotate.setDuration(500);
            rotate.setRepeatCount(5);
            rotate.setInterpolator(new LinearInterpolator());
            rotate.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    refreshActionButton.getActionView().setActivated(false);
                    refreshActionButton.getActionView().setEnabled(false);
                    refreshActionButton.getActionView().setClickable(false);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    refreshActionButton.getActionView().setActivated(true);
                    refreshActionButton.getActionView().setEnabled(true);
                    refreshActionButton.getActionView().setClickable(true);
                    isRefreshing = false;
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            refreshActionButton.getActionView().startAnimation(rotate);
        }
    }

    public static void startUpdateLocatationAnimation(){
        {
            if(updateLocationButton !=null && updateLocationButton.getActionView() != null) {
                Animation blink = new AlphaAnimation(1, 0); // Change alpha from fully visible to invisible
                blink.setDuration(1000);
                blink.setRepeatCount(Animation.INFINITE);
                blink.setInterpolator(new LinearInterpolator());
                blink.setRepeatMode(Animation.REVERSE);
                blink.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        updateLocationButton.getActionView().setActivated(false);
                        updateLocationButton.getActionView().setEnabled(false);
                        updateLocationButton.getActionView().setClickable(false);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        updateLocationButton.getActionView().setActivated(true);
                        updateLocationButton.getActionView().setEnabled(true);
                        updateLocationButton.getActionView().setClickable(true);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                updateLocationButton.getActionView().startAnimation(blink);
            }
        }
    }

    //https://devdreamz.com/question/348298-how-to-modify-sensitivity-of-viewpager
    private void reduceViewpager2DragSensitivity(ViewPager2 viewPager, int sensitivity) {
        try {
            Field ff = ViewPager2.class.getDeclaredField("mRecyclerView") ;
            ff.setAccessible(true);
            RecyclerView recyclerView =  (RecyclerView) ff.get(viewPager);
            Field touchSlopField = RecyclerView.class.getDeclaredField("mTouchSlop") ;
            touchSlopField.setAccessible(true);
            int touchSlop = (int) touchSlopField.get(recyclerView);
            touchSlopField.set(recyclerView,touchSlop*sensitivity);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private LocationListener getNewLocationListener() {
        return new LocationListener() {
            @Override
            public void onLocationChanged(android.location.Location location) {
                Log.d("GPS", "Location changed");
                SQLiteHelper db = SQLiteHelper.getInstance(context);
                CityToWatch city = db.getCityToWatch(getWidgetCityID(context));
                city.setLatitude((float) location.getLatitude());
                city.setLongitude((float) location.getLongitude());
                city.setCityName(String.format(Locale.getDefault(), "%.2f° / %.2f°", location.getLatitude(), location.getLongitude()));
                db.updateCityToWatch(city);
                db.deleteWeekForecastsByCityId(getWidgetCityID(context));
                db.deleteCurrentWeatherByCityId(getWidgetCityID(context));
                db.deleteForecastsByCityId(getWidgetCityID(context));
                db.deleteQuarterHourlyForecastsByCityId(getWidgetCityID(context));
                pagerAdapter.loadCities();
                viewPager2.setAdapter(pagerAdapter);
                tabLayout.getTabAt(0).setText(city.getCityName());
                removeLocationListener();
                if (updateLocationButton != null && updateLocationButton.getActionView() != null) {
                    updateLocationButton.getActionView().clearAnimation();
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
    }

    private void removeLocationListener() {
        if (locationListenerGPS!=null) {
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (locationListenerGPS!=null) locationManager.removeUpdates(locationListenerGPS);
        }
        locationListenerGPS=null;
    }
}

