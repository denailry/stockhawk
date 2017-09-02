
package com.project.denail.stockhawk.stock;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class StockModel {

    @SerializedName("dataset_data")
    @Expose
    private DatasetData datasetData;

    public DatasetData getDatasetData() {
        return datasetData;
    }

    public void setDatasetData(DatasetData datasetData) {
        this.datasetData = datasetData;
    }

}
