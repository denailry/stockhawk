package com.project.denail.stockhawk.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.project.denail.stockhawk.APICallManager;
import com.project.denail.stockhawk.MainActivity;
import com.project.denail.stockhawk.R;
import com.project.denail.stockhawk.data.DataStock;
import com.project.denail.stockhawk.data.DataStockMinim;
import com.project.denail.stockhawk.data.DataUpdate;
import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;

import java.util.List;

/**
 * Created by denail on 17/09/02.
 */

public class WidgetProvider extends AppWidgetProvider {

    public static final String KEY_WIDGET_ID = "appWidgetId";
    public static final String ACTION_UPDATE_DATA = "WidgetProvier.ACTION_UPDATE_DATA";
    public static final String ACTION_ITEM_CLICK = "WidgetProvider.ACTION_ITEM_CLICK";
    public static final String ACTION_UPDATE_WIDGET = "WidgetProvider.ACTION_UPDATE_WIDGET";
    public static final int PI_WIDGET_ACTIVITY = 0;
    public static final int PI_WIDGET_RECEIVER = 1;
    public static final int PI_WIDGET_UPDATE = 2;
    public static final int PI_RECEIVE_UPDATE = 3;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        Intent adapterIntent = new Intent(context, ViewService.class);
        adapterIntent.setData(Uri.parse(adapterIntent.toUri(Intent.URI_INTENT_SCHEME)));

        RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.widget);
        view.setRemoteAdapter(R.id.list_widget, adapterIntent);

        Intent clickIntent = new Intent(context, WidgetProvider.class);
        clickIntent.setAction(ACTION_ITEM_CLICK);
        clickIntent.setData(Uri.parse(clickIntent.toUri(Intent.URI_INTENT_SCHEME)));

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, PI_WIDGET_RECEIVER,
                clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setPendingIntentTemplate(R.id.list_widget, pendingIntent);

        Intent updateIntent = new Intent(context, WidgetProvider.class);
        updateIntent.setAction(ACTION_UPDATE_WIDGET);
        updateIntent.putExtra(KEY_WIDGET_ID, appWidgetId);
        updateIntent.setData(Uri.parse(updateIntent.toUri(Intent.URI_INTENT_SCHEME)));

        PendingIntent updatePending = PendingIntent.getBroadcast(context, PI_WIDGET_UPDATE,
                updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.btn_widget, updatePending);

        appWidgetManager.updateAppWidget(appWidgetId, view);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for(int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(ACTION_UPDATE_DATA)) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int appWidgetIds[] = appWidgetManager
                    .getAppWidgetIds(new ComponentName(context, WidgetProvider.class));
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.list_widget);
        }
        if(intent.getAction().equals(ACTION_ITEM_CLICK)) {
            Intent activityIntent = new Intent(context, MainActivity.class);
            activityIntent.putExtras(intent.getExtras());
            PendingIntent pendingIntent = PendingIntent.getActivity(context, PI_WIDGET_ACTIVITY,
                    activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            try {
                pendingIntent.send(PI_WIDGET_ACTIVITY);
            } catch (PendingIntent.CanceledException e) {
                Log.e(getClass().getSimpleName(), e.toString());
            }
        }
        if(intent.getAction().equals(ACTION_UPDATE_WIDGET)) {
            int appWidgetId = intent.getIntExtra(KEY_WIDGET_ID, -1);

            if(appWidgetId != -1) {
                List<DataUpdate> deprecatedData;
                try {
                    deprecatedData = DataUpdate.getUnUpdatedData();
                } catch (RuntimeException e) {
                    FlowManager.init(new FlowConfig.Builder(context).build());
                    deprecatedData = DataUpdate.getUnUpdatedData();
                }

                if(deprecatedData.size() == 0) {
                    Toast.makeText(context, "Already Up to Date", Toast.LENGTH_SHORT).show();
                } else {
                    RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.widget);
                    view.setViewVisibility(R.id.btn_widget, View.GONE);
                    view.setProgressBar(R.id.bar_widget, 100, 0, true);

                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                    appWidgetManager.partiallyUpdateAppWidget(appWidgetId, view);

                    initAPIManager(context, appWidgetId, deprecatedData);
                }
            }
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    private void initAPIManager(final Context context, final int appWidgetId, final List<DataUpdate> deprecatedData) {
        APICallManager apiCallManager = new APICallManager();
        apiCallManager.setResponseListener(new APICallManager.CollectiveListener(apiCallManager, deprecatedData) {
            @Override
            public void onFinish(List<DataStock> updatedData, String date) {
                for(DataStock dataStock : updatedData) {
                    DataUpdate dataUpdate = new DataUpdate(dataStock.getTitle(), date);
                    DataStockMinim dataStockMinim = new DataStockMinim(dataStock);
                    dataStock.save();
                    dataUpdate.save();
                    dataStockMinim.save();
                }
                updateListWidget(context);

                if(updatedData.size() == deprecatedData.size()) {
                    Toast.makeText(context, "All Data Updated Succesfully", Toast.LENGTH_SHORT).show();
                } else {
                    if(updatedData.size() == 0) {
                        Toast.makeText(context, "Unable to Connect to Server", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Some Data Not Updated", Toast.LENGTH_SHORT).show();
                    }
                }

                RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.widget);
                view.setViewVisibility(R.id.btn_widget, View.VISIBLE);

                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                appWidgetManager.partiallyUpdateAppWidget(appWidgetId, view);
            }
        });
    }

    private void updateListWidget(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int appWidgetIds[] = appWidgetManager.getAppWidgetIds(new ComponentName(context, WidgetProvider.class));
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.list_widget);
    }
}
