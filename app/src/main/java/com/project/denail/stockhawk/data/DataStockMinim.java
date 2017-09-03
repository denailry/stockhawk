package com.project.denail.stockhawk.data;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by denail on 17/09/02.
 */

@Table(database = Database.class)
public class DataStockMinim extends BaseModel {
    @PrimaryKey @Column private String title;
    @Column private long id;
    @Column private String value;
    @Column private String diff;
    @Column private boolean isDown;

    public DataStockMinim() {}

    public DataStockMinim(DataStock dataStock) {
        ArrayList<Float> values = dataStock.getValues();
        int lastIndex = values.size()-1;
        float realValue = values.get(lastIndex);
        float realDiff = 0;
        String strDiff;
        if(lastIndex > 0) {
            realDiff = realValue - values.get(lastIndex-1);
        }
        if(realDiff > 0) {
            strDiff = "+" + String.format(new Locale("en"), "%.2f", realDiff);
        } else {
            strDiff = String.format(new Locale("en"), "%.2f", realDiff);
        }

        this.id = dataStock.getId();
        this.title = dataStock.getTitle();
        this.value = String.format(new Locale("en"), "%.2f", realValue);
        this.diff = strDiff;
        this.isDown = (realDiff < 0);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDiff() {
        return diff;
    }

    public void setDiff(String diff) {
        this.diff = diff;
    }

    public boolean isDown() {
        return isDown;
    }

    public void setDown(boolean down) {
        isDown = down;
    }

    public static void deleteData(String dataTitle) {
        DataStockMinim data = new Select()
                .from(DataStockMinim.class)
                .where(DataStockMinim_Table.title.eq(dataTitle))
                .querySingle();
        if(data != null) {
            data.delete();
        }
    }
}
