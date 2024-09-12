package org.woheller69.weather.widget;


import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.widget.RemoteViews;

import org.woheller69.weather.R;
import org.woheller69.weather.activities.ForecastCityActivity;
import org.woheller69.weather.database.CityToWatch;
import org.woheller69.weather.database.CurrentWeatherData;
import org.woheller69.weather.database.SQLiteHelper;
import org.woheller69.weather.database.WeekForecast;
import org.woheller69.weather.services.UpdateDataService;
import org.woheller69.weather.ui.Help.StringFormatUtils;
import org.woheller69.weather.ui.UiResourceProvider;
import org.woheller69.weather.weather_api.IApiToDatabaseConversion;
import static org.woheller69.weather.database.SQLiteHelper.getWidgetCityID;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import static androidx.core.app.JobIntentService.enqueueWork;
import static org.woheller69.weather.services.UpdateDataService.SKIP_UPDATE_INTERVAL;

import androidx.preference.PreferenceManager;

public class WeatherWidget5day extends AppWidgetProvider {

    public void updateAppWidget(Context context, final int appWidgetId) {

        SQLiteHelper db = SQLiteHelper.getInstance(context);
        if (!db.getAllCitiesToWatch().isEmpty()) {

            int cityID = getWidgetCityID(context);

            Intent intent = new Intent(context, UpdateDataService.class);
            intent.setAction(UpdateDataService.UPDATE_SINGLE_ACTION);
            intent.putExtra("cityId", cityID);
            intent.putExtra(SKIP_UPDATE_INTERVAL, true);
            enqueueWork(context, UpdateDataService.class, 0, intent);
        }
    }


    public static void updateView(Context context, AppWidgetManager appWidgetManager, RemoteViews views, int appWidgetId, CityToWatch city, List<WeekForecast> weekforecasts) {
        SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        views.setFloat(R.id.widget_background,"setAlpha", (100.0f - prefManager.getInt("pref_WidgetTransparency", 0)) /100.0f);
        int cityId=getWidgetCityID(context);
        SQLiteHelper database = SQLiteHelper.getInstance(context.getApplicationContext());
        int zonemilliseconds = database.getCurrentWeatherByCityId(cityId).getTimeZoneSeconds()*1000;
        CurrentWeatherData currentWeather = database.getCurrentWeatherByCityId(cityId);

        Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("GMT"));

        int []forecastData = new int[5];
        boolean[] isDay = new boolean[5];
        String []weekday = new String[5];
        for (int i=0;i<5;i++){
            c.setTimeInMillis(weekforecasts.get(i).getForecastTime()+zonemilliseconds);

            if (currentWeather.getTimeSunrise()==0 || currentWeather.getTimeSunset()==0) {
                if ((database.getCityToWatch(cityId).getLatitude()) > 0) {  //northern hemisphere
                    isDay[i] = c.get(Calendar.DAY_OF_YEAR) >= 80 && c.get(Calendar.DAY_OF_YEAR) <= 265;  //from March 21 to September 22 (incl)
                } else { //southern hemisphere
                    isDay[i] = c.get(Calendar.DAY_OF_YEAR) < 80 || c.get(Calendar.DAY_OF_YEAR) > 265;
                }
            } else {
                isDay[i] = true;
            }

            int day = c.get(Calendar.DAY_OF_WEEK);
            weekday[i]=context.getResources().getString(StringFormatUtils.getDayShort(day));

            forecastData[i]=weekforecasts.get(i).getWeatherID();

        }

        views.setImageViewResource(R.id.widget_5day_image1, UiResourceProvider.getIconResourceForWeatherCategory(forecastData[0], isDay[0]));
        views.setImageViewResource(R.id.widget_5day_image2, UiResourceProvider.getIconResourceForWeatherCategory(forecastData[1], isDay[1]));
        views.setImageViewResource(R.id.widget_5day_image3, UiResourceProvider.getIconResourceForWeatherCategory(forecastData[2], isDay[2]));
        views.setImageViewResource(R.id.widget_5day_image4, UiResourceProvider.getIconResourceForWeatherCategory(forecastData[3], isDay[3]));
        views.setImageViewResource(R.id.widget_5day_image5, UiResourceProvider.getIconResourceForWeatherCategory(forecastData[4], isDay[4]));

