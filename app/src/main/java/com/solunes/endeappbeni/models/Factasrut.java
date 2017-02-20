package com.solunes.endeappbeni.models;

import android.database.Cursor;

/**
 * Created by jhonlimaster on 15-02-17.
 */

public class Factasrut {
    private int id;
    private int empresaId;
    private int rutaId;
    private double porcentaje;
    private int estado;
    private String descripcion;
    private boolean aseo;

    public enum Columns {
        id,
        empresa_id,
        ruta,
        porcentaje,
        estado,
        descripcion,
        aseo
    }

    public static Factasrut fromCursor(Cursor cursor) {
        Factasrut factasrut = new Factasrut();
        factasrut.setId(cursor.getInt(Columns.id.ordinal()));
        factasrut.setEmpresaId(cursor.getInt(Columns.empresa_id.ordinal()));
        factasrut.setRutaId(cursor.getInt(Columns.ruta.ordinal()));
        factasrut.setPorcentaje(cursor.getDouble(Columns.porcentaje.ordinal()));
        factasrut.setDescripcion(cursor.getString(Columns.descripcion.ordinal()));
        factasrut.setAseo(cursor.getInt(Columns.aseo.ordinal()) == 1);
        return factasrut;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(int empresaId) {
        this.empresaId = empresaId;
    }

    public int getRutaId() {
        return rutaId;
    }

    public void setRutaId(int rutaId) {
        this.rutaId = rutaId;
    }

    public double getPorcentaje() {
        return porcentaje;
    }

    public void setPorcentaje(double porcentaje) {
        this.porcentaje = porcentaje;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public boolean isAseo() {
        return aseo;
    }

    public void setAseo(boolean aseo) {
        this.aseo = aseo;
    }
}
