package org.woheller69.weather.ui.RecycleList;

import android.content.Context;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.db.chart.Tools;
import com.db.chart.model.BarSet;
import com.db.chart.model.ChartSet;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.BarChartView;
import com.db.chart.view.LineChartView;

import org.woheller69.weather.R;
import org.woheller69.weather.database.CurrentWeatherData;
import org.woheller69.weather.database.HourlyForecast;
import org.woheller69.weather.database.SQLiteHelper;
import org.woheller69.weather.database.WeekForecast;
import org.woheller69.weather.preferences.AppPreferencesManager;
import org.woheller69.weather.ui.Help.StringFormatUtils;
import org.woheller69.weather.ui.UiResourceProvider;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.woheller69.weather.weather_api.IApiToDatabaseConversion;

public class CityWeatherAdapter extends RecyclerView.Adapter<CityWeatherAdapter.ViewHolder> {
    private static final String TAG = "Forecast_Adapter";

    private int[] dataSetTypes;
    private List<HourlyForecast> courseDayList;
    private float[][] forecastData;

    private Context context;
    private ViewGroup mParent;
    private RecyclerView mCourseOfDay;
    private RecyclerView mWeekWeather;

    private CurrentWeatherData currentWeatherDataList;

    public static final int OVERVIEW = 0;
    public static final int DETAILS = 1;
    public static final int WEEK = 2;
    public static final int DAY = 3;
    public static final int CHART = 4;
    public static final int EMPTY = 5;

    public CityWeatherAdapter(CurrentWeatherData currentWeatherDataList, int[] dataSetTypes, Context context) {
        this.currentWeatherDataList = currentWeatherDataList;
        this.dataSetTypes = dataSetTypes;
        this.context = context;

        SQLiteHelper database = SQLiteHelper.getInstance(context.getApplicationContext());

        List<HourlyForecast> hourlyForecasts = database.getForecastsByCityId(currentWeatherDataList.getCity_id());
        List<WeekForecast> weekforecasts = database.getWeekForecastsByCityId(currentWeatherDataList.getCity_id());

        updateForecastData(hourlyForecasts);
        updateWeekForecastData(weekforecasts);

    }

    // function update 3-hour or 1-hour forecast list
    public void updateForecastData(List<HourlyForecast> hourlyForecasts) {

        courseDayList = new ArrayList<>();

        long onehourago = System.currentTimeMillis() - (1 * 60 * 60 * 1000);

            for (HourlyForecast f : hourlyForecasts) {
                if (f.getForecastTime() >= onehourago) {
                    courseDayList.add(f);
                }
            }
            notifyDataSetChanged();
    }

    // function for week forecast list
    public void updateWeekForecastData(List<WeekForecast> forecasts) {
        if (forecasts.isEmpty()) return;

        int cityId = forecasts.get(0).getCity_id();

        SQLiteHelper dbHelper = SQLiteHelper.getInstance(context.getApplicationContext());
        int zonemilliseconds = dbHelper.getCurrentWeatherByCityId(cityId).getTimeZoneSeconds() * 1000;

        //temp max 0, temp min 1, humidity 2, pressure 3, precipitation 4, wind 5, wind direction 6, uv_index 7, forecast time 8, weather ID 9, number of FCs for day 10

        forecastData = new float[forecasts.size()][11];

        for (int i=0;i<forecasts.size();i++){
            forecastData[i][0]=forecasts.get(i).getMaxTemperature();
            forecastData[i][1]=forecasts.get(i).getMinTemperature();
            forecastData[i][2]=forecasts.get(i).getHumidity();
            forecastData[i][3]=forecasts.get(i).getPressure();
            forecastData[i][4]=forecasts.get(i).getPrecipitation();
            forecastData[i][5]=forecasts.get(i).getWind_speed();
            forecastData[i][6]=forecasts.get(i).getWind_direction();
            forecastData[i][7]=forecasts.get(i).getUv_index();
            forecastData[i][8]=forecasts.get(i).getForecastTime()+zonemilliseconds;
            forecastData[i][9]=forecasts.get(i).getWeatherID();
            forecastData[i][10]=1;
        }

        notifyDataSetChanged();
    }