        views.setTextViewText(R.id.widget_5day_day1,weekday[0]);
        views.setTextViewText(R.id.widget_5day_day2,weekday[1]);
        views.setTextViewText(R.id.widget_5day_day3,weekday[2]);
        views.setTextViewText(R.id.widget_5day_day4,weekday[3]);
        views.setTextViewText(R.id.widget_5day_day5,weekday[4]);

        views.setTextViewText(R.id.widget_5day_temp_max1, StringFormatUtils.formatTemperature(context,weekforecasts.get(0).getMaxTemperature()));
        views.setTextViewText(R.id.widget_5day_temp_max2, StringFormatUtils.formatTemperature(context,weekforecasts.get(1).getMaxTemperature()));
        views.setTextViewText(R.id.widget_5day_temp_max3, StringFormatUtils.formatTemperature(context,weekforecasts.get(2).getMaxTemperature()));
        views.setTextViewText(R.id.widget_5day_temp_max4, StringFormatUtils.formatTemperature(context,weekforecasts.get(3).getMaxTemperature()));
        views.setTextViewText(R.id.widget_5day_temp_max5, StringFormatUtils.formatTemperature(context,weekforecasts.get(4).getMaxTemperature()));

        views.setTextViewText(R.id.widget_5day_temp_min1, StringFormatUtils.formatTemperature(context,weekforecasts.get(0).getMinTemperature()));
        views.setTextViewText(R.id.widget_5day_temp_min2, StringFormatUtils.formatTemperature(context,weekforecasts.get(1).getMinTemperature()));
        views.setTextViewText(R.id.widget_5day_temp_min3, StringFormatUtils.formatTemperature(context,weekforecasts.get(2).getMinTemperature()));
        views.setTextViewText(R.id.widget_5day_temp_min4, StringFormatUtils.formatTemperature(context,weekforecasts.get(3).getMinTemperature()));
        views.setTextViewText(R.id.widget_5day_temp_min5, StringFormatUtils.formatTemperature(context,weekforecasts.get(4).getMinTemperature()));

        views.setImageViewResource(R.id.widget_5day_wind1,StringFormatUtils.colorWindSpeedWidget(weekforecasts.get(0).getWind_speed()));
        views.setImageViewResource(R.id.widget_5day_wind2,StringFormatUtils.colorWindSpeedWidget(weekforecasts.get(1).getWind_speed()));
        views.setImageViewResource(R.id.widget_5day_wind3,StringFormatUtils.colorWindSpeedWidget(weekforecasts.get(2).getWind_speed()));
        views.setImageViewResource(R.id.widget_5day_wind4,StringFormatUtils.colorWindSpeedWidget(weekforecasts.get(3).getWind_speed()));
        views.setImageViewResource(R.id.widget_5day_wind5,StringFormatUtils.colorWindSpeedWidget(weekforecasts.get(4).getWind_speed()));

        Intent intent2 = new Intent(context, ForecastCityActivity.class);
        intent2.putExtra("cityId", getWidgetCityID(context));
        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent2, PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent2, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        views.setOnClickPendingIntent(R.id.widget5day_layout, pendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
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

        List<WeekForecast> weekforecasts=dbHelper.getWeekForecastsByCityId(widgetCityID);

        int[] widgetIDs = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, WeatherWidget5day.class));

        for (int widgetID : widgetIDs) {

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weather_widget_5day);
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

            CityToWatch city=dbHelper.getCityToWatch(widgetCityID);

            WeatherWidget5day.updateView(context, appWidgetManager, views, widgetID, city, weekforecasts);
            appWidgetManager.updateAppWidget(widgetID, views);

        }
     }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

}

