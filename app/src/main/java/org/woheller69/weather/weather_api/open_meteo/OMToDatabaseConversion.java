package org.woheller69.weather.weather_api.open_meteo;

import org.woheller69.weather.weather_api.IApiToDatabaseConversion;

public class OMToDatabaseConversion extends IApiToDatabaseConversion {

    @Override
    public int convertWeatherCategory(String category) {
        int value = Integer.parseInt(category);
        if (value >= 95 && value <= 99) {
            return WeatherCategories.THUNDERSTORM.getNumVal();  //TODO including slight and heavy hail ?
        } else if (value >= 51 && value <= 57) {        //TODO including Freezing drizzle 56 57 ?
            return WeatherCategories.DRIZZLE_RAIN.getNumVal();
        } else if (value == 61) {
            return WeatherCategories.LIGHT_RAIN.getNumVal();
        } else if (value == 63 ) {
            return WeatherCategories.MODERATE_RAIN.getNumVal();
        } else if (value == 65) {
            return WeatherCategories.HEAVY_RAIN.getNumVal();
        } else if (value >= 80 && value <= 81 ) {
            return WeatherCategories.LIGHT_SHOWER_RAIN.getNumVal();
        } else if (value == 82 ) {
            return WeatherCategories.SHOWER_RAIN.getNumVal();
        } else if (value == 71 || value == 77) {  // 77=snow grain
            return WeatherCategories.LIGHT_SNOW.getNumVal();
        } else if (value == 73) {
            return WeatherCategories.MODERATE_SNOW.getNumVal();
        } else if (value == 75 ) {
            return WeatherCategories.HEAVY_SNOW.getNumVal();
        } else if (value == 85) {
            return WeatherCategories.LIGHT_SHOWER_SNOW.getNumVal();
        } else if (value == 86) {
            return WeatherCategories.SHOWER_SNOW.getNumVal();
        } else if ((value >= 66 && value <= 67)) {  //TODO?? 66=Freezing light rain 67=Freezing heavy rain
            return WeatherCategories.RAIN_SNOW.getNumVal();
        } else if (value >= 45 && value <= 48) {
            return WeatherCategories.MIST.getNumVal();
        } else if (value == 0) {
            return WeatherCategories.CLEAR_SKY.getNumVal();
        } else if (value == 1) {
            return WeatherCategories.FEW_CLOUDS.getNumVal();
        } else if (value == 2) {
            return WeatherCategories.SCATTERED_CLOUDS.getNumVal();
        } else if (value == 3) {
            return WeatherCategories.OVERCAST_CLOUDS.getNumVal();
        }
        // Fallback: Clouds
        return WeatherCategories.OVERCAST_CLOUDS.getNumVal();
    }

}
