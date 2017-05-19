package com.udacity.stockhawk.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.sync.QuoteSyncJob;
import com.udacity.stockhawk.sync.StockWidgetRemoteViewsService;
import com.udacity.stockhawk.ui.StockDetailActivity;

/**
 * Implementation of App Widget functionality.
 */
public class StockHawkWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_stock_hawk);
            views.setRemoteAdapter(R.id.lv_stocks, new Intent(context, StockWidgetRemoteViewsService.class));

            Intent detailIntent = new Intent(context, StockDetailActivity.class);
            PendingIntent startActivityPendingIntent = PendingIntent.getActivity(context, 0, detailIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setPendingIntentTemplate(R.id.lv_stocks, startActivityPendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (QuoteSyncJob.ACTION_DATA_UPDATED.equals(intent.getAction())) {
            AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
            ComponentName componentName = new ComponentName(context, StockHawkWidget.class);

            int[] appWidgetIds = widgetManager.getAppWidgetIds(componentName);
            widgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.lv_stocks);
        }
    }
}

