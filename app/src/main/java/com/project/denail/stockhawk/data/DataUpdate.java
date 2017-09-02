package com.project.denail.stockhawk.data;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Created by denail on 17/08/27.
 */

@Table(database = Database.class)
public class DataUpdate extends BaseModel {
    @PrimaryKey @Column String title;
    @Column String lastUpdate;

    public DataUpdate() {
    }

    public DataUpdate(String title, String lastUpdate) {
        this.title = title;
        this.lastUpdate = lastUpdate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
