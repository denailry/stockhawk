package com.project.denail.stockhawk.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.project.denail.stockhawk.AdapterStockList;
import com.project.denail.stockhawk.BackCache;
import com.project.denail.stockhawk.listener.SingleListener;
import com.project.denail.stockhawk.R;
import com.project.denail.stockhawk.data.DataStock;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.project.denail.stockhawk.MainActivity.KEY_HIGHLIGHT;
import static com.project.denail.stockhawk.MainActivity.KEY_TITLE;

/**
 * Created by denail on 17/08/26.
 */

public class FragmentStockList extends Fragment {

    @BindView(R.id.rv_fragment_stocklist) RecyclerView rv;
    @BindView(R.id.btn_fragment_stocklist_ok) Button btnOk;
    @BindView(R.id.in_fragment_stocklist_add) EditText inAdd;

    private SingleListener itemClickListener;
    private SingleListener itemAddListener;
    private boolean isHighlightEnabled;
    private String dataTitle;

    public void setListener(SingleListener itemClickListener, SingleListener itemAddListener) {
        this.itemClickListener = itemClickListener;
        this.itemAddListener = itemAddListener;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stocklist, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle arg = getArguments();
        if(arg != null) {
            if(arg.containsKey(KEY_HIGHLIGHT)) {
                isHighlightEnabled = arg.getBoolean(KEY_HIGHLIGHT);
            }
            if(arg.containsKey(KEY_TITLE)) {
                dataTitle = arg.getString(KEY_TITLE);
            }
        }

        rv.setLayoutManager(new LinearLayoutManager(getActivity()));
        rv.setAdapter(new AdapterStockList(itemClickListener, isHighlightEnabled, dataTitle, getActivity()));
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String inputTitle = inAdd.getText().toString();
                InputMethodManager imm = (InputMethodManager) getActivity()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(inAdd.getWindowToken(), 0);
                inAdd.setText("");
                if(inputTitle.trim().length() != 0) {
                    itemAddListener.onAction(inputTitle);
                }
            }
        });
    }

    public AdapterStockList getAdapter() {
        return (AdapterStockList) rv.getAdapter();
    }
}
