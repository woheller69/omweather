package org.woheller69.weather.database;

/**
 * This class is the database model for the cities to watch. 'Cities to watch' means the locations
 * for which a user would like to see the weather for. This includes those locations that will be
 * deleted after app close (non-persistent locations).
 */
public class CityToWatch {

    private int id;
    private int cityId;
    private String cityName;
    private float lon;
    private float lat;
    private int rank;

    public CityToWatch() {
    }

    public CityToWatch(int rank, String countryCode, int id, int cityId, float lon, float lat, String cityName) {
        this.rank = rank;
        this.lon = lon;
        this.lat = lat;
        this.id = id;
        this.cityId = cityId;
        this.cityName = cityName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public void setLongitude(float lon) { this.lon = lon; }

    public float getLongitude() {  return lon; }

    public float getLatitude() {  return lat; }

    public void setLatitude(float lat) { this.lat = lat; }
}