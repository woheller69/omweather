package org.woheller69.weather.activities;

import static java.lang.Boolean.TRUE;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import org.woheller69.weather.BuildConfig;
import org.woheller69.weather.R;


public class RainViewerActivity extends AppCompatActivity {

    private WebView webView;
    private ImageButton btnPrev, btnNext, btnStartStop;

    @Override
    protected void onPause() {
        super.onPause();
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rain_viewer);
        float latitude = getIntent().getFloatExtra("latitude", -1);
        float longitude = getIntent().getFloatExtra("longitude", -1);
        int timezoneseconds = getIntent().getIntExtra("timezoneseconds",0);

        int nightmode=0;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if(sharedPreferences.getBoolean("pref_DarkMode", false)==TRUE) {
            int nightModeFlags = getApplicationContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            if (nightModeFlags==Configuration.UI_MODE_NIGHT_YES) nightmode=1;
        }

        int hour12=1;
        if (android.text.format.DateFormat.is24HourFormat(this) || sharedPreferences.getBoolean("pref_TimeFormat", true)==TRUE){
            hour12=0;
        }

        webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setUserAgentString(BuildConfig.APPLICATION_ID+"/"+BuildConfig.VERSION_NAME);
        webView.loadUrl("file:///android_asset/rainviewer.html?lat=" + latitude + "&lon=" + longitude + "&nightmode=" + nightmode + "&hour12=" + hour12 + "&tz="+timezoneseconds);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {  //register buttons when loading of page finished
                super.onPageFinished(webView, url);
                btnNext = findViewById(R.id.rainviewer_next);
                btnPrev = findViewById(R.id.rainviewer_prev);
                btnStartStop = findViewById(R.id.rainviewer_startstop);

                btnNext.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        webView.loadUrl("javascript:stop();showFrame(animationPosition + 1);");
                    }
                });

                btnPrev.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        webView.loadUrl("javascript:stop();showFrame(animationPosition - 1);");
                    }
                });

                btnStartStop.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        webView.loadUrl("javascript:playStop();");
                    }
                });

            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (getSupportActionBar() == null) {
            setSupportActionBar(toolbar);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

    }

}
