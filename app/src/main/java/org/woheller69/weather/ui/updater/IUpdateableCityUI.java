package org.woheller69.weather.ui.updater;

import org.woheller69.weather.database.CurrentWeatherData;
import org.woheller69.weather.database.HourlyForecast;
import org.woheller69.weather.database.WeekForecast;

import java.util.List;

/**
 * Created by chris on 24.01.2017.
 */
public interface IUpdateableCityUI {
    void processNewCurrentWeatherData(CurrentWeatherData data);

    void processNewForecasts(List<HourlyForecast> hourlyForecasts);

    void processNewWeekForecasts(List<WeekForecast> forecasts);
}
