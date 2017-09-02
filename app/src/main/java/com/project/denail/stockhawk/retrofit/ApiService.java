package com.project.denail.stockhawk.retrofit;

import com.project.denail.stockhawk.stock.StockModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by denail on 5/31/2017.
 */

public interface ApiService {
    @GET("WIKI/{dataset_code}/data.json?")
    Call<StockModel> getStockData(@Path("dataset_code") String dataCode,
                                    @Query("api_key") String apiKey,
                                    @Query("limit") int limit);
}
