package com.project.denail.stockhawk;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.project.denail.stockhawk.data.DataStock;
import com.project.denail.stockhawk.listener.SingleListener;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by denail on 17/08/26.
 */

public class AdapterStockList extends RecyclerView.Adapter<AdapterStockList.ViewHolder> {

    private ViewHolder currentFocus;
    private List<DataStock> itemList;
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
        DataStock item = itemList.get(position);
        ArrayList<Float> values = item.getValues(); Collections.reverse(values);
        int lastIndex = values.size()-1;
        float value = values.get(lastIndex);
        float diff = 0;
        String strDiff;

        if(lastIndex > 0) {
            diff = values.get(lastIndex) - values.get(lastIndex-1);
        }
        if(diff > 0) {
            strDiff = String.valueOf("+" + String.format(new Locale("en"), "%.2f", diff));
        } else {
            strDiff = String.valueOf(String.format(new Locale("en"), "%.2f", diff));
        }
        if(diff >= 0) {
            holder.tvDiff.setBackgroundColor(Color.parseColor("#00ff00"));
        } else {
            holder.tvDiff.setBackgroundColor(Color.parseColor("#ff0000"));
        }
        if(item.getTitle().equals(dataTitle) && isHighlightEnabled) {
            changeColor(currentFocus, "#000000");
            currentFocus = holder;
            changeColor(currentFocus, "#ffffff");
        }

        holder.tvTitle.setText(item.getTitle());
        holder.tvValue.setText(String.format(new Locale("en"), "%.2f", value));
        holder.tvDiff.setText(strDiff);
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

    public boolean addItem(DataStock item) {
        int index = searchItem(item.getTitle());
        if(index != -1) {
            itemList.set(index, item);
            notifyDataSetChanged();
            return false;
        }
        itemList.add(0, item);
        notifyDataSetChanged();
        return true;
    }

    public void refresh(DataStock item) {
        int index = itemList.indexOf(item);
        if(index != -1) {
            itemList.set(index, item);
        }
        notifyDataSetChanged();
    }

    public void refresh() {
        itemList = new Select()
                .from(DataStock.class)
                .queryList();
        Collections.sort(itemList, new ThisComparator());
        notifyDataSetChanged();
        Log.d("TEST-SIZE", String.valueOf(itemList.size()));
    }

    private class ThisComparator implements Comparator<DataStock> {
        @Override
        public int compare(DataStock dataStock, DataStock t1) {
            if(dataStock.getId() == t1.getId()) {
                return 0;
            } else if(dataStock.getId() > t1.getId()) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    private class TouchListener implements View.OnTouchListener {

        private ViewHolder holder;
        private DataStock item;

        public TouchListener(ViewHolder holder, DataStock item) {
            this.holder = holder;
            this.item = item;
        }

        GestureDetector gesture = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                final float scaleY = holder.itemView.getScaleY();
                item.delete();
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
