package com.project.denail.stockhawk.data;

import android.util.Log;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.data.Blob;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by denail on 17/08/26.
 */

@Table(database = Database.class)
public class DataStock extends BaseModel {
    @PrimaryKey @Column private String title;
    @Column private long id;
    @Column private float lastDate;
    @Column private Blob valuesBlob;

    public DataStock() {
        this.title = "";
        this.valuesBlob = null;
        this.lastDate = 0f;
    }

    public DataStock(long id, String title, List<Float> valuesBlob, float lastDate) {
        this.id = id;
        this.title = title;
        this.lastDate = lastDate;
        setValues(valuesBlob);
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

    public Blob getValuesBlob() {
        return valuesBlob;
    }

    public void setValues(List<Float> valuesBlob) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(valuesBlob);
            oos.close();
            this.valuesBlob = new Blob(baos.toByteArray());
        } catch (IOException e) {
            Log.e(getClass().getSimpleName(), e.toString());
            this.valuesBlob = null;
        }
    }

    public void setValuesBlob(Blob valuesBlob) {
        this.valuesBlob = valuesBlob;
    }

    public ArrayList<Float> getValues() {
        ByteArrayInputStream bais = new ByteArrayInputStream(valuesBlob.getBlob());
        try {
            ObjectInputStream ois = new ObjectInputStream(bais);
            Object object = ois.readObject();
            ArrayList<Float> arrValues = (ArrayList<Float>) object;
            return arrValues;
        } catch (ClassCastException | IOException | ClassNotFoundException e) {
            Log.e(getClass().getSimpleName(), e.toString());
        }

        return null;
    }

    public float getLastDate() {
        return lastDate;
    }

    public void setLastDate(float lastDate) {
        this.lastDate = lastDate;
    }
}


