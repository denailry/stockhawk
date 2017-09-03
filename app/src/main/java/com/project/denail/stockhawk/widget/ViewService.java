package com.project.denail.stockhawk.widget;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.project.denail.stockhawk.MainActivity;
import com.project.denail.stockhawk.R;
import com.project.denail.stockhawk.data.DataStockMinim;
import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.util.Collections;
import java.util.List;
/**
 * Created by denail on 17/09/02.
 */

public class ViewService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ViewFactory(this.getApplicationContext());
    }

    class ViewFactory implements RemoteViewsFactory {
        Context context;
        List<DataStockMinim> itemList;

        public ViewFactory(Context context) {
            this.context = context;
        }

        @Override
        public void onCreate() {}

        @Override
        public void onDataSetChanged() {
            try {
                itemList = new Select()
                        .from(DataStockMinim.class)
                        .queryList();
                Collections.reverse(itemList);
            } catch (IllegalStateException e) {
                FlowManager.init(new FlowConfig.Builder(context).build());
                itemList = new Select()
                        .from(DataStockMinim.class)
                        .queryList();
                Collections.reverse(itemList);
            }
        }

        @Override
        public void onDestroy() {

        }

        @Override
        public int getCount() {
            return itemList.size();
        }

        @Override
        public RemoteViews getViewAt(int i) {
            DataStockMinim data = itemList.get(i);
            RemoteViews view;
            int cntId;
            if(itemList.get(i).isDown()) {
                view = new RemoteViews(context.getPackageName(), R.layout.item_widget_red);
                cntId = R.id.cnt_widget_red;
            } else {
                view = new RemoteViews(context.getPackageName(), R.layout.item_widget_green);
                cntId = R.id.cnt_widget_green;
            }
            view.setTextViewText(R.id.tv_widget_title, data.getTitle());
            view.setTextViewText(R.id.tv_widget_value, data.getValue());
            view.setTextViewText(R.id.tv_widget_diff, data.getDiff());

            Bundle args = new Bundle();
            args.putString(MainActivity.KEY_TITLE, data.getTitle());

            Intent fillInIntent = new Intent();
            fillInIntent.putExtras(args);
            view.setOnClickFillInIntent(cntId, fillInIntent);

            return view;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public long getItemId(int i) {
            return itemList.get(i).getId();
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
