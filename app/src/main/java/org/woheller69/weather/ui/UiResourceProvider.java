package org.woheller69.weather.ui;

import org.woheller69.weather.R;
import org.woheller69.weather.weather_api.IApiToDatabaseConversion.WeatherCategories;

/**
 * This static class provides image / icon resources for the UI.
 */
public class UiResourceProvider {

    /**
     * Private constructor in order to make this class static.
     */
    private UiResourceProvider() {
    }

    /**
     * @param categoryNumber The category number. See IApiToDatabaseConversion#WeatherCategories
     *                       for details.
     * @param isDay          True if TimeStamp between sunrise and sunset
     * @return Returns the icon (resource) that belongs to the given category number.
     */
    public static int getIconResourceForWeatherCategory(int categoryNumber, boolean isDay) {
        if (categoryNumber == WeatherCategories.CLEAR_SKY.getNumVal()) {
            if (isDay) {
                return R.drawable.wmo_icon_00d;
            } else {
                return R.drawable.wmo_icon_00n;
            }
        } else if (categoryNumber == WeatherCategories.FEW_CLOUDS.getNumVal()) {
            if (isDay) {
                return R.drawable.wmo_icon_01d;
            } else {
                return R.drawable.wmo_icon_01n;
            }
        } else if (categoryNumber == WeatherCategories.SCATTERED_CLOUDS.getNumVal()) {
            if (isDay) {
                return R.drawable.wmo_icon_02d;
            } else {
                return R.drawable.wmo_icon_02n;
            }
        } else if (categoryNumber == WeatherCategories.OVERCAST_CLOUDS.getNumVal()) {
            if (isDay) {
                return R.drawable.wmo_icon_03d;
            } else {
                return R.drawable.wmo_icon_03n;
            }
        } else if (categoryNumber == WeatherCategories.MIST.getNumVal()) {
            if (isDay) {
                return R.drawable.wmo_icon_45d;
            } else {
                return R.drawable.wmo_icon_45n;
            }
        } else if (categoryNumber == WeatherCategories.DRIZZLE_RAIN.getNumVal()) {
            if (isDay) {
                return R.drawable.wmo_icon_53d;
            } else {
                return R.drawable.wmo_icon_53n;
            }
        } else if (categoryNumber == WeatherCategories.FREEZING_DRIZZLE_RAIN.getNumVal()) {
            if (isDay) {
                return R.drawable.wmo_icon_57d;
            } else {
                return R.drawable.wmo_icon_57n;
            }
        } else if (categoryNumber == WeatherCategories.LIGHT_RAIN.getNumVal()) {
            if (isDay) {
                return R.drawable.wmo_icon_61d;
            } else {
                return R.drawable.wmo_icon_61n;
            }
        } else if (categoryNumber == WeatherCategories.MODERATE_RAIN.getNumVal()) {
            if (isDay) {
                return R.drawable.wmo_icon_63d;
            } else {
                return R.drawable.wmo_icon_63n;
            }
        } else if (categoryNumber == WeatherCategories.HEAVY_RAIN.getNumVal()) {
            if (isDay) {
                return R.drawable.wmo_icon_65d;
            } else {
                return R.drawable.wmo_icon_65n;
            }
        } else if (categoryNumber == WeatherCategories.LIGHT_SHOWER_RAIN.getNumVal()) {
            if (isDay) {
                return R.drawable.wmo_icon_80d;
            } else {
                return R.drawable.wmo_icon_80n;
            }
        } else if (categoryNumber == WeatherCategories.SHOWER_RAIN.getNumVal()) {
            if (isDay) {
                return R.drawable.wmo_icon_81d;
            } else {
                return R.drawable.wmo_icon_81n;
            }
        } else if (categoryNumber == WeatherCategories.LIGHT_SNOW.getNumVal()) {
            if (isDay) {
                return R.drawable.wmo_icon_71d;
            } else {
                return R.drawable.wmo_icon_71n;
            }
        } else if (categoryNumber == WeatherCategories.MODERATE_SNOW.getNumVal()) {
            if (isDay) {
                return R.drawable.wmo_icon_73d;
            } else {
                return R.drawable.wmo_icon_73n;
            }
        } else if (categoryNumber == WeatherCategories.HEAVY_SNOW.getNumVal()) {
            if (isDay) {
                return R.drawable.wmo_icon_75d;
            } else {
                return R.drawable.wmo_icon_75n;
            }
        } else if (categoryNumber == WeatherCategories.LIGHT_FREEZING_RAIN.getNumVal()) {
            if (isDay) {
                return R.drawable.wmo_icon_66d;
            } else {
                return R.drawable.wmo_icon_66n;
            }
        } else if (categoryNumber == WeatherCategories.FREEZING_RAIN.getNumVal()) {
            if (isDay) {
                return R.drawable.wmo_icon_67d;
            } else {
                return R.drawable.wmo_icon_67n;
            }
        } else if (categoryNumber == WeatherCategories.LIGHT_SHOWER_SNOW.getNumVal()) {
            if (isDay) {
                return R.drawable.wmo_icon_85d;
            } else {
                return R.drawable.wmo_icon_85n;
            }
        } else if (categoryNumber == WeatherCategories.SHOWER_SNOW.getNumVal()) {
            if (isDay) {
                return R.drawable.wmo_icon_86d;
            } else {
                return R.drawable.wmo_icon_86n;
            }
        } else if (categoryNumber == WeatherCategories.SHOWER_RAIN_SNOW.getNumVal()) {
            if (isDay) {
                return R.drawable.wmo_icon_84d;
            } else {
                return R.drawable.wmo_icon_84n;
            }
        } else if (categoryNumber == WeatherCategories.THUNDERSTORM.getNumVal()) {
            if (isDay) {
                return R.drawable.wmo_icon_95d;
            } else {
                return R.drawable.wmo_icon_95n;
            }
        } else if (categoryNumber == WeatherCategories.THUNDERSTORM_HAIL.getNumVal()) {
            if (isDay) {
                return R.drawable.wmo_icon_96d;
            } else {
                return R.drawable.wmo_icon_96n;
            }
        } else {  //this should not occur
                return R.drawable.wmo_icon_error;
            }
    }

