package org.woheller69.weather.weather_api.open_meteo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.woheller69.weather.database.CurrentWeatherData;
import org.woheller69.weather.database.HourlyForecast;
import org.woheller69.weather.database.WeekForecast;
import org.woheller69.weather.weather_api.IApiToDatabaseConversion;
import org.woheller69.weather.weather_api.IDataExtractor;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a concrete implementation for extracting weather data that was retrieved by
 * Open-Meteo.
 */
public class OMDataExtractor implements IDataExtractor {


    @Override
    public CurrentWeatherData extractCurrentWeather(String data) {
        try {
            JSONObject jsonData = new JSONObject(data);
            CurrentWeatherData weatherData = new CurrentWeatherData();
            weatherData.setTimestamp(System.currentTimeMillis() / 1000);
            IApiToDatabaseConversion conversion = new OMToDatabaseConversion();
            if (jsonData.has("weathercode")) weatherData.setWeatherID(conversion.convertWeatherCategory(jsonData.getString("weathercode")));
            if (jsonData.has("temperature")) weatherData.setTemperatureCurrent((float) jsonData.getDouble("temperature"));
            if (jsonData.has("windspeed")) weatherData.setWindSpeed((float) jsonData.getDouble("windspeed"));
            if (jsonData.has("winddirection")) weatherData.setWindDirection((float) jsonData.getDouble("winddirection"));
            weatherData.setTimeSunrise(0L);
            weatherData.setTimeSunset(0L);
            weatherData.setHumidity(0);
            weatherData.setPressure(0);
            weatherData.setCloudiness(0);

            return weatherData;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public List<WeekForecast> extractWeekForecast(String data) {
        try {
            List<WeekForecast> weekforecasts = new ArrayList<>();
            JSONObject jsonData = new JSONObject(data);
            JSONArray timeArray = jsonData.getJSONArray("time");
            JSONArray weathercodeArray = jsonData.getJSONArray("weathercode");
            JSONArray tempMaxArray = jsonData.getJSONArray("temperature_2m_max");
            JSONArray tempMinArray = jsonData.getJSONArray("temperature_2m_min");
            JSONArray sunriseArray = jsonData.getJSONArray("sunrise");
            JSONArray sunsetArray = jsonData.getJSONArray("sunset");
            JSONArray uvIndexArray = jsonData.getJSONArray("uv_index_max");
            JSONArray precipitationArray = jsonData.getJSONArray("precipitation_sum");
            JSONArray windSpeedArray = jsonData.getJSONArray("windspeed_10m_max");


            for (int i = 0; i < timeArray.length(); i++) {
                WeekForecast weekForecast = new WeekForecast();
                weekForecast.setTimestamp(System.currentTimeMillis() / 1000);
                if (!timeArray.isNull(i))weekForecast.setForecastTime((timeArray.getLong(i)+12*3600)*1000L);  //shift to midday
                if (!weathercodeArray.isNull(i))weekForecast.setWeatherID(weathercodeArray.getInt(i));
                if (!tempMaxArray.isNull(i))weekForecast.setMaxTemperature((float) tempMaxArray.getDouble(i));
                if (!tempMinArray.isNull(i))weekForecast.setMinTemperature((float) tempMinArray.getDouble(i));
                if (!sunriseArray.isNull(i))weekForecast.setTimeSunrise(sunriseArray.getLong(i));
                if (!sunsetArray.isNull(i))weekForecast.setTimeSunset(sunsetArray.getLong(i));
                if (!uvIndexArray.isNull(i)) {
                    weekForecast.setUv_index((float) uvIndexArray.getDouble(i));
                } else weekForecast.setUv_index(-1);
                if (!precipitationArray.isNull(i))weekForecast.setPrecipitation((float) precipitationArray.getDouble(i));
                if (!windSpeedArray.isNull(i))weekForecast.setWind_speed((float) windSpeedArray.getDouble(i));
                weekforecasts.add(weekForecast);
            }
            return weekforecasts;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @see IDataExtractor#extractHourlyForecast(String)
     */
    @Override
    public List<HourlyForecast> extractHourlyForecast(String data) {
        try {
            List<HourlyForecast> hourlyForecasts = new ArrayList<>();
            JSONObject jsonData = new JSONObject(data);
            JSONArray timeArray = jsonData.getJSONArray("time");
            JSONArray weathercodeArray = jsonData.getJSONArray("weathercode");
            JSONArray tempArray = jsonData.getJSONArray("temperature_2m");
            JSONArray rhArray = jsonData.getJSONArray("relativehumidity_2m");
            JSONArray pressureArray = jsonData.getJSONArray("pressure_msl");
            JSONArray precipitationArray = jsonData.getJSONArray("precipitation");
            JSONArray windSpeedArray = jsonData.getJSONArray("windspeed_10m");
            JSONArray windDirArray = jsonData.getJSONArray("winddirection_10m");


            for (int i = 0; i < timeArray.length(); i++) {
                HourlyForecast hourlyForecast = new HourlyForecast();
                hourlyForecast.setTimestamp(System.currentTimeMillis() / 1000);
                if (!timeArray.isNull(i)) hourlyForecast.setForecastTime(timeArray.getLong(i)*1000L);
                if (!weathercodeArray.isNull(i)) hourlyForecast.setWeatherID(weathercodeArray.getInt(i));
                if (!tempArray.isNull(i)) hourlyForecast.setTemperature((float) tempArray.getDouble(i));
                if (!rhArray.isNull(i)) hourlyForecast.setHumidity((float) rhArray.getDouble(i));
                if (!pressureArray.isNull(i)) hourlyForecast.setPressure((float) pressureArray.getDouble(i));
                if (!precipitationArray.isNull(i)) hourlyForecast.setPrecipitation((float) precipitationArray.getDouble(i));
                if (!windSpeedArray.isNull(i)) hourlyForecast.setWindSpeed((float) windSpeedArray.getDouble(i));
                if (!windDirArray.isNull(i)) hourlyForecast.setWindDirection((float) windDirArray.getDouble(i));
                hourlyForecasts.add(hourlyForecast);
            }
            return hourlyForecasts;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
    /**
     * @see IDataExtractor#extractHourlyForecast(String)
     */
 /*   @Override
    public HourlyForecast extractHourlyForecast(String data) {
        try {

            HourlyForecast hourlyForecast = new HourlyForecast();
            JSONObject jsonData = new JSONObject(data);

            hourlyForecast.setTimestamp(System.currentTimeMillis() / 1000);
            hourlyForecast.setForecastTime(jsonData.getLong("dt") * 1000L);

            IApiToDatabaseConversion conversion = new OMToDatabaseConversion();

            JSONArray jsonWeatherArray = jsonData.getJSONArray("weather");
            JSONObject jsonWeather = new JSONObject(jsonWeatherArray.get(0).toString());
            hourlyForecast.setWeatherID(conversion.convertWeatherCategory(jsonWeather.getString("id")));

            if (jsonData.has("temp")) hourlyForecast.setTemperature((float) jsonData.getDouble("temp"));
            if (jsonData.has("humidity")) hourlyForecast.setHumidity((float) jsonData.getDouble("humidity"));
            if (jsonData.has("pressure")) hourlyForecast.setPressure((float) jsonData.getDouble("pressure"));
            if (jsonData.has("wind_speed")) hourlyForecast.setWindSpeed((float) jsonData.getDouble("wind_speed"));
            if (jsonData.has("wind_deg")) hourlyForecast.setWindDirection((float) jsonData.getDouble("wind_deg"));

            // In case there was no rain in the past 3 hours, there is no "rain" field
            if (jsonData.isNull("rain")) {
                hourlyForecast.setPrecipitation(HourlyForecast.NO_RAIN_VALUE);
            } else {
                JSONObject jsonRain = jsonData.getJSONObject("rain");
                if (jsonRain.isNull("1h")) {
                    hourlyForecast.setPrecipitation(HourlyForecast.NO_RAIN_VALUE);
                } else {
                    hourlyForecast.setPrecipitation((float) jsonRain.getDouble("1h"));
                }
            }
            //add snow precipitation to rain
            if (!jsonData.isNull("snow")) {
                JSONObject jsonSnow = jsonData.getJSONObject("snow");
                if (!jsonSnow.isNull("1h")) {
                    hourlyForecast.setPrecipitation(hourlyForecast.getPrecipitation() + (float) jsonSnow.getDouble("1h"));
                }
            }

            return hourlyForecast;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }*/



    /**
     * @see IDataExtractor#extractRain60min(String, String, String, String, String)
     */
    @Override
    public String extractRain60min(String data0,String data1, String data2, String data3, String data4) {
        try {

            String rain = "";
            JSONObject jsonData0 = new JSONObject(data0);
            JSONObject jsonData1 = new JSONObject(data1);
            JSONObject jsonData2 = new JSONObject(data2);
            JSONObject jsonData3 = new JSONObject(data3);
            JSONObject jsonData4 = new JSONObject(data4);
            double rain5min=jsonData0.getDouble("precipitation")+jsonData1.getDouble("precipitation")+jsonData2.getDouble("precipitation")+jsonData3.getDouble("precipitation")+jsonData4.getDouble("precipitation");
            if (rain5min==0){
                rain ="\u25a1";
            } else if (rain5min<2.5){  // very light rain equals <0.5mm/h (2.5 = 5 x 0.5)
                rain ="\u25a4";
            }else if (rain5min<12.5){  //light rain equals <2.5mm/h (12.5 = 5 x 2.5)
                rain ="\u25a6";
            } else{
                rain ="\u25a0";
            }

            return rain;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * @param data The data that contains the information to retrieve the ID of the city.
     *             If data for a single city were requested, the response string can be
     *             passed as an argument.
     *             If data for multiple cities were requested, make sure to pass only one item
     *             of the response list at a time!
     * @return Returns the ID of the city or Integer#MIN_VALUE in case the data is not well-formed
     * and the information could not be extracted.
     */


    /**
     * @see IDataExtractor#extractLatitudeLongitude(String)
     */
    @Override
    public double[] extractLatitudeLongitude(String data) {

        try {
            JSONObject json = new JSONObject(data);
            JSONObject coordinationObject = json.getJSONObject("coord");
            return new double[]{
                    coordinationObject.getDouble("lat"),
                    coordinationObject.getDouble("lon")
            };
        } catch (JSONException e) {
            e.printStackTrace();
            return new double[0];
        }
    }


}
