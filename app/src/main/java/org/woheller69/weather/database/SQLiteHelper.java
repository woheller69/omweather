package org.woheller69.weather.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import static androidx.core.app.JobIntentService.enqueueWork;

/**
 * @author Karola Marky, Christopher Beckmann
 * @version 1.0
 * @since 25.01.2018
 * created 02.01.2017
 */
public class SQLiteHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private Context context;

    private List<City> allCities = new ArrayList<>();

    private static SQLiteHelper instance = null;

    private static final String DATABASE_NAME = "SQLITE.db";

    //Names of tables in the database
    private static final String TABLE_CITIES_TO_WATCH = "CITIES_TO_WATCH";
    private static final String TABLE_HOURLY_FORECAST = "FORECASTS";
    private static final String TABLE_WEEKFORECAST = "WEEKFORECASTS";
    private static final String TABLE_CURRENT_WEATHER = "CURRENT_WEATHER";


    //Names of columns in TABLE_CITIES_TO_WATCH
    private static final String CITIES_TO_WATCH_ID = "cities_to_watch_id";
    private static final String CITIES_TO_WATCH_CITY_ID = "city_id";
    private static final String CITIES_TO_WATCH_COLUMN_RANK = "rank";
    private static final String CITIES_TO_WATCH_NAME = "city_name";
    private static final String CITIES_TO_WATCH_LONGITUDE = "longitude";
    private static final String CITIES_TO_WATCH_LATITUDE = "latitude";

    //Names of columns in TABLE_FORECAST
    private static final String FORECAST_ID = "forecast_id";
    private static final String FORECAST_CITY_ID = "city_id";
    private static final String FORECAST_COLUMN_TIME_MEASUREMENT = "time_of_measurement";
    private static final String FORECAST_COLUMN_FORECAST_FOR = "forecast_for";
    private static final String FORECAST_COLUMN_WEATHER_ID = "weather_id";
    private static final String FORECAST_COLUMN_TEMPERATURE_CURRENT = "temperature_current";
    private static final String FORECAST_COLUMN_HUMIDITY = "humidity";
    private static final String FORECAST_COLUMN_PRESSURE = "pressure";
    private static final String FORECAST_COLUMN_PRECIPITATION = "precipitation";
    private static final String FORECAST_COLUMN_WIND_SPEED = "wind_speed";
    private static final String FORECAST_COLUMN_WIND_DIRECTION = "wind_direction";

    //Names of columns in TABLE_WEEKFORECAST
    private static final String WEEKFORECAST_ID = "forecast_id";
    private static final String WEEKFORECAST_CITY_ID = "city_id";
    private static final String WEEKFORECAST_COLUMN_TIME_MEASUREMENT = "time_of_measurement";
    private static final String WEEKFORECAST_COLUMN_FORECAST_FOR = "forecast_for";
    private static final String WEEKFORECAST_COLUMN_WEATHER_ID = "weather_id";
    private static final String WEEKFORECAST_COLUMN_TEMPERATURE_CURRENT = "temperature_current";
    private static final String WEEKFORECAST_COLUMN_TEMPERATURE_MIN = "temperature_min";
    private static final String WEEKFORECAST_COLUMN_TEMPERATURE_MAX = "temperature_max";
    private static final String WEEKFORECAST_COLUMN_HUMIDITY = "humidity";
    private static final String WEEKFORECAST_COLUMN_PRESSURE = "pressure";
    private static final String WEEKFORECAST_COLUMN_PRECIPITATION = "precipitation";
    private static final String WEEKFORECAST_COLUMN_WIND_SPEED = "wind_speed";
    private static final String WEEKFORECAST_COLUMN_WIND_DIRECTION = "wind_direction";
    private static final String WEEKFORECAST_COLUMN_UV_INDEX = "uv_index";
    private static final String WEEKFORECAST_COLUMN_TIME_SUNRISE = "time_sunrise";
    private static final String WEEKFORECAST_COLUMN_TIME_SUNSET = "time_sunset";


    //Names of columns in TABLE_CURRENT_WEATHER
    private static final String CURRENT_WEATHER_ID = "current_weather_id";
    private static final String CURRENT_WEATHER_CITY_ID = "city_id";
    private static final String COLUMN_TIME_MEASUREMENT = "time_of_measurement";
    private static final String COLUMN_WEATHER_ID = "weather_id";
    private static final String COLUMN_TEMPERATURE_CURRENT = "temperature_current";
    private static final String COLUMN_HUMIDITY = "humidity";
    private static final String COLUMN_PRESSURE = "pressure";
    private static final String COLUMN_WIND_SPEED = "wind_speed";
    private static final String COLUMN_WIND_DIRECTION = "wind_direction";
    private static final String COLUMN_CLOUDINESS = "cloudiness";
    private static final String COLUMN_TIME_SUNRISE = "time_sunrise";
    private static final String COLUMN_TIME_SUNSET = "time_sunset";
    private static final String COLUMN_TIMEZONE_SECONDS = "timezone_seconds";
    private static final String COLUMN_RAIN60MIN = "Rain60min";

    /**
     * Create Table statements for all tables
     */
    private static final String CREATE_CURRENT_WEATHER = "CREATE TABLE " + TABLE_CURRENT_WEATHER +
            "(" +
            CURRENT_WEATHER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            CURRENT_WEATHER_CITY_ID + " INTEGER," +
            COLUMN_TIME_MEASUREMENT + " LONG NOT NULL," +
            COLUMN_WEATHER_ID + " INTEGER," +
            COLUMN_TEMPERATURE_CURRENT + " REAL," +
            COLUMN_HUMIDITY + " REAL," +
            COLUMN_PRESSURE + " REAL," +
            COLUMN_WIND_SPEED + " REAL," +
            COLUMN_WIND_DIRECTION + " REAL," +
            COLUMN_CLOUDINESS + " REAL," +
            COLUMN_TIME_SUNRISE + "  LONG NOT NULL," +
            COLUMN_TIME_SUNSET + "  LONG NOT NULL," +
            COLUMN_TIMEZONE_SECONDS + " INTEGER," +
            COLUMN_RAIN60MIN + " VARCHAR(25) NOT NULL) ;";


    private static final String CREATE_TABLE_FORECASTS = "CREATE TABLE " + TABLE_HOURLY_FORECAST +
            "(" +
            FORECAST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            FORECAST_CITY_ID + " INTEGER," +
            FORECAST_COLUMN_TIME_MEASUREMENT + " LONG NOT NULL," +
            FORECAST_COLUMN_FORECAST_FOR + " VARCHAR(200) NOT NULL," +
            FORECAST_COLUMN_WEATHER_ID + " INTEGER," +
            FORECAST_COLUMN_TEMPERATURE_CURRENT + " REAL," +
            FORECAST_COLUMN_HUMIDITY + " REAL," +
            FORECAST_COLUMN_PRESSURE + " REAL," +
            FORECAST_COLUMN_PRECIPITATION + " REAL," +
            FORECAST_COLUMN_WIND_SPEED + " REAL," +
            FORECAST_COLUMN_WIND_DIRECTION + " REAL)";

    private static final String CREATE_TABLE_WEEKFORECASTS = "CREATE TABLE " + TABLE_WEEKFORECAST +
            "(" +
            WEEKFORECAST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            WEEKFORECAST_CITY_ID + " INTEGER," +
            WEEKFORECAST_COLUMN_TIME_MEASUREMENT + " LONG NOT NULL," +
            WEEKFORECAST_COLUMN_FORECAST_FOR + " VARCHAR(200) NOT NULL," +
            WEEKFORECAST_COLUMN_WEATHER_ID + " INTEGER," +
            WEEKFORECAST_COLUMN_TEMPERATURE_CURRENT + " REAL," +
            WEEKFORECAST_COLUMN_TEMPERATURE_MIN + " REAL," +
            WEEKFORECAST_COLUMN_TEMPERATURE_MAX + " REAL," +
            WEEKFORECAST_COLUMN_HUMIDITY + " REAL," +
            WEEKFORECAST_COLUMN_PRESSURE + " REAL," +
            WEEKFORECAST_COLUMN_PRECIPITATION + " REAL," +
            WEEKFORECAST_COLUMN_WIND_SPEED + " REAL," +
            WEEKFORECAST_COLUMN_WIND_DIRECTION + " REAL," +
            WEEKFORECAST_COLUMN_UV_INDEX + " REAL," +
            WEEKFORECAST_COLUMN_TIME_SUNRISE + "  LONG NOT NULL," +
            WEEKFORECAST_COLUMN_TIME_SUNSET + "  LONG NOT NULL)";

    private static final String CREATE_TABLE_CITIES_TO_WATCH = "CREATE TABLE " + TABLE_CITIES_TO_WATCH +
            "(" +
            CITIES_TO_WATCH_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            CITIES_TO_WATCH_CITY_ID + " INTEGER," +
            CITIES_TO_WATCH_COLUMN_RANK + " INTEGER," +
            CITIES_TO_WATCH_NAME + " VARCHAR(100) NOT NULL," +
            CITIES_TO_WATCH_LONGITUDE + " REAL NOT NULL," +
            CITIES_TO_WATCH_LATITUDE + " REAL NOT NULL ); ";

    public static SQLiteHelper getInstance(Context context) {
        if (instance == null && context != null) {
            instance = new SQLiteHelper(context.getApplicationContext());
        }
        return instance;
    }

    private SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context.getApplicationContext();
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_CITIES_TO_WATCH);
        db.execSQL(CREATE_CURRENT_WEATHER);
        db.execSQL(CREATE_TABLE_FORECASTS);
        db.execSQL(CREATE_TABLE_WEEKFORECASTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }



    /**
     * Methods for TABLE_CITIES_TO_WATCH
     */
    public synchronized long addCityToWatch(CityToWatch city) {
        SQLiteDatabase database = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(CITIES_TO_WATCH_CITY_ID, city.getCityId());
        values.put(CITIES_TO_WATCH_COLUMN_RANK, city.getRank());
        values.put(CITIES_TO_WATCH_NAME,city.getCityName());
        values.put(CITIES_TO_WATCH_LATITUDE,city.getLatitude());
        values.put(CITIES_TO_WATCH_LONGITUDE,city.getLongitude());

        long id=database.insert(TABLE_CITIES_TO_WATCH, null, values);

        //use id also instead of city id as unique identifier
        values.put(CITIES_TO_WATCH_CITY_ID,id);
        database.update(TABLE_CITIES_TO_WATCH, values, CITIES_TO_WATCH_ID + " = ?",
                new String[]{String.valueOf(id)});

        database.close();
        return id;
    }

    public synchronized CityToWatch getCityToWatch(int id) {
        SQLiteDatabase database = this.getWritableDatabase();

        String[] arguments = {String.valueOf(id)};

        Cursor cursor = database.rawQuery(
                "SELECT " + CITIES_TO_WATCH_ID +
                        ", " + CITIES_TO_WATCH_CITY_ID +
                        ", " + CITIES_TO_WATCH_NAME +
                        ", " + CITIES_TO_WATCH_LONGITUDE +
                        ", " + CITIES_TO_WATCH_LATITUDE +
                        ", " + CITIES_TO_WATCH_COLUMN_RANK +
                        " FROM " + TABLE_CITIES_TO_WATCH +
                        " WHERE " + CITIES_TO_WATCH_CITY_ID + " = ?", arguments);

        CityToWatch cityToWatch = new CityToWatch();

        if (cursor != null && cursor.moveToFirst()) {
            cityToWatch.setId(Integer.parseInt(cursor.getString(0)));
            cityToWatch.setCityId(Integer.parseInt(cursor.getString(1)));
            cityToWatch.setCityName(cursor.getString(2));
            cityToWatch.setLongitude(Float.parseFloat(cursor.getString(3)));
            cityToWatch.setLatitude(Float.parseFloat(cursor.getString(4)));
            cityToWatch.setRank(Integer.parseInt(cursor.getString(5)));

            cursor.close();
        }
        database.close();
        return cityToWatch;

    }


    public synchronized List<CityToWatch> getAllCitiesToWatch() {
        List<CityToWatch> cityToWatchList = new ArrayList<>();

        SQLiteDatabase database = this.getWritableDatabase();

        Cursor cursor = database.rawQuery(
                "SELECT " + CITIES_TO_WATCH_ID +
                        ", " + CITIES_TO_WATCH_CITY_ID +
                        ", " + CITIES_TO_WATCH_NAME +
                        ", " + CITIES_TO_WATCH_LONGITUDE +
                        ", " + CITIES_TO_WATCH_LATITUDE +
                        ", " + CITIES_TO_WATCH_COLUMN_RANK +
                        " FROM " + TABLE_CITIES_TO_WATCH
                , new String[]{});

        CityToWatch cityToWatch;

        if (cursor.moveToFirst()) {
            do {
                cityToWatch = new CityToWatch();
                cityToWatch.setId(Integer.parseInt(cursor.getString(0)));
                cityToWatch.setCityId(Integer.parseInt(cursor.getString(1)));
                cityToWatch.setCityName(cursor.getString(2));
                cityToWatch.setLongitude(Float.parseFloat(cursor.getString(3)));
                cityToWatch.setLatitude(Float.parseFloat(cursor.getString(4)));
                cityToWatch.setRank(Integer.parseInt(cursor.getString(5)));

                cityToWatchList.add(cityToWatch);
            } while (cursor.moveToNext());
        }

        cursor.close();
        database.close();
        return cityToWatchList;
    }

    public synchronized void updateCityToWatch(CityToWatch cityToWatch) {
        SQLiteDatabase database = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(CITIES_TO_WATCH_CITY_ID, cityToWatch.getCityId());
        values.put(CITIES_TO_WATCH_COLUMN_RANK, cityToWatch.getRank());
        values.put(CITIES_TO_WATCH_NAME,cityToWatch.getCityName());
        values.put(CITIES_TO_WATCH_LATITUDE,cityToWatch.getLatitude());
        values.put(CITIES_TO_WATCH_LONGITUDE,cityToWatch.getLongitude());

        database.update(TABLE_CITIES_TO_WATCH, values, CITIES_TO_WATCH_ID + " = ?",
                new String[]{String.valueOf(cityToWatch.getId())});
        database.close();
    }

    public synchronized void deleteCityToWatch(CityToWatch cityToWatch) {

        //First delete all weather data for city which is deleted
        deleteCurrentWeatherByCityId(cityToWatch.getCityId());
        deleteForecastsByCityId(cityToWatch.getCityId());
        deleteWeekForecastsByCityId(cityToWatch.getCityId());

        //Now remove city from CITIES_TO_WATCH
        SQLiteDatabase database = this.getWritableDatabase();
        database.delete(TABLE_CITIES_TO_WATCH, CITIES_TO_WATCH_ID + " = ?",
                new String[]{Integer.toString(cityToWatch.getId())});
        database.close();
    }

    public synchronized int getWatchedCitiesCount() {
        SQLiteDatabase database = this.getWritableDatabase();
        long count = DatabaseUtils.queryNumEntries(database, TABLE_CITIES_TO_WATCH);
        database.close();
        return (int) count;
    }

    public int getMaxRank() {
        List<CityToWatch> cities = getAllCitiesToWatch();
        int maxRank = 0;
        for (CityToWatch ctw : cities) {
            if (ctw.getRank() > maxRank) maxRank = ctw.getRank();
        }
        return maxRank;
    }


    /**
     * Methods for TABLE_FORECAST
     */
    public synchronized void addForecasts(List<HourlyForecast> hourlyForecasts) {
        SQLiteDatabase database = this.getWritableDatabase();
        for (HourlyForecast hourlyForecast: hourlyForecasts) {
            ContentValues values = new ContentValues();
            values.put(FORECAST_CITY_ID, hourlyForecast.getCity_id());
            values.put(FORECAST_COLUMN_TIME_MEASUREMENT, hourlyForecast.getTimestamp());
            values.put(FORECAST_COLUMN_FORECAST_FOR, hourlyForecast.getForecastTime());
            values.put(FORECAST_COLUMN_WEATHER_ID, hourlyForecast.getWeatherID());
            values.put(FORECAST_COLUMN_TEMPERATURE_CURRENT, hourlyForecast.getTemperature());
            values.put(FORECAST_COLUMN_HUMIDITY, hourlyForecast.getHumidity());
            values.put(FORECAST_COLUMN_PRESSURE, hourlyForecast.getPressure());
            values.put(FORECAST_COLUMN_PRECIPITATION, hourlyForecast.getPrecipitation());
            values.put(FORECAST_COLUMN_WIND_SPEED, hourlyForecast.getWindSpeed());
            values.put(FORECAST_COLUMN_WIND_DIRECTION, hourlyForecast.getWindDirection());
            database.insert(TABLE_HOURLY_FORECAST, null, values);
        }
        database.close();
    }

    public synchronized void deleteForecastsByCityId(int cityId) {
        SQLiteDatabase database = this.getWritableDatabase();
        database.delete(TABLE_HOURLY_FORECAST, FORECAST_CITY_ID + " = ?",
                new String[]{Integer.toString(cityId)});
        database.close();
    }


    public synchronized List<HourlyForecast> getForecastsByCityId(int cityId) {
        SQLiteDatabase database = this.getWritableDatabase();

        Cursor cursor = database.query(TABLE_HOURLY_FORECAST,
                new String[]{FORECAST_ID,
                        FORECAST_CITY_ID,
                        FORECAST_COLUMN_TIME_MEASUREMENT,
                        FORECAST_COLUMN_FORECAST_FOR,
                        FORECAST_COLUMN_WEATHER_ID,
                        FORECAST_COLUMN_TEMPERATURE_CURRENT,
                        FORECAST_COLUMN_HUMIDITY,
                        FORECAST_COLUMN_PRESSURE,
                        FORECAST_COLUMN_PRECIPITATION,
                        FORECAST_COLUMN_WIND_SPEED,
                        FORECAST_COLUMN_WIND_DIRECTION}
                , FORECAST_CITY_ID + "=?",
                new String[]{String.valueOf(cityId)}, null, null, null, null);

        List<HourlyForecast> list = new ArrayList<>();
        HourlyForecast hourlyForecast;

        if (cursor != null && cursor.moveToFirst()) {
            do {
                hourlyForecast = new HourlyForecast();
                hourlyForecast.setId(Integer.parseInt(cursor.getString(0)));
                hourlyForecast.setCity_id(Integer.parseInt(cursor.getString(1)));
                hourlyForecast.setTimestamp(Long.parseLong(cursor.getString(2)));
                hourlyForecast.setForecastTime(Long.parseLong(cursor.getString(3)));
                hourlyForecast.setWeatherID(Integer.parseInt(cursor.getString(4)));
                hourlyForecast.setTemperature(Float.parseFloat(cursor.getString(5)));
                hourlyForecast.setHumidity(Float.parseFloat(cursor.getString(6)));
                hourlyForecast.setPressure(Float.parseFloat(cursor.getString(7)));
                hourlyForecast.setPrecipitation(Float.parseFloat(cursor.getString(8)));
                hourlyForecast.setWindSpeed(Float.parseFloat(cursor.getString(9)));
                hourlyForecast.setWindDirection(Float.parseFloat(cursor.getString(10)));
                list.add(hourlyForecast);
            } while (cursor.moveToNext());

            cursor.close();
        }

        return list;
    }


    /**
     * Methods for TABLE_WEEKFORECAST
     */
    public synchronized void addWeekForecasts(List<WeekForecast> weekForecasts) {
        SQLiteDatabase database = this.getWritableDatabase();
        for (WeekForecast weekForecast: weekForecasts) {
            ContentValues values = new ContentValues();
            values.put(WEEKFORECAST_CITY_ID, weekForecast.getCity_id());
            values.put(WEEKFORECAST_COLUMN_TIME_MEASUREMENT, weekForecast.getTimestamp());
            values.put(WEEKFORECAST_COLUMN_FORECAST_FOR, weekForecast.getForecastTime());
            values.put(WEEKFORECAST_COLUMN_WEATHER_ID, weekForecast.getWeatherID());
            values.put(WEEKFORECAST_COLUMN_TEMPERATURE_CURRENT, weekForecast.getTemperature());
            values.put(WEEKFORECAST_COLUMN_TEMPERATURE_MIN, weekForecast.getMinTemperature());
            values.put(WEEKFORECAST_COLUMN_TEMPERATURE_MAX, weekForecast.getMaxTemperature());
            values.put(WEEKFORECAST_COLUMN_HUMIDITY, weekForecast.getHumidity());
            values.put(WEEKFORECAST_COLUMN_PRESSURE, weekForecast.getPressure());
            values.put(WEEKFORECAST_COLUMN_PRECIPITATION, weekForecast.getPrecipitation());
            values.put(WEEKFORECAST_COLUMN_WIND_SPEED, weekForecast.getWind_speed());
            values.put(WEEKFORECAST_COLUMN_WIND_DIRECTION, weekForecast.getWind_direction());
            values.put(WEEKFORECAST_COLUMN_UV_INDEX, weekForecast.getUv_index());
            values.put(WEEKFORECAST_COLUMN_TIME_SUNRISE, weekForecast.getTimeSunrise());
            values.put(WEEKFORECAST_COLUMN_TIME_SUNSET, weekForecast.getTimeSunset());
            database.insert(TABLE_WEEKFORECAST, null, values);
        }
        database.close();
    }

    public synchronized void deleteWeekForecastsByCityId(int cityId) {
        SQLiteDatabase database = this.getWritableDatabase();
        database.delete(TABLE_WEEKFORECAST, WEEKFORECAST_CITY_ID + " = ?",
                new String[]{Integer.toString(cityId)});
        database.close();
    }




    public synchronized List<WeekForecast> getWeekForecastsByCityId(int cityId) {
        SQLiteDatabase database = this.getWritableDatabase();

        Cursor cursor = database.query(TABLE_WEEKFORECAST,
                new String[]{WEEKFORECAST_ID,
                        WEEKFORECAST_CITY_ID,
                        WEEKFORECAST_COLUMN_TIME_MEASUREMENT,
                        WEEKFORECAST_COLUMN_FORECAST_FOR,
                        WEEKFORECAST_COLUMN_WEATHER_ID,
                        WEEKFORECAST_COLUMN_TEMPERATURE_CURRENT,
                        WEEKFORECAST_COLUMN_TEMPERATURE_MIN,
                        WEEKFORECAST_COLUMN_TEMPERATURE_MAX,
                        WEEKFORECAST_COLUMN_HUMIDITY,
                        WEEKFORECAST_COLUMN_PRESSURE,
                        WEEKFORECAST_COLUMN_PRECIPITATION,
                        WEEKFORECAST_COLUMN_WIND_SPEED,
                        WEEKFORECAST_COLUMN_WIND_DIRECTION,
                        WEEKFORECAST_COLUMN_UV_INDEX,
                        WEEKFORECAST_COLUMN_TIME_SUNRISE,
                        WEEKFORECAST_COLUMN_TIME_SUNSET}
                , WEEKFORECAST_CITY_ID + "=?",
                new String[]{String.valueOf(cityId)}, null, null, null, null);

        List<WeekForecast> list = new ArrayList<>();
        WeekForecast weekForecast;

        if (cursor != null && cursor.moveToFirst()) {
            do {
                weekForecast = new WeekForecast();
                weekForecast.setId(Integer.parseInt(cursor.getString(0)));
                weekForecast.setCity_id(Integer.parseInt(cursor.getString(1)));
                weekForecast.setTimestamp(Long.parseLong(cursor.getString(2)));
                weekForecast.setForecastTime(Long.parseLong(cursor.getString(3)));
                weekForecast.setWeatherID(Integer.parseInt(cursor.getString(4)));
                weekForecast.setTemperature(Float.parseFloat(cursor.getString(5)));
                weekForecast.setMinTemperature(Float.parseFloat(cursor.getString(6)));
                weekForecast.setMaxTemperature(Float.parseFloat(cursor.getString(7)));
                weekForecast.setHumidity(Float.parseFloat(cursor.getString(8)));
                weekForecast.setPressure(Float.parseFloat(cursor.getString(9)));
                weekForecast.setPrecipitation(Float.parseFloat(cursor.getString(10)));
                weekForecast.setWind_speed(Float.parseFloat(cursor.getString(11)));
                weekForecast.setWind_direction(Float.parseFloat(cursor.getString(12)));
                weekForecast.setUv_index(Float.parseFloat(cursor.getString(13)));
                weekForecast.setTimeSunrise(Long.parseLong(cursor.getString(14)));
                weekForecast.setTimeSunset(Long.parseLong(cursor.getString(15)));
                list.add(weekForecast);
            } while (cursor.moveToNext());

            cursor.close();
        }

        return list;
    }

      /**
     * Methods for TABLE_CURRENT_WEATHER
     */
    public synchronized void addCurrentWeather(CurrentWeatherData currentWeather) {
        SQLiteDatabase database = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(CURRENT_WEATHER_CITY_ID, currentWeather.getCity_id());
        values.put(COLUMN_TIME_MEASUREMENT, currentWeather.getTimestamp());
        values.put(COLUMN_WEATHER_ID, currentWeather.getWeatherID());
        values.put(COLUMN_TEMPERATURE_CURRENT, currentWeather.getTemperatureCurrent());
        values.put(COLUMN_HUMIDITY, currentWeather.getHumidity());
        values.put(COLUMN_PRESSURE, currentWeather.getPressure());
        values.put(COLUMN_WIND_SPEED, currentWeather.getWindSpeed());
        values.put(COLUMN_WIND_DIRECTION, currentWeather.getWindDirection());
        values.put(COLUMN_CLOUDINESS, currentWeather.getCloudiness());
        values.put(COLUMN_TIME_SUNRISE, currentWeather.getTimeSunrise());
        values.put(COLUMN_TIME_SUNSET, currentWeather.getTimeSunset());
        values.put(COLUMN_TIMEZONE_SECONDS, currentWeather.getTimeZoneSeconds());
        values.put(COLUMN_RAIN60MIN, currentWeather.getRain60min());


        database.insert(TABLE_CURRENT_WEATHER, null, values);
        database.close();
    }



    public synchronized CurrentWeatherData getCurrentWeatherByCityId(int cityId) {
        SQLiteDatabase database = this.getReadableDatabase();

        Cursor cursor = database.query(TABLE_CURRENT_WEATHER,
                new String[]{CURRENT_WEATHER_ID,
                        CURRENT_WEATHER_CITY_ID,
                        COLUMN_TIME_MEASUREMENT,
                        COLUMN_WEATHER_ID,
                        COLUMN_TEMPERATURE_CURRENT,
                        COLUMN_HUMIDITY,
                        COLUMN_PRESSURE,
                        COLUMN_WIND_SPEED,
                        COLUMN_WIND_DIRECTION,
                        COLUMN_CLOUDINESS,
                        COLUMN_TIME_SUNRISE,
                        COLUMN_TIME_SUNSET,
                        COLUMN_TIMEZONE_SECONDS,
                        COLUMN_RAIN60MIN},
                CURRENT_WEATHER_CITY_ID + " = ?",
                new String[]{String.valueOf(cityId)}, null, null, null, null);

        CurrentWeatherData currentWeather = new CurrentWeatherData();

        if (cursor != null && cursor.moveToFirst()) {
            currentWeather.setId(Integer.parseInt(cursor.getString(0)));
            currentWeather.setCity_id(Integer.parseInt(cursor.getString(1)));
            currentWeather.setTimestamp(Long.parseLong(cursor.getString(2)));
            currentWeather.setWeatherID(Integer.parseInt(cursor.getString(3)));
            currentWeather.setTemperatureCurrent(Float.parseFloat(cursor.getString(4)));
            currentWeather.setHumidity(Float.parseFloat(cursor.getString(5)));
            currentWeather.setPressure(Float.parseFloat(cursor.getString(6)));
            currentWeather.setWindSpeed(Float.parseFloat(cursor.getString(7)));
            currentWeather.setWindDirection(Float.parseFloat(cursor.getString(8)));
            currentWeather.setCloudiness(Float.parseFloat(cursor.getString(9)));
            currentWeather.setTimeSunrise(Long.parseLong(cursor.getString(10)));
            currentWeather.setTimeSunset(Long.parseLong(cursor.getString(11)));
            currentWeather.setTimeZoneSeconds(Integer.parseInt(cursor.getString(12)));
            currentWeather.setRain60min(cursor.getString(13));

            cursor.close();
        }

        return currentWeather;
    }

    public synchronized void updateCurrentWeather(CurrentWeatherData currentWeather) {
        SQLiteDatabase database = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(CURRENT_WEATHER_CITY_ID, currentWeather.getCity_id());
        values.put(COLUMN_TIME_MEASUREMENT, currentWeather.getTimestamp());
        values.put(COLUMN_WEATHER_ID, currentWeather.getWeatherID());
        values.put(COLUMN_TEMPERATURE_CURRENT, currentWeather.getTemperatureCurrent());
        values.put(COLUMN_HUMIDITY, currentWeather.getHumidity());
        values.put(COLUMN_PRESSURE, currentWeather.getPressure());
        values.put(COLUMN_WIND_SPEED, currentWeather.getWindSpeed());
        values.put(COLUMN_WIND_DIRECTION, currentWeather.getWindDirection());
        values.put(COLUMN_CLOUDINESS, currentWeather.getCloudiness());
        values.put(COLUMN_TIME_SUNRISE, currentWeather.getTimeSunrise());
        values.put(COLUMN_TIME_SUNSET, currentWeather.getTimeSunset());
        values.put(COLUMN_TIMEZONE_SECONDS, currentWeather.getTimeZoneSeconds());
        values.put(COLUMN_RAIN60MIN, currentWeather.getRain60min());

        database.update(TABLE_CURRENT_WEATHER, values, CURRENT_WEATHER_CITY_ID + " = ?",
                new String[]{String.valueOf(currentWeather.getCity_id())});
    }

    public synchronized void deleteCurrentWeather(CurrentWeatherData currentWeather) {
        SQLiteDatabase database = this.getWritableDatabase();
        database.delete(TABLE_CURRENT_WEATHER, CURRENT_WEATHER_ID + " = ?",
                new String[]{Integer.toString(currentWeather.getId())});
        database.close();
    }

    public synchronized void deleteCurrentWeatherByCityId(int cityId) {
        SQLiteDatabase database = this.getWritableDatabase();
        database.delete(TABLE_CURRENT_WEATHER, CURRENT_WEATHER_CITY_ID + " = ?",
                new String[]{Integer.toString(cityId)});
        database.close();
    }

    public static int getWidgetCityID(Context context) {
        SQLiteHelper db = SQLiteHelper.getInstance(context);
        int cityID=0;
        List<CityToWatch> cities = db.getAllCitiesToWatch();
        int rank=cities.get(0).getRank();
        for (int i = 0; i < cities.size(); i++) {   //find cityID for first city to watch = lowest Rank
            CityToWatch city = cities.get(i);
            //Log.d("debugtag",Integer.toString(city.getRank()));
            if (city.getRank() <= rank ){
                rank=city.getRank();
                cityID = city.getCityId();
            }
        }
        return cityID;
    }
    public synchronized void deleteAllForecasts() {
        SQLiteDatabase database = this.getWritableDatabase();
        database.execSQL("delete from " + TABLE_HOURLY_FORECAST);
        database.execSQL("delete from " + TABLE_WEEKFORECAST);
        database.execSQL("delete from " + TABLE_CURRENT_WEATHER);
        database.close();
    }
}