    /**
     * @param categoryNumber The category number. See IApiToDatabaseConversion#WeatherCategories
     *                       for details.
     * @param isDay          True if TimeStamp between sunrise and sunset
     * @return Returns the image resource that belongs to the given category number.
     */
    public static int getImageResourceForWeatherCategory(int categoryNumber, boolean isDay) {
        if (categoryNumber == WeatherCategories.CLEAR_SKY.getNumVal()) {
            if (isDay) {
                return R.drawable.wmo_image_00d;
            } else {
                return R.drawable.wmo_image_00n;
            }
        } else if (categoryNumber == WeatherCategories.FEW_CLOUDS.getNumVal()) {
            if (isDay) {
                return R.drawable.wmo_image_01d;
            } else {
                return R.drawable.wmo_image_01n;
            }
        } else if (categoryNumber == WeatherCategories.SCATTERED_CLOUDS.getNumVal()) {
            if (isDay) {
                return R.drawable.wmo_image_02d;
            } else {
                return R.drawable.wmo_image_02n;
            }
        } else if (categoryNumber == WeatherCategories.OVERCAST_CLOUDS.getNumVal()) {
            if (isDay) {
                return R.drawable.wmo_image_03d;
            } else {
                return R.drawable.wmo_image_03n;
            }
        } else if (categoryNumber == WeatherCategories.MIST.getNumVal()) {
            if (isDay) {
                return R.drawable.wmo_image_45d;
            } else {
                return R.drawable.wmo_image_45n;
            }
        } else if (categoryNumber == WeatherCategories.DRIZZLE_RAIN.getNumVal()) {
            if (isDay) {
                return R.drawable.wmo_image_53d;
            } else {
                return R.drawable.wmo_image_53n;
            }
        } else if (categoryNumber == WeatherCategories.FREEZING_DRIZZLE_RAIN.getNumVal()) {
            if (isDay) {
                return R.drawable.wmo_image_57d;
            } else {
                return R.drawable.wmo_image_57n;
            }
        } else if (categoryNumber == WeatherCategories.LIGHT_RAIN.getNumVal()) {
            if (isDay) {
                return R.drawable.wmo_image_61d;
            } else {
                return R.drawable.wmo_image_61n;
            }
        } else if (categoryNumber == WeatherCategories.MODERATE_RAIN.getNumVal()) {
            if (isDay) {
                return R.drawable.wmo_image_63d;
            } else {
                return R.drawable.wmo_image_63n;
            }
        } else if (categoryNumber == WeatherCategories.HEAVY_RAIN.getNumVal()) {
            if (isDay) {
                return R.drawable.wmo_image_65d;
            } else {
                return R.drawable.wmo_image_65n;
            }
        } else if (categoryNumber == WeatherCategories.LIGHT_SHOWER_RAIN.getNumVal()) {
            if (isDay) {
                return R.drawable.wmo_image_80d;
            } else {
                return R.drawable.wmo_image_80n;
            }
        } else if (categoryNumber == WeatherCategories.SHOWER_RAIN.getNumVal()) {
            if (isDay) {
                return R.drawable.wmo_image_81d;
            } else {
                return R.drawable.wmo_image_81n;
            }
        } else if (categoryNumber == WeatherCategories.LIGHT_SNOW.getNumVal()) {
            if (isDay) {
                return R.drawable.wmo_image_71d;
            } else {
                return R.drawable.wmo_image_71n;
            }
        } else if (categoryNumber == WeatherCategories.MODERATE_SNOW.getNumVal()) {
            if (isDay) {
                return R.drawable.wmo_image_73d;
            } else {
                return R.drawable.wmo_image_73n;
            }
        } else if (categoryNumber == WeatherCategories.HEAVY_SNOW.getNumVal()) {
            if (isDay) {
                return R.drawable.wmo_image_75d;
            } else {
                return R.drawable.wmo_image_75n;
            }
        } else if (categoryNumber == WeatherCategories.LIGHT_FREEZING_RAIN.getNumVal()) {
            if (isDay) {
                return R.drawable.wmo_image_66d;
            } else {
                return R.drawable.wmo_image_66n;
            }
        } else if (categoryNumber == WeatherCategories.FREEZING_RAIN.getNumVal()) {
            if (isDay) {
                return R.drawable.wmo_image_67d;
            } else {
                return R.drawable.wmo_image_67n;
            }
        } else if (categoryNumber == WeatherCategories.LIGHT_SHOWER_SNOW.getNumVal()) {
            if (isDay) {
                return R.drawable.wmo_image_85d;
            } else {
                return R.drawable.wmo_image_85n;
            }
        } else if (categoryNumber == WeatherCategories.SHOWER_SNOW.getNumVal()) {
            if (isDay) {
                return R.drawable.wmo_image_86d;
            } else {
                return R.drawable.wmo_image_86n;
            }
        } else if (categoryNumber == WeatherCategories.SHOWER_RAIN_SNOW.getNumVal()) {
            if (isDay) {
                return R.drawable.wmo_image_84d;
            } else {
                return R.drawable.wmo_image_84n;
            }
        } else if (categoryNumber == WeatherCategories.THUNDERSTORM.getNumVal()) {
            if (isDay) {
                return R.drawable.wmo_image_95d;
            } else {
                return R.drawable.wmo_image_95n;
            }
        } else if (categoryNumber == WeatherCategories.THUNDERSTORM_HAIL.getNumVal()) {
            if (isDay) {
                return R.drawable.wmo_image_96d;
            } else {
                return R.drawable.wmo_image_96n;
            }
        } else {  //this should not occur
                return R.drawable.wmo_image_error;
            }
    }
}
