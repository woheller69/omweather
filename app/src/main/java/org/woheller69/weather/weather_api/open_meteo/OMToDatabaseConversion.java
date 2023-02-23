package org.woheller69.weather.weather_api.open_meteo;

import org.woheller69.weather.weather_api.IApiToDatabaseConversion;

public class OMToDatabaseConversion extends IApiToDatabaseConversion {

    @Override
    public int convertWeatherCategory(String category) {
        int value = Integer.parseInt(category);
        if (value == 0) {
            return WeatherCategories.CLEAR_SKY.getNumVal();
        } else if (value == 1) {
            return WeatherCategories.FEW_CLOUDS.getNumVal();
        } else if (value == 2) {
            return WeatherCategories.SCATTERED_CLOUDS.getNumVal();
        } else if (value == 3) {
            return WeatherCategories.OVERCAST_CLOUDS.getNumVal();
        } else if (value >= 45 && value <= 48) {
            return WeatherCategories.MIST.getNumVal();
        } else if (value >= 50 && value <= 55) {
            return WeatherCategories.DRIZZLE_RAIN.getNumVal();
        } else if (value >= 56 && value <= 57) {
            return WeatherCategories.FREEZING_DRIZZLE_RAIN.getNumVal();
        } else if (value >= 60 && value <= 61) {
            return WeatherCategories.LIGHT_RAIN.getNumVal();
        } else if (value >= 62 && value <=63) {
            return WeatherCategories.MODERATE_RAIN.getNumVal();
        } else if (value >= 64 && value <=65) {
            return WeatherCategories.HEAVY_RAIN.getNumVal();
        } else if (value == 66) {
            return WeatherCategories.LIGHT_FREEZING_RAIN.getNumVal();
        } else if (value == 67) {
            return WeatherCategories.FREEZING_RAIN.getNumVal();
        } else if (value == 70 || value == 71 || value == 77) {  // 77=snow grain
            return WeatherCategories.LIGHT_SNOW.getNumVal();
        } else if (value >= 72 && value <=73) {
            return WeatherCategories.MODERATE_SNOW.getNumVal();
        } else if (value >= 74 && value <=75 ) {
            return WeatherCategories.HEAVY_SNOW.getNumVal();
        } else if (value == 80 ) {
            return WeatherCategories.LIGHT_SHOWER_RAIN.getNumVal();
        } else if (value == 81 || value == 82 ) {
            return WeatherCategories.SHOWER_RAIN.getNumVal();
        } else if (value == 85) {
            return WeatherCategories.LIGHT_SHOWER_SNOW.getNumVal();
        } else if (value == 86) {
            return WeatherCategories.SHOWER_SNOW.getNumVal();
        } else if (value == 95) {
            return WeatherCategories.THUNDERSTORM.getNumVal();
        } else if (value == 96 || value == 99) {
            return WeatherCategories.THUNDERSTORM_HAIL.getNumVal();
        }
        // Fallback: ERROR
        return WeatherCategories.ERROR.getNumVal();
    }

}
