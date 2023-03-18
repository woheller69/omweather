package org.woheller69.weather.services;

import static org.woheller69.weather.database.SQLiteHelper.getWidgetCityID;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.woheller69.weather.database.CurrentWeatherData;
import org.woheller69.weather.database.SQLiteHelper;
import org.woheller69.weather.database.WeekForecast;

import java.util.List;

public class Broadcaster {
    public static void possiblyUpdateOtherApp(Context context,
                                              CurrentWeatherData weatherData,
                                              List<WeekForecast> weekForecasts) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        boolean run = sp.getBoolean("sendToApp", false);

        if(run && weatherData.getCity_id()==getWidgetCityID(context)) {
            Intent intent = new Intent();
            intent.setAction(sp.getString("sendAction", "nodomain.freeyourgadget.gadgetbridge.ACTION_GENERIC_WEATHER"));
            intent.setPackage(sp.getString("sendPackage","nodomain.freeyourgadget.gadgetbridge"));
            intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            intent.putExtra("WeatherJson", generateWeatherJson(context, weatherData, weekForecasts));
            context.sendBroadcast(intent);
        }
    }

    private static String generateWeatherJson(Context context, CurrentWeatherData weatherData, List<WeekForecast> weekForecasts) {
        JSONArray forecasts = generateForecasts(getWidgetCityID(context), weekForecasts);
        SQLiteHelper dbHelper = SQLiteHelper.getInstance(context);
        JSONObject weatherJson = new JSONObject();

        try {
            weatherJson.put("timestamp", (int) weatherData.getTimestamp());
            weatherJson.put("location", dbHelper.getCityToWatch(weatherData.getCity_id()).getCityName());
            weatherJson.put("currentTemp", Converter.celsiusToKelvin(weatherData.getTemperatureCurrent()));
            weatherJson.put("currentConditionCode", Converter.convertOMtoOW(weatherData.getWeatherID()));
            weatherJson.put("currentCondition", Converter.convertOMtoOWString(weatherData.getWeatherID()));
            weatherJson.put("currentHumidity", (int) weatherData.getHumidity());
            weatherJson.put("todayMaxTemp", Converter.celsiusToKelvin(weekForecasts.get(0).getMaxTemperature()));
            weatherJson.put("todayMinTemp", Converter.celsiusToKelvin(weekForecasts.get(0).getMinTemperature()));
            weatherJson.put("windSpeed", weatherData.getWindSpeed());
            weatherJson.put("windDirection",(int) weatherData.getWindDirection());
            weatherJson.put("forecasts", forecasts);
        } catch (Exception  e){
            //
        }

        return weatherJson.toString();
    }

    private static JSONArray generateForecasts(int cityID, List<WeekForecast> weekForecasts) {
        JSONArray forecasts = new JSONArray();

        try{
            for (WeekForecast day : weekForecasts) {
                if (day.getCity_id() == cityID) {
                    JSONObject temp = new JSONObject();
                    temp.put("minTemp", Converter.celsiusToKelvin(day.getMinTemperature()));
                    temp.put("maxTemp", Converter.celsiusToKelvin(day.getMaxTemperature()));
                    temp.put("conditionCode", Converter.convertOMtoOW(day.getWeatherID()));
                    temp.put("humidity", (int) day.getHumidity());
                    forecasts.put(temp);
                }
            }
        }catch (JSONException e)
        {
            //
        }

        forecasts.remove(0);    //first element is always current day's data.
        return forecasts;
    }
}
