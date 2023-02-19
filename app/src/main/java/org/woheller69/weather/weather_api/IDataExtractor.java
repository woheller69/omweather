package org.woheller69.weather.weather_api;

import org.woheller69.weather.database.CurrentWeatherData;
import org.woheller69.weather.database.HourlyForecast;
import org.woheller69.weather.database.WeekForecast;
import java.util.List;

/**
 * This interface defines the frame of the functionality to extractCurrentWeatherData weather information from which
 * is returned by some API.
 */
public interface IDataExtractor {

    /**
     * @param data The data that contains the information to instantiate a CurrentWeatherData
     *             object. In the easiest case this is the (HTTP) response of the One Call API.
     * @return Returns the extracted information as a CurrentWeatherData instance.
     */
    CurrentWeatherData extractCurrentWeather(String data);

    /**
     * @param data The data that contains the information to instantiate a Forecast object.
     * @return Returns the extracted weather forecast information. In case some error occurs, null
     * will be returned.
     */
    List<WeekForecast> extractWeekForecast(String data);

    /**
     * @param data The data that contains the information to instantiate a Forecast object.
     * @return Returns the extracted weather forecast information. In case some error occurs, null
     * will be returned.
     */

    List<HourlyForecast> extractHourlyForecast(String data);

    /**
     * @param data0, data1, data2, data3, data4 contain the information to retrieve the rain for a minute within the next 60min.
     * @return Returns a string with a rain drop in case of rain or a - in case of no rain
     */
    String extractRain60min(String data0,String data1, String data2, String data3, String data4);

}
