package org.woheller69.weather.weather_api.open_meteo;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;
import org.woheller69.weather.R;
import org.woheller69.weather.activities.NavigationActivity;
import org.woheller69.weather.database.CityToWatch;
import org.woheller69.weather.database.CurrentWeatherData;
import org.woheller69.weather.database.HourlyForecast;
import org.woheller69.weather.database.QuarterHourlyForecast;
import org.woheller69.weather.database.WeekForecast;
import org.woheller69.weather.database.SQLiteHelper;
import org.woheller69.weather.ui.updater.ViewUpdater;
import org.woheller69.weather.weather_api.IDataExtractor;
import org.woheller69.weather.weather_api.IProcessHttpRequest;
import org.woheller69.weather.widget.WeatherDigitalClockWidget;
import org.woheller69.weather.widget.WeatherWidget;
import org.woheller69.weather.widget.WeatherWidget5day;
import static org.woheller69.weather.database.SQLiteHelper.getWidgetCityID;

import androidx.preference.PreferenceManager;
import org.woheller69.weather.weather_api.IApiToDatabaseConversion.WeatherCategories;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class processes the HTTP requests that are made to the Open-Meteo API requesting the
 * current weather for all stored cities.
 */
public class ProcessOMweatherAPIRequest implements IProcessHttpRequest {

    /**
     * Constants
     */
    private final String DEBUG_TAG = "process_forecast";

    /**
     * Member variables
     */
    private Context context;
    private SQLiteHelper dbHelper;

    /**
     * Constructor.
     *
     * @param context The context of the HTTP request.
     */
    public ProcessOMweatherAPIRequest(Context context) {
        this.context = context;
        this.dbHelper = SQLiteHelper.getInstance(context);
    }

