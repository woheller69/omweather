package org.woheller69.weather.dialogs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.os.ConfigurationCompat;
import androidx.fragment.app.DialogFragment;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONObject;
import org.woheller69.weather.BuildConfig;
import org.woheller69.weather.R;
import org.woheller69.weather.activities.ManageLocationsActivity;
import org.woheller69.weather.database.City;
import org.woheller69.weather.database.SQLiteHelper;
import org.woheller69.weather.ui.util.geocodingApiCall;
import org.woheller69.weather.ui.util.AutoSuggestAdapter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class AddLocationDialogOmGeocodingAPI extends DialogFragment {

    Activity activity;
    View rootView;
    SQLiteHelper database;
    private WebView webview;

    private AutoCompleteTextView autoCompleteTextView;
    City selectedCity;

    private static final int TRIGGER_AUTO_COMPLETE = 100;
    private static final long AUTO_COMPLETE_DELAY = 300;
    private static final int TRIGGER_HIDE_KEYBOARD = 200;
    private static final long HIDE_KEYBOARD_DELAY = 3000;
    private Handler handler;
    private AutoSuggestAdapter autoSuggestAdapter;
    String url="https://geocoding-api.open-meteo.com/v1/search?name=";
    String lang="en";

    public AddLocationDialogOmGeocodingAPI() {
        setRetainInstance(true);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
       if (context instanceof Activity){
            this.activity=(Activity) context;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) dismiss();
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.removeMessages(TRIGGER_HIDE_KEYBOARD);
        if(selectedCity != null && webview != null) webview.loadUrl("file:///android_asset/map.html?lat=" + selectedCity.getLatitude() + "&lon=" + selectedCity.getLongitude());
    }

    @NonNull
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Locale locale = ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration()).get(0);
        if (locale != null) lang=locale.getLanguage();

        LayoutInflater inflater = activity.getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View view = inflater.inflate(R.layout.dialog_add_location, null);

        rootView = view;

        builder.setView(view);
        builder.setTitle(activity.getString(R.string.dialog_add_label));

        this.database = SQLiteHelper.getInstance(activity);


        webview = rootView.findViewById(R.id.webViewAddLocation);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setUserAgentString(BuildConfig.APPLICATION_ID+"/"+BuildConfig.VERSION_NAME);
        webview.setBackgroundColor(0x00000000);
        webview.setBackgroundResource(R.drawable.map_back);

        autoCompleteTextView = (AutoCompleteTextView) rootView.findViewById(R.id.autoCompleteTvAddDialog);

        //Setting up the adapter for AutoSuggest
        autoSuggestAdapter = new AutoSuggestAdapter(requireContext(),
                R.layout.list_item_autocomplete);
        autoCompleteTextView.setThreshold(2);
        autoCompleteTextView.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        autoCompleteTextView.setAdapter(autoSuggestAdapter);

        autoCompleteTextView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        selectedCity=autoSuggestAdapter.getObject(position);
                        //Hide keyboard to have more space
                        final InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
                        handler.removeMessages(TRIGGER_HIDE_KEYBOARD);
                        //Show city on map
                        webview.loadUrl("file:///android_asset/map.html?lat=" + selectedCity.getLatitude() + "&lon=" + selectedCity.getLongitude());
                    }
                });

        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int
                    count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                handler.removeMessages(TRIGGER_AUTO_COMPLETE);
                handler.sendEmptyMessageDelayed(TRIGGER_AUTO_COMPLETE, AUTO_COMPLETE_DELAY);
                handler.removeMessages(TRIGGER_HIDE_KEYBOARD);
                handler.sendEmptyMessageDelayed(TRIGGER_HIDE_KEYBOARD, HIDE_KEYBOARD_DELAY);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        handler = new Handler(Looper.getMainLooper(), msg -> {
            if (msg.what == TRIGGER_AUTO_COMPLETE) {
                if (!TextUtils.isEmpty(autoCompleteTextView.getText())) {
                    try {
                        makeApiCall(URLEncoder.encode(autoCompleteTextView.getText().toString(), StandardCharsets.UTF_8.name()));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            } else if (msg.what == TRIGGER_HIDE_KEYBOARD) {
                //Hide keyboard to show entries behind the keyboard
                final InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
            }
            return false;
        });

        builder.setPositiveButton(activity.getString(R.string.dialog_add_add_button), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                performDone();
            }
        });

        builder.setNegativeButton(activity.getString(R.string.dialog_add_close_button), null);

        return builder.create();

    }
    private void makeApiCall(String text) {
        geocodingApiCall.make(getContext(), text, url,lang, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //parsing logic, please change it as per your requirement
                List<String> stringList = new ArrayList<>();
                List<City> cityList = new ArrayList<>();
                try {
                    JSONObject responseObject = new JSONObject(response);

                    JSONArray array = responseObject.getJSONArray("results");
                    for (int i = 0; i < array.length(); i++) {
                        City city =new City();
                        String citystring="";
                        JSONObject jsonFeatures = array.getJSONObject(i);
                        String name="";
                        if (jsonFeatures.has("name")) {
                            name=jsonFeatures.getString("name");
                            citystring=citystring+name;
                        }

                        String countrycode="";
                        if (jsonFeatures.has("country_code")) {
                            countrycode=jsonFeatures.getString("country_code");
                            citystring=citystring+", "+countrycode;
                        }
                        String admin1="";
                        if (jsonFeatures.has("admin1")) {
                            admin1=jsonFeatures.getString("admin1");
                            citystring=citystring+", "+admin1;
                        }

                        String admin2="";
                        if (jsonFeatures.has("admin2")) {
                            admin2=jsonFeatures.getString("admin2");
                            citystring=citystring+", "+admin2;
                        }

                        String admin3="";
                        if (jsonFeatures.has("admin3")) {
                            admin3=jsonFeatures.getString("admin3");
                            citystring=citystring+", "+admin3;
                        }

                        String admin4="";
                        if (jsonFeatures.has("admin4")) {
                            admin4=jsonFeatures.getString("admin4");
                            citystring=citystring+", "+admin4;
                        }

                        city.setCityName(name);
                        city.setCountryCode(countrycode);
                        city.setLatitude((float) jsonFeatures.getDouble("latitude"));
                        city.setLongitude((float) jsonFeatures.getDouble("longitude"));
                        cityList.add(city);
                        stringList.add(citystring);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //IMPORTANT: set data here and notify
                autoSuggestAdapter.setData(stringList,cityList);
                autoSuggestAdapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Handler h = new Handler(activity.getMainLooper());
                h.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(activity, error.toString(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }


    private void performDone() {
        if (selectedCity == null) {
            Toast.makeText(activity, R.string.dialog_add_no_city_found, Toast.LENGTH_SHORT).show();
        }else {
            ((ManageLocationsActivity) activity).addCityToList(selectedCity);
            dismiss();
        }
    }

}
