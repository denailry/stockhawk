package com.project.denail.stockhawk;

import android.util.Log;

import com.project.denail.stockhawk.data.DataStock;
import com.project.denail.stockhawk.data.DataStock_Table;
import com.project.denail.stockhawk.data.DataUpdate;
import com.project.denail.stockhawk.retrofit.ApiHelper;
import com.project.denail.stockhawk.retrofit.ApiService;
import com.project.denail.stockhawk.stock.DatasetData;
import com.project.denail.stockhawk.stock.StockModel;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by denail on 17/09/01.
 */

public class APICallManager implements Runnable {
    private final String API_KEY = "";
    private final int API_LIMIT = 7;

    // Timeout For New Data Call
    private final int TIMEOUT = 3;

    // Network Connection State
    private boolean networkActive;

    // Data Queue
    private List<String> unUpdatedData;
    private List<String> newData;

    // Current Data Running Value
    private String currentTitle;
    private boolean isNewData;
    private boolean isRunning;
    private boolean defautlTimeoutEnabled;
    private int currentIndex;
    private int newDataTryCount;
    private int timeoutTryCount;

    private ResponseListener responseListener;

    public interface ResponseListener {
        void onTimeout();
        void onReceiveNew(DataStock dataStock, String date);
        void onReceiveUpdate(DataStock dataStock, String date);
    }

    @Override
    public void run() {
        chooseData();
        if(currentTitle != null) {
            callApi(this.currentTitle);
        } else {
            isRunning = false;
        }
    }

    public APICallManager() {
        currentTitle = null;
        newDataTryCount = 0;
        timeoutTryCount = 0;
        defautlTimeoutEnabled = false;
        unUpdatedData = new ArrayList<>();
        newData = new ArrayList<>();
    }

    public void addUnUpdatedData(String dataTitle) {
        unUpdatedData.add(dataTitle);
        if(!isRunning) {
            if(networkActive) {
                isRunning = true;
                new Thread(this).start();
            }
        }
    }

    public void addNewData(String dataTitle) {
        newData.add(dataTitle);
        if(!isRunning) {
            isRunning = true;
            new Thread(this).start();
        }
    }

    public void setNetworkActive(boolean networkActive) {
        this.networkActive = networkActive;
        if(!isRunning) {
            if(networkActive) {
                isRunning = true;
                new Thread(this).start();
            }
        }
    }

    public void setResponseListener(ResponseListener responseListener) {
        this.responseListener = responseListener;
    }

    public void setDefautlTimeoutEnabled(boolean defautlTimeoutEnabled) {
        this.defautlTimeoutEnabled = defautlTimeoutEnabled;
    }

    private void chooseData() {
        Log.d("TEST-CHOOSE", "HALO");
        if(newData.size() != 0) {
            this.isNewData = true;
            this.currentIndex = newData.size() - 1;
            this.currentTitle = newData.get(this.currentIndex);
        } else if(unUpdatedData.size() != 0){
            this.isNewData = false;
            this.currentIndex = unUpdatedData.size() - 1;
            this.currentTitle = unUpdatedData.get(this.currentIndex);
        }
    }

    private void onPostCall(boolean callSuccess) {
        this.currentTitle = null;

        if(callSuccess) {
            newDataTryCount = 0;
            if(isNewData) {
                newData.remove(this.currentIndex);
            } else {
                unUpdatedData.remove(this.currentIndex);
            }
        } else {
            if(isNewData) {
                newDataTryCount++;
                if(newDataTryCount >= TIMEOUT || !networkActive) {
                    newData = new ArrayList<>();
                    newDataTryCount = 0;
                    if(responseListener != null) {
                        responseListener.onTimeout();
                    }
                }
            } else if(defautlTimeoutEnabled) {
                timeoutTryCount++;
                if(timeoutTryCount >= TIMEOUT) {
                    unUpdatedData = new ArrayList<>();
                    timeoutTryCount = 0;
                    if(responseListener != null) {
                        responseListener.onTimeout();
                    }
                }
            }
        }

        if(newData.size() != 0 || unUpdatedData.size() != 0) {
            if(networkActive) {
                new Thread(this).start();
            } else {
                isRunning = false;
            }
        } else {
            isRunning = false;
        }
    }

    private void callApi(final String dataTitle) {
        ApiService service = ApiHelper.client().create(ApiService.class);
        Call<StockModel> call = service.getStockData(dataTitle, API_KEY, API_LIMIT);
        call.enqueue(new Callback<StockModel>() {
            @Override
            public void onResponse(Call<StockModel> call, Response<StockModel> response) {
                DataStock data = null;
                if(response.body() != null) {
                    DatasetData stock = response.body().getDatasetData();
                    long id;
                    if(isNewData) {
                        id = TimeManip.genId();
                    } else {
                        id = (new Select().from(DataStock.class)
                                .where(DataStock_Table.title.eq(dataTitle))
                                .querySingle()).getId();
                    }
                    data = new DataStock(id, dataTitle, stock.getRealValues(), 7);
                }
                if(isNewData) {
                    responseListener.onReceiveNew(data, TimeManip.getTodayDate());
                } else {
                    responseListener.onReceiveUpdate(data, TimeManip.getTodayDate());
                }
                onPostCall(true);
            }
            @Override
            public void onFailure(Call<StockModel> call, Throwable t) {
                onPostCall(false);
            }
        });
    }

    public static abstract class CollectiveListener implements ResponseListener {

        private List<DataStock> updatedData;
        private int dataSize;

        public CollectiveListener(APICallManager manager, List<DataUpdate> unUpdatedData) {
            updatedData = new ArrayList<>();
            this.dataSize = unUpdatedData.size();

            manager.setNetworkActive(true);
            manager.setDefautlTimeoutEnabled(true);
            for(DataUpdate data : unUpdatedData) {
                manager.addUnUpdatedData(data.getTitle());
            }
        }

        public abstract void onFinish(List<DataStock> updatedData, String date);

        @Override
        public void onTimeout() {
            Log.d("TEST-API", "HALO 1");
            onFinish(updatedData, TimeManip.getTodayDate());
        }

        @Override
        public void onReceiveNew(DataStock dataStock, String date) {
            Log.d("TEST-API", "HALO 2");
        }

        @Override
        public void onReceiveUpdate(DataStock dataStock, String date) {
            Log.d("TEST-API", "HALO 3");
            updatedData.add(dataStock);
            onPostListen();
        }

        private void onPostListen() {
            if(dataSize == updatedData.size()) {
                onFinish(updatedData, TimeManip.getTodayDate());
            }
        }
    }
}
