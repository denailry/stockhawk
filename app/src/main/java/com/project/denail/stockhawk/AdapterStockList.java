package com.project.denail.stockhawk;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.project.denail.stockhawk.data.DataStock;
import com.project.denail.stockhawk.data.DataStockMinim;
import com.project.denail.stockhawk.data.Database;
import com.project.denail.stockhawk.listener.SingleListener;
import com.project.denail.stockhawk.widget.WidgetProvider;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by denail on 17/08/26.
 */

public class AdapterStockList extends RecyclerView.Adapter<AdapterStockList.ViewHolder> {

    private ViewHolder currentFocus;
    private List<DataStockMinim> itemList;
    private SingleListener listener;
    private boolean isHighlightEnabled;
    private String dataTitle;
    private Context context;

    public AdapterStockList(SingleListener listener, boolean isHighlightEnabled,
                            String dataTitle, Context context) {
        refresh();
        this.listener = listener;
        this.isHighlightEnabled = isHighlightEnabled;
        this.dataTitle = dataTitle;
        this.context = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_adStocklist_title) TextView tvTitle;
        @BindView(R.id.tv_adStocklist_value) TextView tvValue;
        @BindView(R.id.tv_adStocklist_diff) TextView tvDiff;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_stocklist,
                parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        DataStockMinim item = itemList.get(position);

        if(item.isDown()) {
            holder.tvDiff.setBackgroundColor(Color.parseColor("#ff0000"));
        } else {
            holder.tvDiff.setBackgroundColor(Color.parseColor("#00ff00"));
        }
        if(item.getTitle().equals(dataTitle) && isHighlightEnabled) {
            changeColor(currentFocus, "#000000");
            currentFocus = holder;
            changeColor(currentFocus, "#ffffff");
        }

        holder.tvTitle.setText(item.getTitle());
        holder.tvValue.setText(item.getValue());
        holder.tvDiff.setText(item.getDiff());
        holder.itemView.setOnTouchListener(new TouchListener(holder, item));
    }

    private void changeColor(ViewHolder holder, String backgroundColor) {
        if(holder != null) {
            String newBackgroundColor = "#000000";
            String newContentColor = "#ffffff";
            if(backgroundColor.equals("#ffffff")) {
                newBackgroundColor = "#ffffff";
                newContentColor = "#000000";
            }
            holder.itemView.setBackgroundColor(Color.parseColor(newBackgroundColor));
            holder.tvTitle.setTextColor(Color.parseColor(newContentColor));
            holder.tvValue.setTextColor(Color.parseColor(newContentColor));
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    private int searchItem(String title) {
        boolean exist = false;
        int i = 0;
        while(!exist && i < itemList.size()) {
            if(itemList.get(i).getTitle().equals(title)) {
                exist = true;
            } else {
                i++;
            }
        }
        if(!exist) {
            return -1;
        } else {
            return i;
        }
    }

    public boolean addItem(DataStockMinim item) {
        int index = searchItem(item.getTitle());
        if(index != -1) {
            itemList.set(index, item);
            notifyDataSetChanged();
            return false;
        }
        itemList.add(0, item);
        notifyDataChange();
        return true;
    }

    public void refresh(DataStockMinim item) {
        int index = itemList.indexOf(item);
        if(index != -1) {
            itemList.set(index, item);
        }
        notifyDataChange();
    }

    public void refresh() {
        itemList = new Select()
                .from(DataStockMinim.class)
                .queryList();
        Collections.reverse(itemList);
        notifyDataChange();
    }

    private void notifyDataChange() {
        notifyDataSetChanged();

        try {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int appWidgetIds[] = appWidgetManager
                    .getAppWidgetIds(new ComponentName(context, WidgetProvider.class));
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.list_widget);
        } catch (NullPointerException e) {
            Log.e(getClass().getSimpleName(), e.toString());
        }
    }

    public void setDataTitle(String dataTitle) {
        this.dataTitle = dataTitle;
        notifyDataSetChanged();
    }

    private class TouchListener implements View.OnTouchListener {

        private ViewHolder holder;
        private DataStockMinim item;

        public TouchListener(ViewHolder holder, DataStockMinim item) {
            this.holder = holder;
            this.item = item;
        }

        GestureDetector gesture = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                final float scaleY = holder.itemView.getScaleY();
                Database.deleteData(item.getTitle());
                holder.itemView.animate()
                        .alpha(0f)
                        .scaleY(0f)
                        .setDuration(500)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                refresh();
                                if(holder == currentFocus) {
                                    changeColor(currentFocus, "#000000");
                                    currentFocus = null;
                                    dataTitle = null;
                                }
                                holder.itemView.setScaleY(scaleY);
                                holder.itemView.setAlpha(1f);

                                super.onAnimationEnd(animation);
                            }
                        });
                return super.onFling(e1, e2, velocityX, velocityY);
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                listener.onAction(item.getTitle());
                if(isHighlightEnabled) {
                    changeColor(currentFocus, "#000000");
                    currentFocus = holder;
                    changeColor(currentFocus, "#ffffff");
                }
                return super.onSingleTapConfirmed(e);
            }
        });

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if(holder != currentFocus) {
                if(motionEvent.getAction() == MotionEvent.ACTION_UP ||
                        motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
                    changeColor(holder, "#000000");
                } else if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    changeColor(holder, "#ffffff");
                }
            }
            gesture.onTouchEvent(motionEvent);
            return true;
        }
    }
}
