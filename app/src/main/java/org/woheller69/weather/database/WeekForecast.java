package org.woheller69.weather.database;

import android.content.Context;

/**
 * This class is the database model for the forecasts table.
 */
public class WeekForecast {

    public static final float NO_RAIN_VALUE = 0;
    private int id;
    private int city_id;
    private long timestamp;
    private long forecastFor;
    private int weatherID;
    private float temperature;
    private float temperature_min;
    private float temperature_max;
    private float humidity;
    private float pressure;
    private float precipitation;
    private float wind_speed;
    private float wind_direction;
    private float uv_index;
    private long timeSunrise;
    private long timeSunset;

    public WeekForecast() {
    }

    public WeekForecast(int id, int city_id, long timestamp, long forecastFor, int weatherID, float temperature, float temperature_min, float temperature_max, float humidity, float pressure, float precipitation, float wind_speed, float wind_direction, float uv_index) {
        this.id = id;
        this.city_id = city_id;
        this.timestamp = timestamp;
        this.forecastFor = forecastFor;
        this.weatherID = weatherID;
        this.temperature = temperature;
        this.temperature_min = temperature_min;
        this.temperature_max = temperature_max;
        this.humidity = humidity;
        this.pressure = pressure;
        this.precipitation=precipitation;
        this.wind_speed=wind_speed;
        this.wind_direction=wind_direction;
        this.uv_index=uv_index;
    }


    /**
     * @return Returns the ID of the record (which uniquely identifies the record).
     */
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return Returns the date and time for the forecast.
     */
    public long getForecastTime() {
        return forecastFor;
    }

    /**
     * @return Returns the local time for the forecast in UTC epoch
     */
    public long getLocalForecastTime(Context context) {
        SQLiteHelper dbhelper = SQLiteHelper.getInstance(context);
        int timezoneseconds = dbhelper.getCurrentWeatherByCityId(city_id).getTimeZoneSeconds();
        return forecastFor + timezoneseconds * 1000L;
    }

    /**
     * @param forecastFor The point of time for the forecast.
     */
    public void setForecastTime(long forecastFor) {
        this.forecastFor = forecastFor;
    }

    /**
     * @return Returns the point of time when the data was inserted into the database in Unix, UTC.
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * @param timestamp The point of time to set when the data was inserted into the database in
     *                  Unix, UTC.
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getCity_id() {
        return city_id;
    }

    public void setCity_id(int city_id) {
        this.city_id = city_id;
    }

    /**
     * @return Returns the weather condition ID.
     */
    public int getWeatherID() {
        return weatherID;
    }

    /**
     * @param weatherID The weather condition ID to set.
     */
    public void setWeatherID(int weatherID) {
        this.weatherID = weatherID;
    }

    /**
     * @return Returns the current temperature in Celsius.
     */
    public float getTemperature() {
        return temperature;
    }

    /**
     * @param temperature The current temperature to set in Celsius.
     */
    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    /**
     * @return Returns the min temperature in Celsius.
     */
    public float getMinTemperature() {
        return temperature_min;
    }

    /**
     * @param temperature_min The min temperature to set in Celsius.
     */
    public void setMinTemperature(float temperature_min) {
        this.temperature_min = temperature_min;
    }

    /**
     * @return Returns the max temperature in Celsius.
     */
    public float getMaxTemperature() {
        return temperature_max;
    }

    /**
     * @param temperature_max The max temperature to set in Celsius.
     */
    public void setMaxTemperature(float temperature_max) {
        this.temperature_max = temperature_max;
    }


    /**
     * @return Returns the humidity value in percent.
     */
    public float getHumidity() {
        return humidity;
    }

    /**
     * @param humidity The humidity value in percent to set.
     */
    public void setHumidity(float humidity) {
        this.humidity = humidity;
    }

    public float getPressure() { return pressure;}
    public void setPressure(float pressure) {this.pressure=pressure;}

    public float getPrecipitation() {return precipitation;    }
    public void setPrecipitation(float precipitation) {this.precipitation=precipitation;}

    public float getWind_speed() { return wind_speed;}
    public void setWind_speed(float wind_speed) {this.wind_speed=wind_speed;}

    public float getWind_direction() { return wind_direction;}
    public void setWind_direction(float wind_direction) {this.wind_direction=wind_direction;}

    public float getUv_index() { return uv_index; }
    public void setUv_index(float uv_index) {this.uv_index=uv_index;}

    public long getTimeSunrise() { return timeSunrise; }

    public void setTimeSunrise(long timeSunrise) {
        this.timeSunrise = timeSunrise;
    }

    public long getTimeSunset() {
        return timeSunset;
    }

    public void setTimeSunset(long timeSunset) {
        this.timeSunset = timeSunset;
    }

}
