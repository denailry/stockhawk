package com.project.denail.stockhawk;

import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.project.denail.stockhawk.data.DataStock;
import com.project.denail.stockhawk.data.DataUpdate;
import com.project.denail.stockhawk.data.DataUpdate_Table;
import com.project.denail.stockhawk.fragment.FragmentStockChart;
import com.project.denail.stockhawk.fragment.FragmentStockList;
import com.project.denail.stockhawk.listener.SingleListener;
import com.project.denail.stockhawk.listener.VoidListener;
import com.project.denail.stockhawk.receiver.NetworkReceiver;
import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.Select;
import java.util.List;

import static android.app.FragmentManager.POP_BACK_STACK_INCLUSIVE;
import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;

public class MainActivity extends AppCompatActivity {
    public static final String KEY_TITLE = "title";
    public static final String KEY_HIGHLIGHT = "highlight";
    public static final String ID_FRAG_CHART = "frag_main_chart";

    private boolean dualPane;
    private boolean currentNetworkStatus;
    private String currentDataTitle;
    private APICallManager apiCallManager;
    private NetworkReceiver networkReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BackCache.newInstance();

        dualPane = (findViewById(R.id.root_main) == null);
        apiCallManager = new APICallManager();
        FlowManager.init(new FlowConfig.Builder(this).build());
        initInstanceState(savedInstanceState);
        initFragment();
        initNetwork();
        initAPIManager();
        initData();
    }

    private void initInstanceState(Bundle instanceState) {
        if(instanceState != null && instanceState.containsKey(KEY_TITLE)) {
            currentDataTitle = instanceState.getString(KEY_TITLE);
        }
    }

    private void initFragment() {
        if(dualPane) {
            FragmentStockList fragmentList = (FragmentStockList) getSupportFragmentManager()
                    .findFragmentById(R.id.frag_main_land_list);
            fragmentList.setListener(new StockListClick(), new StockListAdd());
            Bundle arg = new Bundle();
            arg.putBoolean(KEY_HIGHLIGHT, dualPane);
            arg.putString(KEY_TITLE, currentDataTitle);
            FragmentStockChart fragmentChart = (FragmentStockChart) getSupportFragmentManager()
                    .findFragmentById(R.id.frag_main_chart);
            fragmentChart.setData(currentDataTitle);
            fragmentList.setArguments(arg);
        } else {
            FragmentStockList fragmentList = (FragmentStockList) getSupportFragmentManager()
                    .findFragmentById(R.id.frag_main_port_list);
            fragmentList.setListener(new StockListClick(), new StockListAdd());
            Bundle arg = new Bundle();
            arg.putBoolean(KEY_HIGHLIGHT, dualPane);
            fragmentList.setArguments(arg);
        }
    }


    private void initNetwork() {
        currentNetworkStatus = isNetworkActive();
        networkReceiver = new NetworkReceiver(new VoidListener() {
            @Override
            public void onAction() {
                currentNetworkStatus = isNetworkActive();
                apiCallManager.setNetworkActive(currentNetworkStatus);
            }
        });
        IntentFilter intentFilter = new IntentFilter(CONNECTIVITY_ACTION);
        this.registerReceiver(networkReceiver, intentFilter);
    }

    private void initAPIManager() {
        apiCallManager.setNetworkActive(currentNetworkStatus);
        apiCallManager.setResponseListener(new APICallManager.ResponseListener() {

            FragmentStockList fragmentList;
            FragmentStockChart fragmentChart;

            private void getFragment() {
                if(dualPane) {
                    fragmentList = (FragmentStockList) getSupportFragmentManager()
                            .findFragmentById(R.id.frag_main_land_list);
                } else {
                    fragmentList = (FragmentStockList) getSupportFragmentManager()
                            .findFragmentById(R.id.frag_main_port_list);
                }

                fragmentChart = (FragmentStockChart) getSupportFragmentManager()
                        .findFragmentById(R.id.frag_main_chart);
            }

            @Override
            public void onTimeout() {
                Toast.makeText(getApplicationContext(), "Unable to Connect to Server", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onReceiveNew(DataStock dataStock, String date) {
                if(dataStock != null) {
                    DataUpdate dataUpdate = new DataUpdate(dataStock.getTitle(), date);
                    dataStock.save();
                    dataUpdate.save();
                    getFragment();
                    if(fragmentList != null) {
                        if(fragmentList.getAdapter().addItem(dataStock)) {
                            Toast.makeText(getApplicationContext(), "New Stock Added", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Stock Already Exist", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "No Stock Found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onReceiveUpdate(DataStock dataStock, String date) {
                DataUpdate dataUpdate = new Select()
                        .from(DataUpdate.class)
                        .where(DataUpdate_Table.title.eq(dataStock.getTitle()))
                        .querySingle();
                dataUpdate.setLastUpdate(date);
                dataUpdate.save();
                dataStock.save();
                getFragment();
                if(fragmentList != null) {
                    (fragmentList.getAdapter()).refresh(dataStock);
                }
                if(currentDataTitle != null && currentDataTitle.equals(dataStock.getTitle())) {
                    if(fragmentChart != null) {
                        fragmentChart.setData(currentDataTitle);
                    }
                }
            }
        });
    }

    private void initData() {
        // Indexing Old Data
        String todayDate = TimeManip.getTodayDate();
        List<DataUpdate> dataUpdateList = new Select()
                .from(DataUpdate.class)
                .queryList();
        for(DataUpdate dataUpdate : dataUpdateList) {
            if(!dataUpdate.getLastUpdate().equals(todayDate)) {
                apiCallManager.addUnUpdatedData(dataUpdate.getTitle());
            }
        }
    }

    private boolean isNetworkActive() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return (networkInfo != null);
    }

    private class StockListClick implements SingleListener {
        @Override
        public void onAction(Object object) {
            currentDataTitle = (String) object;
            if(dualPane) {
                FragmentStockChart fragment = (FragmentStockChart) getSupportFragmentManager()
                        .findFragmentById(R.id.frag_main_chart);
                fragment.setData(currentDataTitle);
            } else {
                FragmentStockChart fragment = FragmentStockChart.newInstance(currentDataTitle);
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.root_main, fragment)
                        .addToBackStack(ID_FRAG_CHART)
                        .commit();
            }
        }
    }

    private class StockListAdd implements SingleListener {
        @Override
        public void onAction(Object object) {
            String dataTitle = (String) object;
            apiCallManager.addNewData(dataTitle);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(KEY_TITLE, currentDataTitle);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        getSupportFragmentManager().popBackStack(ID_FRAG_CHART, POP_BACK_STACK_INCLUSIVE);
        try {
            this.unregisterReceiver(networkReceiver);
        } catch (IllegalArgumentException e) {
            Log.e(getClass().getSimpleName(), "Receiver has been removed before");
        }
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if(BackCache.getSize() != 0) {
            BackCache.onBackPress();
            return;
        }
        super.onBackPressed();
    }
}
