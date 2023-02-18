package org.woheller69.weather.ui.updater;

import org.woheller69.weather.database.CurrentWeatherData;
import org.woheller69.weather.database.HourlyForecast;
import org.woheller69.weather.database.WeekForecast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chris on 24.01.2017.
 */

public class ViewUpdater {
    private static List<IUpdateableCityUI> subscribers = new ArrayList<>();

    public static void addSubscriber(IUpdateableCityUI sub) {
        if (!subscribers.contains(sub)) {
            subscribers.add(sub);
        }
    }

    public static void removeSubscriber(IUpdateableCityUI sub) {
        subscribers.remove(sub);
    }

    public static void updateCurrentWeatherData(CurrentWeatherData data) {
        ArrayList<IUpdateableCityUI> subcopy = new ArrayList<>(subscribers);  //copy list needed as bugfix for concurrent modification exception
        for (IUpdateableCityUI sub : subcopy) {
            sub.processNewCurrentWeatherData(data);
        }
    }

    public static void updateWeekForecasts(List<WeekForecast> forecasts) {
        ArrayList<IUpdateableCityUI> subcopy = new ArrayList<>(subscribers);
        for (IUpdateableCityUI sub : subcopy) {
            sub.processNewWeekForecasts(forecasts);
        }
    }

    public static void updateForecasts(List<HourlyForecast> hourlyForecasts) {
        ArrayList<IUpdateableCityUI> subcopy = new ArrayList<>(subscribers);
        for (IUpdateableCityUI sub : subcopy) {
            sub.processNewForecasts(hourlyForecasts);
        }
    }
}
