package com.project.denail.stockhawk;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by denail on 17/08/28.
 */

public class TimeManip {

    public static Long genId() {
        return (new GregorianCalendar()).getTimeInMillis();
    }

    public static String getTodayDate() {
        GregorianCalendar calendar = new GregorianCalendar();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        String date = "";
        if(day < 10) {
            date = date + "0";
        }
        date = date + String.valueOf(day);
        if(month < 10) {
            date = date + "0";
        }
        date = date + String.valueOf(month);
        date = date + String.valueOf(year);
        return date;
    }
}
