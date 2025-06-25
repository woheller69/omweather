package org.woheller69.weather.database;

import android.content.Context;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * This class represents the database model for current weather data of cities.
 */

public class CurrentWeatherData {

    private int id;
    private int city_id;
    private long timestamp;
    private int weatherID;
    private float temperatureCurrent;
    private float humidity;
    private float pressure;
    private float windSpeed;
    private float windDirection;
    private float cloudiness;
    private long timeSunrise;
    private long timeSunset;
    private int timeZoneSeconds;
    private String Rain60min;

    private String city_name;

    public CurrentWeatherData() {
        this.city_id = Integer.MIN_VALUE;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCity_id() {
        return city_id;
    }

    public void setCity_id(int city_id) {
        this.city_id = city_id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getWeatherID() {
        return weatherID;
    }

    public void setWeatherID(int weatherID) {
        this.weatherID = weatherID;
    }

    public float getTemperatureCurrent() {
        return temperatureCurrent;
    }

    public void setTemperatureCurrent(float temperatureCurrent) {
        this.temperatureCurrent = temperatureCurrent;
    }

    public float getHumidity() {
        return humidity;
    }

    public void setHumidity(float humidity) {
        this.humidity = humidity;
    }

    public float getPressure() {
        return pressure;
    }

    public void setPressure(float pressure) {
        this.pressure = pressure;
    }

    public float getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(float windSpeed) {
        this.windSpeed = windSpeed;
    }

    public float getWindDirection() {
        return windDirection;
    }

    public void setWindDirection(float windDirection) {
        this.windDirection = windDirection;
    }

    public float getCloudiness() {
        return cloudiness;
    }

    public void setCloudiness(float cloudiness) {
        this.cloudiness = cloudiness;
    }

    public boolean isDay(Context context){
        Calendar currentTime = Calendar.getInstance();
        currentTime.setTimeZone(TimeZone.getTimeZone("GMT"));
        currentTime.setTimeInMillis(System.currentTimeMillis() + timeZoneSeconds* 1000L);
        SQLiteHelper dbHelper = SQLiteHelper.getInstance(context);
        if ((timeSunrise - timeSunset) % 86400 == 0){
            if ((dbHelper.getCityToWatch(city_id).getLatitude())>0){  //northern hemisphere
                return currentTime.get(Calendar.DAY_OF_YEAR) >= 80 && currentTime.get(Calendar.DAY_OF_YEAR) <= 265;  //from March 21 to September 22 (incl)
            }else{ //southern hemisphere
                return currentTime.get(Calendar.DAY_OF_YEAR) < 80 || currentTime.get(Calendar.DAY_OF_YEAR) > 265;
            }
        } else {
            Calendar sunSetTime = Calendar.getInstance();
            sunSetTime.setTimeZone(TimeZone.getTimeZone("GMT"));
            sunSetTime.setTimeInMillis(timeSunset * 1000 + timeZoneSeconds * 1000L);
            sunSetTime.set(Calendar.DAY_OF_YEAR, currentTime.get(Calendar.DAY_OF_YEAR));
            sunSetTime.set(Calendar.YEAR, currentTime.get(Calendar.YEAR));

            Calendar sunRiseTime = Calendar.getInstance();
            sunRiseTime.setTimeZone(TimeZone.getTimeZone("GMT"));
            sunRiseTime.setTimeInMillis(timeSunrise * 1000 + timeZoneSeconds * 1000L);
            sunRiseTime.set(Calendar.DAY_OF_YEAR, currentTime.get(Calendar.DAY_OF_YEAR));
            sunRiseTime.set(Calendar.YEAR, currentTime.get(Calendar.YEAR));

            return currentTime.after(sunRiseTime) && currentTime.before(sunSetTime);
        }
    }

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

    public String getCity_name() {
        return city_name;
    }

    public void setCity_name(String city_name) {
        this.city_name = city_name;
    }

    public int getTimeZoneSeconds() {
        return timeZoneSeconds;
    }

    public void setTimeZoneSeconds(int timeZoneSeconds) {
        this.timeZoneSeconds = timeZoneSeconds;
    }

    public String getRain60min() {
        return Rain60min;
    }

    public void setRain60min(String Rain60min) {
        this.Rain60min = Rain60min;
    }
}
