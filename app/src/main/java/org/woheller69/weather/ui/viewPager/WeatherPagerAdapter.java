package org.woheller69.weather.ui.viewPager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.woheller69.weather.database.CityToWatch;
import org.woheller69.weather.database.CurrentWeatherData;
import org.woheller69.weather.database.HourlyForecast;
import org.woheller69.weather.database.SQLiteHelper;
import org.woheller69.weather.database.WeekForecast;
import org.woheller69.weather.services.UpdateDataService;
import org.woheller69.weather.ui.WeatherCityFragment;
import org.woheller69.weather.ui.updater.IUpdateableCityUI;

import java.util.Collections;
import java.util.List;

import static androidx.core.app.JobIntentService.enqueueWork;
import static org.woheller69.weather.services.UpdateDataService.SKIP_UPDATE_INTERVAL;

/**
 * Created by thomagglaser on 07.08.2017.
 */

public class WeatherPagerAdapter extends FragmentStateAdapter implements IUpdateableCityUI {

    private Context mContext;

    private SQLiteHelper database;

    private List<CityToWatch> cities;


    public WeatherPagerAdapter(Context context, @NonNull FragmentManager supportFragmentManager, @NonNull Lifecycle lifecycle) {
        super(supportFragmentManager,lifecycle);
        this.mContext = context;
        this.database = SQLiteHelper.getInstance(context);

        loadCities();
    }

    public void loadCities() {
        this.cities = database.getAllCitiesToWatch();
        Collections.sort(cities, (o1, o2) -> o1.getRank() - o2.getRank());
    }

    @NonNull
    @Override
    public WeatherCityFragment createFragment(int position) {
        Bundle args = new Bundle();
        args.putInt("city_id", cities.get(position).getCityId());

        return WeatherCityFragment.newInstance(args);
    }

    @Override
    public int getItemCount() {
        return cities.size();
    }

    public CharSequence getPageTitle(int position) {
         return cities.get(position).getCityName();
    }

    public static void refreshSingleData(Context context, Boolean asap, int cityId) {
        Intent intent = new Intent(context, UpdateDataService.class);
        intent.setAction(UpdateDataService.UPDATE_SINGLE_ACTION);
        intent.putExtra(SKIP_UPDATE_INTERVAL, asap);
        intent.putExtra("cityId",cityId);
        enqueueWork(context, UpdateDataService.class, 0, intent);
    }


    @Override
    public void processNewCurrentWeatherData(CurrentWeatherData data) {

    }

    @Override
    public void processNewForecasts(List<HourlyForecast> hourlyForecasts) {
        //empty because Fragments are subscribers themselves
    }

    @Override
    public void processNewWeekForecasts(List<WeekForecast> forecasts) {
        //empty because Fragments are subscribers themselves
    }

    public int getCityIDForPos(int pos) {
            CityToWatch city = cities.get(pos);
                 return city.getCityId();
    }

    public int getPosForCityID(int cityID) {
        for (int i = 0; i < cities.size(); i++) {
            CityToWatch city = cities.get(i);
            if (city.getCityId() == cityID) {
                return i;
            }
        }
        return -1;  //item not found
    }

    public float getLatForPos(int pos) {
        CityToWatch city = cities.get(pos);
        return city.getLatitude();
    }

    public float getLonForPos(int pos) {
        CityToWatch city = cities.get(pos);
        return city.getLongitude();
    }

}