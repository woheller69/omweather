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
        CLEAR_SKY(10),
        FEW_CLOUDS(20),
        SCATTERED_CLOUDS(30),
        BROKEN_CLOUDS(40),
        OVERCAST_CLOUDS(45),
        MIST(50),
        DRIZZLE_RAIN(60),
        LIGHT_RAIN(70),
        MODERATE_RAIN(71),
        HEAVY_RAIN(72),
        SHOWER_RAIN(75),
        LIGHT_SNOW(80),
        MODERATE_SNOW(81),
        HEAVY_SNOW(82),
        RAIN_SNOW(83),
        SHOWER_SNOW(85),
        SHOWER_RAIN_SNOW(86),   //only used as icon in week forecasts
        THUNDERSTORM(90);

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
