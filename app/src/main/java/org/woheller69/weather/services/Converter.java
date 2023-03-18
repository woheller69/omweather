package org.woheller69.weather.services;

import org.woheller69.weather.weather_api.IApiToDatabaseConversion.WeatherCategories;

public class Converter {
    /**
     * Converts OpenMeteo weather code to Open Weather weather code.
     * Refer to respective sites for code descriptions.
     *  OWM: https://openweathermap.org/weather-conditions
     *  OpenMateo: https://open-meteo.com/en/docs
     * @param weatherCode open weather code
     * @return open meteo code
     */
    public static int convertOMtoOW(int weatherCode)
    {
        WeatherCategories weather = WeatherCategories.getWeatherCategory(weatherCode);
        switch (weather) {
            case CLEAR_SKY:
                return 800;
            case FEW_CLOUDS:
                return 801;
            case SCATTERED_CLOUDS:
                return 802;
            case OVERCAST_CLOUDS:
                return 804;
            case MIST:
                return 741;
            case DRIZZLE_RAIN:
                return 300;
            case FREEZING_DRIZZLE_RAIN:
            case LIGHT_FREEZING_RAIN:
            case FREEZING_RAIN:
                return 511;
            case LIGHT_RAIN:
                return 500;
            case MODERATE_RAIN:
                return 501;
            case HEAVY_RAIN:
                return 502;
            case LIGHT_SNOW:
                return 600;
            case MODERATE_SNOW:
                return 601;
            case HEAVY_SNOW:
                return 602;
            case LIGHT_SHOWER_RAIN:
                return 520;
            case SHOWER_RAIN:
                return 521;
            case LIGHT_SHOWER_SNOW:
                return 620;
            case SHOWER_SNOW:
                return 621;
            case THUNDERSTORM:
                return 211;
            case SHOWER_RAIN_SNOW:
                return 615;
            case THUNDERSTORM_HAIL:
                return 202;
            default:
                return 800;
        }
    }

    public static String convertOMtoOWString(int weatherCode)
    {
        WeatherCategories weather = WeatherCategories.getWeatherCategory(weatherCode);
        switch (weather) {
            case CLEAR_SKY:
                return "clear sky";
            case FEW_CLOUDS:
                return "few clouds";
            case SCATTERED_CLOUDS:
                return "scattered clouds";
            case OVERCAST_CLOUDS:
                return "overcast clouds";
            case MIST:
                return "mist";
            case DRIZZLE_RAIN:
                return "drizzle";
            case FREEZING_DRIZZLE_RAIN:
            case LIGHT_FREEZING_RAIN:
            case FREEZING_RAIN:
                return "freezing rain";
            case LIGHT_RAIN:
                return "light rain";
            case MODERATE_RAIN:
                return "moderate rain";
            case HEAVY_RAIN:
                return "heavy intensity rain";
            case LIGHT_SNOW:
                return "light snow";
            case MODERATE_SNOW:
                return "snow";
            case HEAVY_SNOW:
                return "heavy snow";
            case LIGHT_SHOWER_RAIN:
                return "light intensity shower rain";
            case SHOWER_RAIN:
                return "shower rain";
            case LIGHT_SHOWER_SNOW:
                return "light shower snow";
            case SHOWER_SNOW:
                return "shower snow";
            case THUNDERSTORM:
                return "thunderstorm";
            case SHOWER_RAIN_SNOW:
                return "light rain and snow";
            case THUNDERSTORM_HAIL:
                return "thunderstorm with heavy rain";
            default:
                return "clear sky";
        }
    }

    public static int celsiusToKelvin(float celTemp)
    {
        return (int) Math.round(celTemp + 273.15);
    }
}
