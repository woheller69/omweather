package org.woheller69.weather.ui.RecycleList;

import android.content.Context;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.woheller69.weather.R;
import org.woheller69.weather.database.CurrentWeatherData;
import org.woheller69.weather.database.SQLiteHelper;
import org.woheller69.weather.ui.Help.StringFormatUtils;
import org.woheller69.weather.ui.UiResourceProvider;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by yonjuni on 02.01.17.
 */

public class WeekWeatherAdapter extends RecyclerView.Adapter<WeekWeatherAdapter.WeekForecastViewHolder> {

    private Context context;
    private float[][] forecastData;
    private int cityID;
    private Date courseOfDayHeaderDate;

    WeekWeatherAdapter(Context context, float[][] forecastData, int cityID) {
        this.context = context;
        this.cityID = cityID;
        this.forecastData = forecastData;
        if (forecastData!=null && forecastData.length!=0 && forecastData[0]!=null) {
            this.courseOfDayHeaderDate = new Date((long) forecastData[0][8]);  //init with date of first weekday
        } else this.courseOfDayHeaderDate = new Date();  //fallback if no data available
    }

    public void setCourseOfDayHeaderDate(Date courseOfDayHeaderDate){
        Date oldDate=this.courseOfDayHeaderDate;
        this.courseOfDayHeaderDate=courseOfDayHeaderDate;
        Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("GMT"));

        c.setTime(oldDate);
        int oldDay=c.get(Calendar.DAY_OF_MONTH);
        c.setTime(courseOfDayHeaderDate);
        int newDay=c.get(Calendar.DAY_OF_MONTH);
        if (newDay!=oldDay){   //Refresh viewholder only of day has changed
            notifyDataSetChanged();
        }
    }


    @Override
    public WeekForecastViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_week_forecast, parent, false);
        return new WeekForecastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(WeekForecastViewHolder holder, int position) {
        float[] dayValues = forecastData[position];
        if (dayValues.length!=11) return;  //Fixes app crash if forecastData not yet ready.

        SQLiteHelper dbHelper = SQLiteHelper.getInstance(context);
        CurrentWeatherData currentWeather = dbHelper.getCurrentWeatherByCityId(cityID);

        Calendar forecastTime = Calendar.getInstance();
        forecastTime.setTimeZone(TimeZone.getTimeZone("GMT"));
        forecastTime.setTimeInMillis((long) dayValues[8]);

        boolean isDay;

        if (currentWeather.getTimeSunrise()==0 || currentWeather.getTimeSunset()==0) {
            if ((dbHelper.getCityToWatch(cityID).getLatitude()) > 0) {  //northern hemisphere
                isDay = forecastTime.get(Calendar.DAY_OF_YEAR) >= 80 && forecastTime.get(Calendar.DAY_OF_YEAR) <= 265;  //from March 21 to September 22 (incl)
            } else { //southern hemisphere
                isDay = forecastTime.get(Calendar.DAY_OF_YEAR) < 80 || forecastTime.get(Calendar.DAY_OF_YEAR) > 265;
            }
        } else {
            isDay = true;
        }

        setIcon((int) dayValues[9], holder.weather, isDay);

        if (dayValues[4] == 0)
            holder.precipitation.setText("-");
        else
            holder.precipitation.setText(StringFormatUtils.formatDecimal(dayValues[4], context.getString(R.string.units_mm)));

        holder.uv_index.setText(String.format("UV %s",StringFormatUtils.formatInt(Math.round(dayValues[7]))));
        holder.uv_index.setBackground(StringFormatUtils.colorUVindex(context,Math.round(dayValues[7])));
        holder.wind_speed.setText(StringFormatUtils.formatWindSpeed(context, dayValues[5]));
        holder.wind_speed.setBackground(StringFormatUtils.colorWindSpeed(context, dayValues[5]));

        Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("GMT"));
        c.setTimeInMillis((long) dayValues[8]);
        int day = c.get(Calendar.DAY_OF_WEEK);

        holder.day.setText(StringFormatUtils.getDayShort(day));
        holder.temperature_max.setText(StringFormatUtils.formatTemperature(context,dayValues[0]));
        holder.temperature_min.setText(StringFormatUtils.formatTemperature(context,dayValues[1]));

        day=c.get(Calendar.DAY_OF_MONTH);
        c.setTimeInMillis(courseOfDayHeaderDate.getTime());
        int dayheader=c.get(Calendar.DAY_OF_MONTH);
        if (dayheader==day) {
            holder.itemView.setBackground(ResourcesCompat.getDrawable(context.getResources(),R.drawable.rounded_highlight,null));
        }else{
            holder.itemView.setBackground(ResourcesCompat.getDrawable(context.getResources(),R.drawable.rounded_transparent,null));
        }

    }

    @Override
    public int getItemCount() {
    if (forecastData!=null)
        return forecastData.length;
    else
        return 0;
    }

    class WeekForecastViewHolder extends RecyclerView.ViewHolder {

        TextView day;
        ImageView weather;
        TextView temperature_max;
        TextView temperature_min;
        TextView wind_speed;
        TextView precipitation;
        TextView uv_index;

        WeekForecastViewHolder(View itemView) {
            super(itemView);

            day = itemView.findViewById(R.id.week_forecast_day);
            weather = itemView.findViewById(R.id.week_forecast_weather);
            temperature_max = itemView.findViewById(R.id.week_forecast_temperature_max);
            temperature_max.setTextColor(ContextCompat.getColor(context,R.color.red));
            temperature_min = itemView.findViewById(R.id.week_forecast_temperature_min);
            temperature_min.setTextColor(ContextCompat.getColor(context,R.color.midblue));
            wind_speed = itemView.findViewById(R.id.week_forecast_wind_speed);
            precipitation = itemView.findViewById(R.id.week_forecast_precipitation);
            uv_index = itemView.findViewById(R.id.week_forecast_uv_index);
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void setIcon(int value, ImageView imageView, boolean isDay) {
        imageView.setImageResource(UiResourceProvider.getIconResourceForWeatherCategory(value, isDay));
    }

}