    /**
     * Converts the response to JSON and updates the database. Note that for this method no
     * UI-related operations are performed.
     *
     * @param response The response of the HTTP request.
     */
    @Override
    public void processSuccessScenario(String response, int cityId) {

        IDataExtractor extractor = new OMDataExtractor(context);
        try {
            JSONObject json = new JSONObject(response);

            //Extract daily weather
            List<WeekForecast> weekforecasts = new ArrayList<>();
            weekforecasts = extractor.extractWeekForecast(json.getString("daily"));

            if (weekforecasts!=null && !weekforecasts.isEmpty()){
                for (WeekForecast weekForecast: weekforecasts){
                    weekForecast.setCity_id(cityId);
                }
            } else {
                final String ERROR_MSG = context.getResources().getString(R.string.error_convert_to_json);
                if (NavigationActivity.isVisible)
                    Toast.makeText(context, ERROR_MSG, Toast.LENGTH_LONG).show();
                return;
            }

            //Extract current weather
                String rain60min=context.getResources().getString(R.string.error_no_rain60min_data);
                CurrentWeatherData weatherData = extractor.extractCurrentWeather(json.getString("current_weather"));

                if (weatherData == null) {
                    final String ERROR_MSG = context.getResources().getString(R.string.error_convert_to_json);
                    if (NavigationActivity.isVisible)
                        Toast.makeText(context, ERROR_MSG, Toast.LENGTH_LONG).show();
                    return;
                } else {
                    weatherData.setCity_id(cityId);
                    weatherData.setRain60min(rain60min);
                    weatherData.setTimeSunrise(weekforecasts.get(0).getTimeSunrise());
                    weatherData.setTimeSunset(weekforecasts.get(0).getTimeSunset());
                    weatherData.setTimeZoneSeconds(json.getInt("utc_offset_seconds"));
                    CurrentWeatherData current = dbHelper.getCurrentWeatherByCityId(cityId);
                    if (current != null && current.getCity_id() == cityId) {
                        dbHelper.updateCurrentWeather(weatherData);
                    } else {
                        dbHelper.addCurrentWeather(weatherData);
                    }
                }


                //Extract hourly weather
                List<HourlyForecast> hourlyforecasts = new ArrayList<>();
                hourlyforecasts = extractor.extractHourlyForecast(json.getString("hourly"));

                if (hourlyforecasts!=null && !hourlyforecasts.isEmpty()){
                    for (HourlyForecast hourlyForecast: hourlyforecasts){
                        hourlyForecast.setCity_id(cityId);
                    }
                } else {
                    final String ERROR_MSG = context.getResources().getString(R.string.error_convert_to_json);
                    if (NavigationActivity.isVisible)
                        Toast.makeText(context, ERROR_MSG, Toast.LENGTH_LONG).show();
                    return;
                }
            dbHelper.replaceForecasts(hourlyforecasts);

            SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(context);
            if (prefManager.getBoolean("pref_weekIDs", false)){
                weekforecasts = reanalyzeWeekIDs(weekforecasts, hourlyforecasts);
            }
            dbHelper.replaceWeekForecasts(weekforecasts);


            //Extract quarter-hourly weather
            if (json.has("minutely_15")){
                List<QuarterHourlyForecast> quarterHourlyForecasts = new ArrayList<>();
                quarterHourlyForecasts = extractor.extractQuarterHourlyForecast(json.getString("minutely_15"));

                if (quarterHourlyForecasts!=null && !quarterHourlyForecasts.isEmpty()){
                    for (QuarterHourlyForecast quarterHourlyForecast: quarterHourlyForecasts){
                        quarterHourlyForecast.setCity_id(cityId);
                    }
                } else {
                    final String ERROR_MSG = context.getResources().getString(R.string.error_convert_to_json);
                    if (NavigationActivity.isVisible)
                        Toast.makeText(context, ERROR_MSG, Toast.LENGTH_LONG).show();
                    return;
                }
                dbHelper.replaceQuarterHourlyForecasts(quarterHourlyForecasts);
            }

            possiblyUpdateWidgets(cityId, weatherData, weekforecasts, hourlyforecasts);

            ViewUpdater.updateCurrentWeatherData(weatherData);
            ViewUpdater.updateWeekForecasts(weekforecasts);
            ViewUpdater.updateForecasts(hourlyforecasts);

        } catch (JSONException e) {
            final String ERROR_MSG = context.getResources().getString(R.string.error_convert_to_json);
            if (NavigationActivity.isVisible)
                Toast.makeText(context, ERROR_MSG, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Reanalyze weekforecasts and improve weather codes which are not representative for the day
     * @param weekforecasts
     * @param hourlyforecasts
     * @return
     */
    private List<WeekForecast> reanalyzeWeekIDs(List<WeekForecast> weekforecasts, List<HourlyForecast> hourlyforecasts) {

        Map<Integer,Integer> mappingTable = new HashMap<>();
        mappingTable.put(WeatherCategories.OVERCAST_CLOUDS.getNumVal(),WeatherCategories.SCATTERED_CLOUDS.getNumVal());
        mappingTable.put(WeatherCategories.MIST.getNumVal(),WeatherCategories.SCATTERED_CLOUDS.getNumVal());
        mappingTable.put(WeatherCategories.DRIZZLE_RAIN.getNumVal(),WeatherCategories.LIGHT_SHOWER_RAIN.getNumVal());
        mappingTable.put(WeatherCategories.FREEZING_DRIZZLE_RAIN.getNumVal(),WeatherCategories.LIGHT_SHOWER_RAIN.getNumVal());
        mappingTable.put(WeatherCategories.LIGHT_RAIN.getNumVal(),WeatherCategories.LIGHT_SHOWER_RAIN.getNumVal());
        mappingTable.put(WeatherCategories.LIGHT_FREEZING_RAIN.getNumVal(),WeatherCategories.LIGHT_SHOWER_RAIN.getNumVal());
        mappingTable.put(WeatherCategories.MODERATE_RAIN.getNumVal(),WeatherCategories.SHOWER_RAIN.getNumVal());
        mappingTable.put(WeatherCategories.HEAVY_RAIN.getNumVal(),WeatherCategories.SHOWER_RAIN.getNumVal());
        mappingTable.put(WeatherCategories.FREEZING_RAIN.getNumVal(),WeatherCategories.SHOWER_RAIN.getNumVal());
        mappingTable.put(WeatherCategories.LIGHT_SNOW.getNumVal(),WeatherCategories.LIGHT_SHOWER_SNOW.getNumVal());
        mappingTable.put(WeatherCategories.MODERATE_SNOW.getNumVal(),WeatherCategories.SHOWER_SNOW.getNumVal());
        mappingTable.put(WeatherCategories.HEAVY_SNOW.getNumVal(),WeatherCategories.SHOWER_SNOW.getNumVal());

        Map<Integer,Integer> sunTable = new HashMap<>();
        sunTable.put(WeatherCategories.CLEAR_SKY.getNumVal(), 0);
        sunTable.put(WeatherCategories.FEW_CLOUDS.getNumVal(), 0);
        sunTable.put(WeatherCategories.SCATTERED_CLOUDS.getNumVal(), 0);

        for (WeekForecast weekForecast: weekforecasts){
            Integer ID = weekForecast.getWeatherID();
            if (mappingTable.containsKey(ID)){
                int totalCount = 0;
                int sunCount = 0;
                long sunrise = weekForecast.getTimeSunrise()*1000L;
                long sunset = weekForecast.getTimeSunset()*1000L;
                for (HourlyForecast hourlyForecast: hourlyforecasts){
                    if(hourlyForecast.getForecastTime() >= sunrise && hourlyForecast.getForecastTime() <= sunset){
                        totalCount++;
                        if(sunTable.containsKey(hourlyForecast.getWeatherID())) sunCount++;
                    }
                }
                if (totalCount>0 && (float)sunCount/totalCount>0.2f)  weekForecast.setWeatherID(mappingTable.get(ID));
            }
        }
        return weekforecasts;
    }

    /**
     * Shows an error that the data could not be retrieved.
     *
     * @param error The error that occurred while executing the HTTP request.
     */
    @Override
    public void processFailScenario(final VolleyError error) {
        Handler h = new Handler(this.context.getMainLooper());
        h.post(new Runnable() {
            @Override
            public void run() {
                if (NavigationActivity.isVisible) Toast.makeText(context, context.getResources().getString(R.string.error_fetch_forecast), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void possiblyUpdateWidgets(int cityID, CurrentWeatherData currentWeather, List<WeekForecast> weekforecasts, List<HourlyForecast> hourlyforecasts) {
        //search for widgets with same city ID
        int widgetCityID=getWidgetCityID(context);

        int[] widgetIDs = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, WeatherWidget.class));

        for (int widgetID : widgetIDs) {
            //check if city ID is same
            if (cityID == widgetCityID) {
                //perform update for the widget

                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weather_widget);
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

                CityToWatch city=dbHelper.getCityToWatch(cityID);

                WeatherWidget.updateView(context, appWidgetManager, views, widgetID, city, currentWeather,weekforecasts,hourlyforecasts);
                appWidgetManager.updateAppWidget(widgetID, views);
            }
        }


        //search for digital clock widgets with same city ID
        int widgetDigitalClockCityID=getWidgetCityID(context);

        int[] widgetDigitalClockIDs = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, WeatherDigitalClockWidget.class));

        for (int widgetID : widgetDigitalClockIDs) {
            //check if city ID is same
            if (cityID == widgetDigitalClockCityID) {
                //perform update for the widget

                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weather_digital_clock_widget);
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

                CityToWatch city=dbHelper.getCityToWatch(cityID);

                WeatherDigitalClockWidget.updateView(context, appWidgetManager, views, widgetID, city, currentWeather, weekforecasts);
                appWidgetManager.updateAppWidget(widgetID, views);
            }
        }

        //search for 5day widgets with same city ID
        int widget5dayCityID= getWidgetCityID(context);
        int[] widget5dayIDs = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, WeatherWidget5day.class));

        for (int widgetID : widget5dayIDs) {
            //check if city ID is same
            if (cityID == widget5dayCityID) {
                //perform update for the widget

                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weather_widget_5day);
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

                CityToWatch city=dbHelper.getCityToWatch(cityID);

                WeatherWidget5day.updateView(context, appWidgetManager, views, widgetID, city, weekforecasts);
                appWidgetManager.updateAppWidget(widgetID, views);
            }
        }

    }
}
