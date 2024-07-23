package org.woheller69.weather.weather_api.open_meteo;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.woheller69.weather.database.CurrentWeatherData;
import org.woheller69.weather.database.HourlyForecast;
import org.woheller69.weather.database.QuarterHourlyForecast;
import org.woheller69.weather.database.WeekForecast;
import org.woheller69.weather.weather_api.IApiToDatabaseConversion;
import org.woheller69.weather.weather_api.IDataExtractor;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a concrete implementation for extracting weather data that was retrieved by
 * Open-Meteo.
 */
public class OMDataExtractor implements IDataExtractor {

    private Context context;
    public OMDataExtractor(Context context) {
        this.context = context;
    }

    @Override
    public CurrentWeatherData extractCurrentWeather(String data) {
        try {
            JSONObject jsonData = new JSONObject(data);
            CurrentWeatherData weatherData = new CurrentWeatherData();
            weatherData.setTimestamp(System.currentTimeMillis() / 1000);
            IApiToDatabaseConversion conversion = new OMToDatabaseConversion();
            if (jsonData.has("weathercode")) weatherData.setWeatherID(conversion.convertWeatherCategory(jsonData.getString("weathercode")));
            if (jsonData.has("temperature")) weatherData.setTemperatureCurrent((float) jsonData.getDouble("temperature"));
            if (jsonData.has("windspeed")) weatherData.setWindSpeed((float) jsonData.getDouble("windspeed"));
            if (jsonData.has("winddirection")) weatherData.setWindDirection((float) jsonData.getDouble("winddirection"));
            weatherData.setTimeSunrise(0L);
            weatherData.setTimeSunset(0L);
            weatherData.setHumidity(0);
            weatherData.setPressure(0);
            weatherData.setCloudiness(0);

            return weatherData;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public List<WeekForecast> extractWeekForecast(String data) {
        try {
            SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(context);

            List<WeekForecast> weekforecasts = new ArrayList<>();
            JSONObject jsonData = new JSONObject(data);
            JSONArray timeArray = jsonData.getJSONArray("time");
            JSONArray weathercodeArray = jsonData.has("weather_code") ? jsonData.getJSONArray("weather_code") : null;
            JSONArray tempMaxArray = null;
            if (jsonData.has("temperature_2m_max")) tempMaxArray = jsonData.getJSONArray("temperature_2m_max");
            if (jsonData.has("apparent_temperature_max")) tempMaxArray = jsonData.getJSONArray("apparent_temperature_max");
            JSONArray tempMinArray = null;
            if (jsonData.has("temperature_2m_min")) tempMinArray = jsonData.getJSONArray("temperature_2m_min");
            if (jsonData.has("apparent_temperature_min")) tempMinArray = jsonData.getJSONArray("apparent_temperature_min");
            JSONArray sunriseArray = jsonData.has("sunrise") ? jsonData.getJSONArray("sunrise") : null;
            JSONArray sunsetArray = jsonData.has("sunset") ? jsonData.getJSONArray("sunset") : null;
            JSONArray uvIndexArray = jsonData.has("uv_index_max") ? jsonData.getJSONArray("uv_index_max") : null;
            JSONArray precipitationArray = jsonData.has("precipitation_sum") ? jsonData.getJSONArray("precipitation_sum") : null;
            JSONArray windSpeedArray = jsonData.has("wind_speed_10m_max") ? jsonData.getJSONArray("wind_speed_10m_max") : null;
            JSONArray snowfallArray = jsonData.has("snowfall_sum") ? jsonData.getJSONArray("snowfall_sum") : null;
            JSONArray showersArray = jsonData.has("showers_sum") ? jsonData.getJSONArray("showers_sum") : null;
            JSONArray rainArray = jsonData.has("rain_sum") ? jsonData.getJSONArray("rain_sum") : null;
            JSONArray sunshineDurationArray = jsonData.has("sunshine_duration") ? jsonData.getJSONArray("sunshine_duration") : null;

            IApiToDatabaseConversion conversion = new OMToDatabaseConversion();
            for (int i = 0; i < timeArray.length(); i++) {
                WeekForecast weekForecast = new WeekForecast();
                weekForecast.setTimestamp(System.currentTimeMillis() / 1000);
                if (timeArray != null && !timeArray.isNull(i)) weekForecast.setForecastTime((timeArray.getLong(i)+12*3600)*1000L);  //shift to midday
                if (weathercodeArray != null && !weathercodeArray.isNull(i)) weekForecast.setWeatherID(conversion.convertWeatherCategory(weathercodeArray.getString(i)));
                if (tempMaxArray != null && !tempMaxArray.isNull(i)) weekForecast.setMaxTemperature((float) tempMaxArray.getDouble(i));
                if (tempMinArray != null && !tempMinArray.isNull(i)) weekForecast.setMinTemperature((float) tempMinArray.getDouble(i));
                if (sunriseArray != null && !sunriseArray.isNull(i)) weekForecast.setTimeSunrise(sunriseArray.getLong(i));
                if (sunsetArray != null && !sunsetArray.isNull(i)) weekForecast.setTimeSunset(sunsetArray.getLong(i));
                if (sunshineDurationArray != null && !sunshineDurationArray.isNull(i)) weekForecast.setSunshineHours((float) (sunshineDurationArray.getDouble(i)/3600));
                if (uvIndexArray != null && !uvIndexArray.isNull(i)) {
                    weekForecast.setUv_index((float) uvIndexArray.getDouble(i));
                } else weekForecast.setUv_index(-1);
                if (prefManager.getBoolean("pref_snow", false)) {
                    float precipitationAmount=0;
                    if (snowfallArray != null && !snowfallArray.isNull(i)) precipitationAmount += (float) snowfallArray.getDouble(i)*10;  //snowfall is reported in cm instead of mm
                    if (rainArray != null && !rainArray.isNull(i)) precipitationAmount += (float) rainArray.getDouble(i);
                    if (showersArray != null &&!showersArray.isNull(i)) precipitationAmount += (float) showersArray.getDouble(i);
                    weekForecast.setPrecipitation(precipitationAmount);
                } else {
                    if (precipitationArray != null && !precipitationArray.isNull(i)) weekForecast.setPrecipitation((float) precipitationArray.getDouble(i));
                }
                if (windSpeedArray != null && !windSpeedArray.isNull(i)) weekForecast.setWind_speed((float) windSpeedArray.getDouble(i));
                weekforecasts.add(weekForecast);
            }
            return weekforecasts;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @see IDataExtractor#extractHourlyForecast(String)
     */
    @Override
    public List<HourlyForecast> extractHourlyForecast(String data) {
        try {
            SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(context);

            List<HourlyForecast> hourlyForecasts = new ArrayList<>();
            JSONObject jsonData = new JSONObject(data);
            JSONArray timeArray = jsonData.getJSONArray("time");
            JSONArray weathercodeArray = jsonData.has("weather_code") ? jsonData.getJSONArray("weather_code") : null;
            JSONArray tempArray = null;
            if (jsonData.has("temperature_2m")) tempArray = jsonData.getJSONArray("temperature_2m");
            if (jsonData.has("apparent_temperature")) tempArray = jsonData.getJSONArray("apparent_temperature");
            JSONArray rhArray = jsonData.has("relative_humidity_2m") ? jsonData.getJSONArray("relative_humidity_2m") : null;
            JSONArray pressureArray = jsonData.has("pressure_msl") ? jsonData.getJSONArray("pressure_msl") : null;
            JSONArray precipitationArray = jsonData.has("precipitation") ? jsonData.getJSONArray("precipitation") : null;
            JSONArray windSpeedArray = jsonData.has("wind_speed_10m") ? jsonData.getJSONArray("wind_speed_10m") : null;
            JSONArray windDirArray = jsonData.has("wind_direction_10m") ? jsonData.getJSONArray("wind_direction_10m") : null;
            JSONArray snowfallArray = jsonData.has("snowfall") ? jsonData.getJSONArray("snowfall") : null;
            JSONArray showersArray = jsonData.has("showers") ? jsonData.getJSONArray("showers") : null;
            JSONArray rainArray = jsonData.has("rain") ? jsonData.getJSONArray("rain") : null;

            IApiToDatabaseConversion conversion = new OMToDatabaseConversion();
            for (int i = 0; i < timeArray.length(); i++) {
                HourlyForecast hourlyForecast = new HourlyForecast();
                hourlyForecast.setTimestamp(System.currentTimeMillis() / 1000);
                if (timeArray != null && !timeArray.isNull(i)) hourlyForecast.setForecastTime(timeArray.getLong(i)*1000L);
                if (weathercodeArray != null && !weathercodeArray.isNull(i)) hourlyForecast.setWeatherID(conversion.convertWeatherCategory(weathercodeArray.getString(i)));
                //if (weathercodeArray != null) hourlyForecast.setWeatherID(conversion.convertWeatherCategory(Integer.toString(i-24)));  //for icon test
                if (tempArray != null && !tempArray.isNull(i)) hourlyForecast.setTemperature((float) tempArray.getDouble(i));
                if (rhArray != null && !rhArray.isNull(i)) hourlyForecast.setHumidity((float) rhArray.getDouble(i));
                //if (rhArray != null) hourlyForecast.setHumidity((float) (i-24));  //for icon test
                if (pressureArray != null && !pressureArray.isNull(i)) hourlyForecast.setPressure((float) pressureArray.getDouble(i));
                if (prefManager.getBoolean("pref_snow", false)) {
                    float precipitationAmount=0;
                    if (snowfallArray != null && !snowfallArray.isNull(i)) precipitationAmount += (float) snowfallArray.getDouble(i)*10;  //snowfall is reported in cm instead of mm
                    if (rainArray != null && !rainArray.isNull(i)) precipitationAmount += (float) rainArray.getDouble(i);
                    if (showersArray != null && !showersArray.isNull(i)) precipitationAmount += (float) showersArray.getDouble(i);
                    hourlyForecast.setPrecipitation(precipitationAmount);
                } else {
                    if (precipitationArray != null && !precipitationArray.isNull(i)) hourlyForecast.setPrecipitation((float) precipitationArray.getDouble(i));
                }
                if (windSpeedArray != null && !windSpeedArray.isNull(i)) hourlyForecast.setWindSpeed((float) windSpeedArray.getDouble(i));
                if (windDirArray != null && !windDirArray.isNull(i)) hourlyForecast.setWindDirection((float) windDirArray.getDouble(i));
                hourlyForecasts.add(hourlyForecast);
            }
            return hourlyForecasts;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @see IDataExtractor#extractQuarterHourlyForecast(String)
     */
    @Override
    public List<QuarterHourlyForecast> extractQuarterHourlyForecast(String data) {
        try {
            SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(context);

            List<QuarterHourlyForecast> quarterHourlyForecasts = new ArrayList<>();
            JSONObject jsonData = new JSONObject(data);
            JSONArray timeArray = jsonData.getJSONArray("time");
            JSONArray weathercodeArray = jsonData.has("weather_code") ? jsonData.getJSONArray("weather_code") : null;
            JSONArray tempArray = null;
            if (jsonData.has("temperature_2m")) tempArray = jsonData.getJSONArray("temperature_2m");
            if (jsonData.has("apparent_temperature")) tempArray = jsonData.getJSONArray("apparent_temperature");
            JSONArray precipitationArray = jsonData.has("precipitation") ? jsonData.getJSONArray("precipitation") : null;
            JSONArray windSpeedArray = jsonData.has("wind_speed_10m") ? jsonData.getJSONArray("wind_speed_10m") : null;
            JSONArray snowfallArray = jsonData.has("snowfall") ? jsonData.getJSONArray("snowfall") : null;
            JSONArray showersArray = jsonData.has("showers") ? jsonData.getJSONArray("showers") : null;
            JSONArray rainArray = jsonData.has("rain") ? jsonData.getJSONArray("rain") : null;

            IApiToDatabaseConversion conversion = new OMToDatabaseConversion();
            for (int i = 0; i < timeArray.length(); i++) {
                QuarterHourlyForecast quarterHourlyForecast = new QuarterHourlyForecast();
                quarterHourlyForecast.setTimestamp(System.currentTimeMillis() / 1000);
                if (timeArray != null && !timeArray.isNull(i)) quarterHourlyForecast.setForecastTime(timeArray.getLong(i)*1000L);
                if (weathercodeArray != null && !weathercodeArray.isNull(i)) quarterHourlyForecast.setWeatherID(conversion.convertWeatherCategory(weathercodeArray.getString(i)));
                if (tempArray != null && !tempArray.isNull(i)) quarterHourlyForecast.setTemperature((float) tempArray.getDouble(i));
                if (prefManager.getBoolean("pref_snow", false)) {
                    float precipitationAmount=0;
                    if (snowfallArray != null && !snowfallArray.isNull(i)) precipitationAmount += (float) snowfallArray.getDouble(i)*10;  //snowfall is reported in cm instead of mm
                    if (rainArray != null && !rainArray.isNull(i)) precipitationAmount += (float) rainArray.getDouble(i);
                    if (showersArray != null && !showersArray.isNull(i)) precipitationAmount += (float) showersArray.getDouble(i);
                    quarterHourlyForecast.setPrecipitation(precipitationAmount);
                } else {
                    if (precipitationArray != null && !precipitationArray.isNull(i)) quarterHourlyForecast.setPrecipitation((float) precipitationArray.getDouble(i));
                }
                if (windSpeedArray != null && !windSpeedArray.isNull(i)) quarterHourlyForecast.setWindSpeed((float) windSpeedArray.getDouble(i));
                quarterHourlyForecasts.add(quarterHourlyForecast);
            }
            return quarterHourlyForecasts;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
