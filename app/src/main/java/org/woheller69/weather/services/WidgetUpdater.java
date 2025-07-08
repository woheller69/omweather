package org.woheller69.weather.services;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.List;

public class WidgetUpdater extends Worker {
    private Context mContext;
    public WidgetUpdater(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        mContext = context;
    }

    @Override
    public Result doWork() {
        // Do the work here--in this case, upload the images.
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
        List<AppWidgetProviderInfo> providers = appWidgetManager.getInstalledProviders();

        for (AppWidgetProviderInfo info : providers) {
            ComponentName provider = info.provider;
            if (provider.getPackageName().equals(getApplicationContext().getPackageName())) {
                int[] widgetIds = appWidgetManager.getAppWidgetIds(provider);
                Log.d("WidgetUpdater",provider.getClassName() + widgetIds.length);
                Intent intent = new Intent();
                intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                intent.setComponent(provider); // this is the ComponentName
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);
                getApplicationContext().sendBroadcast(intent);
            }
        }

        // Indicate whether the work finished successfully with the Result
        return Result.success();
    }

}
