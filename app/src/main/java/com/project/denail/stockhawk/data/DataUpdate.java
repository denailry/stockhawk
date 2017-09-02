package com.project.denail.stockhawk.data;

import com.project.denail.stockhawk.TimeManip;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by denail on 17/08/27.
 */

@Table(database = Database.class)
public class DataUpdate extends BaseModel {
    @PrimaryKey @Column String title;
    @Column String lastUpdate;

    public DataUpdate() {}

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

    public static List<DataUpdate> getUnUpdatedData() {
        String todayDate = TimeManip.getTodayDate();
        List<DataUpdate> unUpdatedData = new ArrayList<>();
        List<DataUpdate> dataUpdateList = new Select()
                .from(DataUpdate.class)
                .queryList();
        for(DataUpdate dataUpdate : dataUpdateList) {
            if(!dataUpdate.getLastUpdate().equals(todayDate)) {
                unUpdatedData.add(dataUpdate);
            }
        }
        return unUpdatedData;
    }

    public static void deleteData(String dataTitle) {
        DataUpdate data = new Select()
                .from(DataUpdate.class)
                .where(DataUpdate_Table.title.eq(dataTitle))
                .querySingle();
        if(data != null) {
            data.delete();
        }
    }
}