    static class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(View v) {
            super(v);
        }
    }

    public class OverViewHolder extends ViewHolder {
        TextView temperature;
        ImageView weather;
        ImageView windicon;
        TextView updatetime;
        TextView sun;

        OverViewHolder(View v) {
            super(v);
            this.temperature = v.findViewById(R.id.card_overview_temperature);
            this.weather = v.findViewById(R.id.card_overview_weather_image);
            this.sun=v.findViewById(R.id.card_overview_sunrise_sunset);
            this.windicon=v.findViewById(R.id.card_overview_windicon);
            this.updatetime=v.findViewById(R.id.card_overview_update_time);
        }
    }

    public class DetailViewHolder extends ViewHolder {
        TextView humidity;
        TextView pressure;
        TextView windspeed;
        TextView rain60min;
        TextView rain60minLegend;
        TextView time;
        ImageView winddirection;

        DetailViewHolder(View v) {
            super(v);
            this.humidity = v.findViewById(R.id.card_details_humidity_value);
            this.pressure = v.findViewById(R.id.card_details_pressure_value);
            this.windspeed = v.findViewById(R.id.card_details_wind_speed_value);
            this.rain60min = v.findViewById(R.id.card_details_rain60min_value);
            this.rain60minLegend=v.findViewById(R.id.card_details_legend_rain60min);
            this.winddirection =v.findViewById((R.id.card_details_wind_direction_value));
            this.time=v.findViewById(R.id.card_details_title);
        }
    }

    public class WeekViewHolder extends ViewHolder {
        RecyclerView recyclerView;

        WeekViewHolder(View v) {
            super(v);
            recyclerView = v.findViewById(R.id.recycler_view_week);
            mWeekWeather=recyclerView;
        }
    }

    public class DayViewHolder extends ViewHolder {
        RecyclerView recyclerView;
        TextView recyclerViewHeader;

        DayViewHolder(View v) {
            super(v);
            recyclerView = v.findViewById(R.id.recycler_view_course_day);
            mCourseOfDay=recyclerView;
            recyclerViewHeader=v.findViewById(R.id.recycler_view_header);
        }
    }

    public class ChartViewHolder extends ViewHolder {
        TextView temperatureunit;
        TextView precipitationunit;
        LineChartView lineChartView;
        BarChartView barChartView;
        BarChartView barChartViewAxis;

        ChartViewHolder(View v) {
            super(v);
            this.lineChartView = v.findViewById(R.id.graph_temperature);
            this.barChartView = v.findViewById(R.id.graph_precipitation);
            this.temperatureunit=v.findViewById(R.id.graph_temperatureunit);
            this.barChartViewAxis=v.findViewById(R.id.graph_axis);
            this.precipitationunit=v.findViewById(R.id.graph_precipitationunit);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v;
        mParent=viewGroup;
        if (viewType == OVERVIEW) {
            v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.card_overview, viewGroup, false);

            return new OverViewHolder(v);

        } else if (viewType == DETAILS) {

            v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.card_details, viewGroup, false);
            return new DetailViewHolder(v);

        } else if (viewType == WEEK) {

            v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.card_week, viewGroup, false);
            return new WeekViewHolder(v);

        } else if (viewType == DAY) {

            v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.card_day, viewGroup, false);
            return new DayViewHolder(v);

        } else if (viewType == CHART) {

            v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.card_chart, viewGroup, false);
            return new ChartViewHolder(v);
        } else {
            v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.card_empty, viewGroup, false);
            return new ViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        boolean isDay = currentWeatherDataList.isDay(context);

        if (viewHolder.getItemViewType() == OVERVIEW) {
            OverViewHolder holder = (OverViewHolder) viewHolder;

            //correct for timezone differences
            int zoneseconds = currentWeatherDataList.getTimeZoneSeconds();
            long riseTime = (currentWeatherDataList.getTimeSunrise() + zoneseconds) * 1000;
            long setTime = (currentWeatherDataList.getTimeSunset() + zoneseconds) * 1000;
            if (riseTime==zoneseconds*1000 || setTime==zoneseconds*1000) holder.sun.setText("\u2600\u25b2 --:--" + " \u25bc --:--" );
            else  {
                holder.sun.setText("\u2600\u25b2 " + StringFormatUtils.formatTimeWithoutZone(context, riseTime) + " \u25bc " + StringFormatUtils.formatTimeWithoutZone(context, setTime));
            }
            holder.windicon.setImageResource(StringFormatUtils.colorWindSpeedWidget(currentWeatherDataList.getWindSpeed()));
            long time = currentWeatherDataList.getTimestamp();
            long updateTime = ((time + zoneseconds) * 1000);

            holder.updatetime.setText("("+StringFormatUtils.formatTimeWithoutZone(context, updateTime)+")");

            setImage(currentWeatherDataList.getWeatherID(), holder.weather, isDay);

            holder.temperature.setText(StringFormatUtils.formatTemperature(context, currentWeatherDataList.getTemperatureCurrent()));

        } else if (viewHolder.getItemViewType() == DETAILS) {

            DetailViewHolder holder = (DetailViewHolder) viewHolder;

            long time = currentWeatherDataList.getTimestamp();
            int zoneseconds = currentWeatherDataList.getTimeZoneSeconds();
            long updateTime = ((time + zoneseconds) * 1000);

            holder.time.setText(String.format("%s (%s)", context.getResources().getString(R.string.card_details_heading), StringFormatUtils.formatTimeWithoutZone(context, updateTime)));
            holder.humidity.setText(StringFormatUtils.formatInt(currentWeatherDataList.getHumidity(), context.getString(R.string.units_rh)));
            holder.pressure.setText(StringFormatUtils.formatDecimal(currentWeatherDataList.getPressure(), context.getString(R.string.units_hPa)));
            holder.windspeed.setText(StringFormatUtils.formatWindSpeed(context, currentWeatherDataList.getWindSpeed()));
            holder.windspeed.setBackground(StringFormatUtils.colorWindSpeed(context, currentWeatherDataList.getWindSpeed()));
            holder.winddirection.setRotation(currentWeatherDataList.getWindDirection());

            if (currentWeatherDataList.getRain60min()!=null && currentWeatherDataList.getRain60min().length()==12){
                holder.rain60min.setText(currentWeatherDataList.getRain60min().substring(0,3)+"\u2009"+currentWeatherDataList.getRain60min().substring(3,6)+"\u2009"+currentWeatherDataList.getRain60min().substring(6,9)+"\u2009"+currentWeatherDataList.getRain60min().substring(9));
            } else {
                holder.rain60min.setText(R.string.error_no_rain60min_data);
            }
            holder.rain60minLegend.setText("( "+context.getResources().getString(R.string.units_mm_h)+String.format(Locale.getDefault(),": □ %.1f ▤ <%.1f ▦ <%.1f ■ >=%.1f )",0.0,0.5,2.5,2.5));

        } else if (viewHolder.getItemViewType() == WEEK) {

            final WeekViewHolder holder = (WeekViewHolder) viewHolder;
            LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
            holder.recyclerView.setLayoutManager(layoutManager);


            final WeekWeatherAdapter adapter = new WeekWeatherAdapter(context, forecastData,currentWeatherDataList.getCity_id());
            holder.recyclerView.setAdapter(adapter);
            holder.recyclerView.setFocusable(false);

            if (mCourseOfDay!=null) {  //otherwise crash if courseOfDay not visible
                CourseOfDayAdapter dayadapter = (CourseOfDayAdapter) mCourseOfDay.getAdapter();
                dayadapter.setWeekRecyclerView(holder.recyclerView);        //provide CourseOfDayAdapter with reference to week recyclerview
                adapter.setCourseOfDayHeaderDate(dayadapter.getCourseOfDayHeaderDate());  //initialize WeekWeatherAdapter with current HeaderDate from CourseOfDayAdapter
            }

            holder.recyclerView.addOnItemTouchListener(
                    new RecyclerItemClickListener(context, holder.recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            SQLiteHelper database = SQLiteHelper.getInstance(context.getApplicationContext());
                            List<WeekForecast> weekforecasts = database.getWeekForecastsByCityId(currentWeatherDataList.getCity_id());
                            long time = weekforecasts.get(position).getForecastTime();  //time of clicked week item
                            time=time-6*3600000;                                       //week item normally midday -> subtract 6h to get morning time

                            if (mCourseOfDay!=null){  //otherwise crash if courseOfDay not visible
                                LinearLayoutManager llm = (LinearLayoutManager) mCourseOfDay.getLayoutManager();

                                assert llm != null;
                                int num = llm.findLastVisibleItemPosition() - llm.findFirstVisibleItemPosition();  //get number of visible elements
                                int i;

                                for (i = 0; i < courseDayList.size(); i++) {
                                    if (courseDayList.get(i).getForecastTime() > time) {        //find first ForecastTime > time of clicked item
                                        Calendar HeaderTime = Calendar.getInstance();
                                        HeaderTime.setTimeZone(TimeZone.getTimeZone("GMT"));
                                        HeaderTime.setTimeInMillis(courseDayList.get(i).getLocalForecastTime(context));
                                        adapter.setCourseOfDayHeaderDate(HeaderTime.getTime());
                                        break;
                                    }
                                }

                                if (i < courseDayList.size()) {  //only if element found
                                    if (i > llm.findFirstVisibleItemPosition()) {               //if scroll right
                                        int min = Math.min(i + num, courseDayList.size()-1);      //scroll to i+num so that requested element is on the left. Max scroll to courseDayList.size()
                                        mCourseOfDay.getLayoutManager().scrollToPosition(min);
                                    } else {                                                    //if scroll left
                                        mCourseOfDay.getLayoutManager().scrollToPosition(i);
                                    }

                                    highlightSelected(view);
                                }

                            }
                        }

                        private void highlightSelected(View view) {
                            for (int j=0;j<courseDayList.size();j++){  //reset all items
                                if (holder.recyclerView.getLayoutManager().getChildAt(j)!=null){
                                    holder.recyclerView.getLayoutManager().getChildAt(j).setBackground(ResourcesCompat.getDrawable(context.getResources(),R.drawable.rounded_transparent,null));
                                }
                            }
                            view.setBackground(ResourcesCompat.getDrawable(context.getResources(),R.drawable.rounded_highlight,null)); //highlight selected item
                        }

                        public void onLongItemClick(View view, int position) {

                        }
                    })
            );

        } else if (viewHolder.getItemViewType() == DAY) {

            DayViewHolder holder = (DayViewHolder) viewHolder;
            LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
            holder.recyclerView.setLayoutManager(layoutManager);
            CourseOfDayAdapter adapter = new CourseOfDayAdapter(courseDayList, context,holder.recyclerViewHeader,holder.recyclerView);
            holder.recyclerView.setAdapter(adapter);
            holder.recyclerView.setFocusable(false);

        } else if (viewHolder.getItemViewType() == CHART) {
            ChartViewHolder holder = (ChartViewHolder) viewHolder;

            SQLiteHelper database = SQLiteHelper.getInstance(context.getApplicationContext());
            AppPreferencesManager prefManager = new AppPreferencesManager(PreferenceManager.getDefaultSharedPreferences(this.context));
            List<WeekForecast> weekforecasts = database.getWeekForecastsByCityId(currentWeatherDataList.getCity_id());

            if (weekforecasts.isEmpty()) {
                return;
            }

            float tmin=1000;
            float tmax=-1000;

            float pmax=0;

            LineSet datasetmax = new LineSet();
            LineSet datasetmin = new LineSet();
            LineSet xaxis = new LineSet(); //create own x-axis as the x-axis of the chart crosses the y-axis numbers. Does not look good

            BarSet precipitationDataset = new BarSet();

            Calendar c = Calendar.getInstance();
            c.setTimeZone(TimeZone.getTimeZone("GMT"));
            int zonemilliseconds = currentWeatherDataList.getTimeZoneSeconds()*1000;

            for (int i=0 ; i< weekforecasts.size();i++) {
                c.setTimeInMillis(weekforecasts.get(i).getForecastTime()+zonemilliseconds);
                int day = c.get(Calendar.DAY_OF_WEEK);
                float temp_max=weekforecasts.get(i).getMaxTemperature();
                float temp_min=weekforecasts.get(i).getMinTemperature();
                float precip=weekforecasts.get(i).getPrecipitation();

                if ((i == 0) || (i == (weekforecasts.size()-1 ))) {  // 1 bar at begin and end for alignment with temperature line chart (first day starts at noon, last ends at noon)
                    precipitationDataset.addBar(context.getResources().getString(StringFormatUtils.getDayShort(day)), precip);
                    //x-labels for precipitation dataset must be there and cannot be empty even though they are made invisible below. Otherwise alignment gets destroyed!
                    datasetmax.addPoint(context.getResources().getString(StringFormatUtils.getDayShort(day)), prefManager.convertTemperatureFromCelsius(temp_max));
                    datasetmin.addPoint(context.getResources().getString(StringFormatUtils.getDayShort(day)), prefManager.convertTemperatureFromCelsius(temp_min));


                } else { // 2 bars in the middle for alignment with temperature line chart

                    precipitationDataset.addBar(context.getResources().getString(StringFormatUtils.getDayShort(day)), precip);
                    precipitationDataset.addBar(context.getResources().getString(StringFormatUtils.getDayShort(day)), precip);

                    datasetmax.addPoint(context.getResources().getString(StringFormatUtils.getDayShort(day)), prefManager.convertTemperatureFromCelsius(temp_max));
                    datasetmin.addPoint(context.getResources().getString(StringFormatUtils.getDayShort(day)), prefManager.convertTemperatureFromCelsius(temp_min));
                }

                if (prefManager.convertTemperatureFromCelsius(temp_max)>tmax) tmax=prefManager.convertTemperatureFromCelsius(temp_max);
                if (prefManager.convertTemperatureFromCelsius(temp_min)<tmin) tmin=prefManager.convertTemperatureFromCelsius(temp_min);
                if (precip>pmax) pmax=precip;
            }

            tmax++;  //add some space above and below
            tmin--;
            int mid = Math.round((tmin + tmax) / 2);
            int step = Math.max(1, (int) Math.ceil(Math.abs(tmax - tmin) / 4));  //step size for y-axis

            for (int i=0 ; i< weekforecasts.size();i++) {
                xaxis.addPoint("",mid-2*step);   //create x-axis at position of min y-axis value
            }

            ArrayList<ChartSet> temperature = new ArrayList<>();
            temperature.add(datasetmax);
            temperature.add(datasetmin);
            temperature.add(xaxis);

            datasetmax.setColor(ContextCompat.getColor(context,R.color.red));
            datasetmax.setThickness(6);
            datasetmax.setSmooth(true);
            datasetmax.setFill(ContextCompat.getColor(context,R.color.middlegrey));

            datasetmin.setColor(ContextCompat.getColor(context,R.color.lightblue));
            datasetmin.setThickness(6);
            datasetmin.setSmooth(true);
            datasetmin.setFill(ContextCompat.getColor(context,R.color.backgroundBlue)); //fill with background, so only range between curves is visible

            xaxis.setThickness(3);
            xaxis.setColor(ContextCompat.getColor(context,R.color.colorPrimaryDark));

            ArrayList<ChartSet> precipitation = new ArrayList<>();
            precipitation.add(precipitationDataset);

            precipitationDataset.setColor(ContextCompat.getColor(context,R.color.blue));
            precipitationDataset.setAlpha(0.8f);  // make precipitation bars transparent

            holder.lineChartView.addData(temperature);
            holder.lineChartView.setAxisBorderValues( mid-2*step, mid+2*step);
            holder.lineChartView.setStep(step);
            holder.lineChartView.setXAxis(false);
            holder.lineChartView.setYAxis(false);
            holder.lineChartView.setYLabels(AxisController.LabelPosition.INSIDE);  //must be INSIDE! OUTSIDE will destroy alignment with precipitation bar chart
            holder.lineChartView.setLabelsColor(ContextCompat.getColor(context,R.color.colorPrimaryDark));
            holder.lineChartView.setAxisColor(ContextCompat.getColor(context,R.color.colorPrimaryDark));
            holder.lineChartView.setFontSize((int) Tools.fromDpToPx(17));
            holder.lineChartView.setBorderSpacing(Tools.fromDpToPx(30));

            holder.lineChartView.show();

            step = (int) Math.ceil((Math.max(10,pmax*2))/4);
            holder.barChartView.addData(precipitation);
            holder.barChartView.setBarSpacing(0);
            holder.barChartView.setAxisBorderValues(0, step*4);  //scale down in case of high precipitation, limit to lower half of chart
            holder.barChartView.setXAxis(false);
            holder.barChartView.setYAxis(false);
            holder.barChartView.setYLabels(AxisController.LabelPosition.NONE); //no labels for precipitation
            holder.barChartView.setLabelsColor(0);  //transparent color, make labels invisible
            holder.barChartView.setAxisColor(ContextCompat.getColor(context,R.color.colorPrimaryDark));
            holder.barChartView.setFontSize((int) Tools.fromDpToPx(17));
            holder.barChartView.setBorderSpacing(Tools.fromDpToPx(30));

            holder.barChartView.show();

            //create another bar chart to show the y-axis for precipitation on the right of the chart
            BarSet precipitationAxisData = new BarSet();
            precipitationAxisData.addBar("", 0);
            ArrayList<ChartSet> precipitationAxis = new ArrayList<>();
            precipitationAxis.add(precipitationAxisData);

            precipitationAxisData.setColor(0);  //transparent color, make invisible

            holder.barChartViewAxis.addData(precipitationAxis);
            holder.barChartViewAxis.setBarSpacing(0);
            holder.barChartViewAxis.setAxisBorderValues(0, step*4);  //scale down in case of high precipitation, limit to lower half of chart
            holder.barChartViewAxis.setStep(step);
            holder.barChartViewAxis.setXAxis(false);
            holder.barChartViewAxis.setYAxis(false);
            holder.barChartViewAxis.setYLabels(AxisController.LabelPosition.OUTSIDE); // labels for precipitation at the right
            holder.barChartViewAxis.setLabelsColor(ContextCompat.getColor(context,R.color.colorPrimaryDark));
            holder.barChartViewAxis.setAxisColor(ContextCompat.getColor(context,R.color.colorPrimaryDark));
            holder.barChartViewAxis.setFontSize((int) Tools.fromDpToPx(17));

            holder.barChartViewAxis.show();

            holder.temperatureunit.setText(" "+ prefManager.getTemperatureUnit() + " ");
            holder.precipitationunit.setText(" " + context.getResources().getString(R.string.units_mm)+" ");
        }
        //No update for error needed
    }

    public void setImage(int value, ImageView imageView, boolean isDay) {
        imageView.setImageResource(UiResourceProvider.getImageResourceForWeatherCategory(value, isDay));
    }


    @Override
    public int getItemCount() {
        return dataSetTypes.length;
    }

    @Override
    public int getItemViewType(int position) {
        return dataSetTypes[position];
    }
}