package org.woheller69.weather.activities;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import org.woheller69.weather.BuildConfig;
import org.woheller69.weather.R;

/**
 * Created by yonjuni on 15.06.16.
 */
public class AboutActivity extends NavigationActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        ((TextView) findViewById(R.id.rainviewerURL)).setMovementMethod(LinkMovementMethod.getInstance());
        ((TextView) findViewById(R.id.openmeteoURL)).setMovementMethod(LinkMovementMethod.getInstance());
        ((TextView) findViewById(R.id.githubURL)).setMovementMethod(LinkMovementMethod.getInstance());
        ((TextView) findViewById(R.id.textFieldVersionName)).setText(BuildConfig.VERSION_NAME);

    }

    @Override
    protected int getNavigationDrawerID() {
        return R.id.nav_about;
    }
}

