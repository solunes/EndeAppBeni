package com.solunes.endeappbeni.models;

import android.database.Cursor;

/**
 * Created by jhonlimaster on 11-08-16.
 */
public class User {
    private int lecId;
    private String lecNom;
    private String lecCod;
    private String lecPas;
    private int lecNiv;
    private int lecAsi;
    private int lecAct;
    private int areaCod;

    public enum Columns {
        LecId, LecNom, LecCod, LecPas, LecNiv, LecAsi, LecAct, AreaCod
    }

    public static User fromCursor(Cursor cursor){
        User user = new User();
        user.setLecId(cursor.getInt(Columns.LecId.ordinal()));
        user.setLecNom(cursor.getString(Columns.LecNom.ordinal()));
        user.setLecCod(cursor.getString(Columns.LecCod.ordinal()));
        user.setLecPas(cursor.getString(Columns.LecPas.ordinal()));
        user.setLecNiv(cursor.getInt(Columns.LecNiv.ordinal()));
        user.setLecAsi(cursor.getInt(Columns.LecAsi.ordinal()));
        user.setLecAct(cursor.getInt(Columns.LecAct.ordinal()));
        user.setAreaCod(cursor.getInt(Columns.AreaCod.ordinal()));
        return user;
    }

    public int getLecId() {
        return lecId;
    }

    public void setLecId(int lecId) {
        this.lecId = lecId;
    }

    public String getLecNom() {
        return lecNom;
    }

    public void setLecNom(String lecNom) {
        this.lecNom = lecNom;
    }

    public String getLecCod() {
        return lecCod;
    }

    public void setLecCod(String lecCod) {
        this.lecCod = lecCod;
    }

    public String getLecPas() {
        return lecPas;
    }

    public void setLecPas(String lecPas) {
        this.lecPas = lecPas;
    }

    public int getLecNiv() {
        return lecNiv;
    }

    public void setLecNiv(int lecNiv) {
        this.lecNiv = lecNiv;
    }

    public int getLecAsi() {
        return lecAsi;
    }

    public void setLecAsi(int lecAsi) {
        this.lecAsi = lecAsi;
    }

    public int getLecAct() {
        return lecAct;
    }

    public void setLecAct(int lecAct) {
        this.lecAct = lecAct;
    }

    public int getAreaCod() {
        return areaCod;
    }

    public void setAreaCod(int areaCod) {
        this.areaCod = areaCod;
    }
}
