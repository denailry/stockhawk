
package com.project.denail.stockhawk.stock;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DatasetData {

    @SerializedName("limit")
    @Expose
    private Object limit;
    @SerializedName("transform")
    @Expose
    private Object transform;
    @SerializedName("column_index")
    @Expose
    private Object columnIndex;
    @SerializedName("column_names")
    @Expose
    private List<String> columnNames = null;
    @SerializedName("start_date")
    @Expose
    private String startDate;
    @SerializedName("end_date")
    @Expose
    private String endDate;
    @SerializedName("frequency")
    @Expose
    private String frequency;
    @SerializedName("data")
    @Expose
    private List<List<String>> data = null;
    @SerializedName("collapse")
    @Expose
    private Object collapse;
    @SerializedName("order")
    @Expose
    private Object order;
    @SerializedName("code")
    @Expose
    private String code;

    public Object getLimit() {
        return limit;
    }

    public void setLimit(Object limit) {
        this.limit = limit;
    }

    public Object getTransform() {
        return transform;
    }

    public void setTransform(Object transform) {
        this.transform = transform;
    }

    public Object getColumnIndex() {
        return columnIndex;
    }

    public void setColumnIndex(Object columnIndex) {
        this.columnIndex = columnIndex;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(List<String> columnNames) {
        this.columnNames = columnNames;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public List<List<String>> getData() {
        return data;
    }

    public void setData(List<List<String>> data) {
        this.data = data;
    }

    public Object getCollapse() {
        return collapse;
    }

    public void setCollapse(Object collapse) {
        this.collapse = collapse;
    }

    public Object getOrder() {
        return order;
    }

    public void setOrder(Object order) {
        this.order = order;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<Float> getRealValues() {
        List<Float> realValues = new ArrayList<>();
        for(List<String> list : getData()) {
            Float value = Float.valueOf(list.get(1));
            realValues.add(value);
        }

        return realValues;
    }
}
