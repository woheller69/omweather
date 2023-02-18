package org.woheller69.weather.activities;

import android.content.Context;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;

import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import org.woheller69.weather.R;
import org.woheller69.weather.database.City;
import org.woheller69.weather.database.CityToWatch;
import org.woheller69.weather.database.SQLiteHelper;
import org.woheller69.weather.dialogs.AddLocationDialogOmGeocodingAPI;
import org.woheller69.weather.ui.RecycleList.RecyclerItemClickListener;
import org.woheller69.weather.ui.RecycleList.RecyclerOverviewListAdapter;
import org.woheller69.weather.ui.RecycleList.SimpleItemTouchHelperCallback;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

//in-App: where cities get added & sorted
public class ManageLocationsActivity extends NavigationActivity {

    private SQLiteHelper database;

    private ItemTouchHelper.Callback callback;
    private ItemTouchHelper touchHelper;
    RecyclerOverviewListAdapter adapter;
    List<CityToWatch> cities;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_locations);
        overridePendingTransition(0, 0);
        context=this;
        database = SQLiteHelper.getInstance(getApplicationContext());


        try {
            cities = database.getAllCitiesToWatch();
            Collections.sort(cities, new Comparator<CityToWatch>() {
                @Override
                public int compare(CityToWatch o1, CityToWatch o2) {
                    return o1.getRank() - o2.getRank();
                }

            });
        } catch (NullPointerException e) {
            e.printStackTrace();
            Toast toast = Toast.makeText(getBaseContext(), "No cities in DB", Toast.LENGTH_SHORT);
            toast.show();
        }



        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.list_view_cities);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getBaseContext(), recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        AlertDialog.Builder alert = new AlertDialog.Builder(context);
                        final EditText edittext = new EditText(context);
                        edittext.setText(adapter.getCityName(position));
                        edittext.setTextSize(18);
                        edittext.setGravity(Gravity.CENTER);
                        alert.setTitle(getString(R.string.edit_location_hint_name));
                        alert.setView(edittext);

                        alert.setPositiveButton(getString(R.string.dialog_edit_change_button), (dialog, whichButton) -> adapter.renameCity(position, String.valueOf(edittext.getText())));
                        alert.setNegativeButton(getString(R.string.dialog_add_close_button), (dialog, whichButton) -> {
                        });

                        alert.show();
                    }

                    public void onLongItemClick(View view, int position) {

                    }

                })
        );

        adapter = new RecyclerOverviewListAdapter(getApplicationContext(), cities);
        recyclerView.setAdapter(adapter);
        recyclerView.setFocusable(false);

        callback = new SimpleItemTouchHelperCallback(adapter);
        touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);

        FloatingActionButton addFab1 = (FloatingActionButton) findViewById(R.id.fabAddLocation);

            if (addFab1 != null) {

                addFab1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        AddLocationDialogOmGeocodingAPI addLocationDialog = new AddLocationDialogOmGeocodingAPI();
                        addLocationDialog.show(fragmentManager, "AddLocationDialog");
                        getSupportFragmentManager().executePendingTransactions();
                        addLocationDialog.getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    }
                });
            }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected int getNavigationDrawerID() {
        return R.id.nav_manage;
    }

    public void addCityToList(City city) {
        CityToWatch newCity=convertCityToWatched(city);
        long id=database.addCityToWatch(newCity);
        newCity.setId((int) id);
        newCity.setCityId((int) id);  //use id also instead of city id as unique identifier
        cities.add(newCity);
        adapter.notifyDataSetChanged();
    }
    private CityToWatch convertCityToWatched(City selectedCity) {

        return new CityToWatch(
                database.getMaxRank() + 1,
                selectedCity.getCountryCode(),
                -1,
                selectedCity.getCityId(), selectedCity.getLongitude(),selectedCity.getLatitude(),
                selectedCity.getCityName()
        );
    }
}
