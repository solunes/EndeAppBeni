package com.solunes.endeappbeni.models;

import android.database.Cursor;

/**
 * Created by jhonlimaster on 11-10-16.
 */

public class PrintObs {
    private int id;
    private String obiDes;
    private int obiAuto;

    public enum Columns {
        id, ObiDes, ObiAut
    }

    public static PrintObs fromCursor(Cursor cursor) {
        PrintObs printObs = new PrintObs();
        printObs.setId(cursor.getInt(Columns.id.ordinal()));
        printObs.setObiDes(cursor.getString(Columns.ObiDes.ordinal()));
        printObs.setObiAut(cursor.getInt(Columns.ObiAut.ordinal()));
        return printObs;
    }

    public int getObiAut() {
        return obiAuto;
    }

    public void setObiAut(int obiAuto) {
        this.obiAuto = obiAuto;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getObiDes() {
        return obiDes;
    }

    public void setObiDes(String obiDes) {
        this.obiDes = obiDes;
    }
}
