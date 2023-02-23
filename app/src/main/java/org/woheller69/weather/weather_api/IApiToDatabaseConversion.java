package org.woheller69.weather.weather_api;

/**
 * This interface class defines a set of methods that guarantee that even the use of multiple APIs
 * result in the same data bases.
 */
public abstract class IApiToDatabaseConversion {

    /**
     * This enum provides a list of all available weather categories and assigns them a numerical
     * value. Please note that is ordered from best weather (CLEAR_SKY) to worst weather
     * (THUNDERSTORM).
     */
    public enum WeatherCategories {
        ERROR(-1),
        CLEAR_SKY(0),
        FEW_CLOUDS(1),
        SCATTERED_CLOUDS(2),
        OVERCAST_CLOUDS(3),
        MIST(45),
        DRIZZLE_RAIN(53),
        FREEZING_DRIZZLE_RAIN(57),
        LIGHT_RAIN(61),
        MODERATE_RAIN(63),
        HEAVY_RAIN(65),
        LIGHT_FREEZING_RAIN(66),
        FREEZING_RAIN(67),
        LIGHT_SNOW(71),
        MODERATE_SNOW(73),
        HEAVY_SNOW(75),
        LIGHT_SHOWER_RAIN(80),
        SHOWER_RAIN(81),
        SHOWER_RAIN_SNOW(84),   //only used as icon in week forecasts
        LIGHT_SHOWER_SNOW(85),
        SHOWER_SNOW(86),
        THUNDERSTORM(95),
        THUNDERSTORM_HAIL(96);

        private int numVal;

        WeatherCategories(int numVal) {
            this.numVal = numVal;
        }

        public int getNumVal() {
            return numVal;
        }

    }


    /**
     * Different APIs will use different representation for weather conditions / categories.
     * Internally, they will stored uniformly.
     *
     * @param category The category to convert into the internal representation.
     * @return Returns 10 for clear sky, 20 for (few) clouds, 30 for scattered cloud, 40 for broken
     * clouds, 50 for shower rain, 60 for rain, 70 for thunderstorm, 80 for snow, 90 for mist.
     */
    public abstract int convertWeatherCategory(String category);

}
