package org.woheller69.weather.services;

import static org.woheller69.weather.database.SQLiteHelper.getWidgetCityID;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import org.woheller69.weather.database.CurrentWeatherData;
import org.woheller69.weather.database.SQLiteHelper;
import org.woheller69.weather.database.WeekForecast;

import java.util.ArrayList;
import java.util.List;

import nodomain.freeyourgadget.gadgetbridge.model.Converter;
import nodomain.freeyourgadget.gadgetbridge.model.WeatherSpec;

public class Broadcaster {
    public static void possiblyUpdateOtherApp(Context context,
                                              CurrentWeatherData weatherData,
                                              List<WeekForecast> weekForecasts) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        boolean run = sp.getBoolean("sendToApp", false);

        if(run && weatherData.getCity_id()==getWidgetCityID(context)) {
            Intent intent = new Intent();
            intent.setAction(sp.getString("sendAction", "de.kaffeemitkoffein.broadcast.WEATHERDATA"));
            intent.setPackage(sp.getString("sendPackage","nodomain.freeyourgadget.gadgetbridge"));
            intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            intent.putExtra("WeatherSpec", generateWeatherSpec(context, weatherData, weekForecasts));
            context.sendBroadcast(intent);
        }
    }

    private static WeatherSpec generateWeatherSpec(Context context, CurrentWeatherData weatherData, List<WeekForecast> weekForecasts) {
        ArrayList<WeatherSpec.Forecast> forecasts = generateForecasts(getWidgetCityID(context), weekForecasts);
        SQLiteHelper dbHelper = SQLiteHelper.getInstance(context);
        WeatherSpec spec = new WeatherSpec();

        spec.timestamp = (int) weatherData.getTimestamp();
        spec.location = dbHelper.getCityToWatch(weatherData.getCity_id()).getCityName();
        spec.currentTemp = Converter.celsiusToKelvin(weatherData.getTemperatureCurrent());
        spec.currentConditionCode = Converter.convertOMtoOW(weatherData.getWeatherID());
        spec.currentCondition = Converter.convertOMtoOWString(weatherData.getWeatherID());
        spec.currentHumidity = (int) weatherData.getHumidity();
        spec.todayMaxTemp = Converter.celsiusToKelvin(weekForecasts.get(0).getMaxTemperature());
        spec.todayMinTemp = Converter.celsiusToKelvin(weekForecasts.get(0).getMinTemperature());
        spec.windSpeed = weatherData.getWindSpeed();
        spec.windDirection = (int) weatherData.getWindDirection();
        spec.forecasts = forecasts;

        return spec;
    }

    private static ArrayList<WeatherSpec.Forecast> generateForecasts(int cityID, List<WeekForecast> weekForecasts) {
        ArrayList<WeatherSpec.Forecast> forecasts = new ArrayList<>();

        for (WeekForecast day : weekForecasts) {
            if (day.getCity_id() == cityID) {
                WeatherSpec.Forecast temp = new WeatherSpec.Forecast(
                        Converter.celsiusToKelvin(day.getMinTemperature()),
                        Converter.celsiusToKelvin(day.getMaxTemperature()),
                        Converter.convertOMtoOW(day.getWeatherID()),
                        (int) day.getHumidity());
                forecasts.add(temp);
            }
        }

        forecasts.remove(0);    //first element is always current day's data.
        return forecasts;
    }
}
