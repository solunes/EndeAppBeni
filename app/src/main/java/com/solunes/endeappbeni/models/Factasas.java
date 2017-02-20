package com.solunes.endeappbeni.models;

import android.database.Cursor;

/**
 * Created by jhonlimaster on 15-02-17.
 */

public class Factasas {
    private int id;
    private int empresaId;
    private int servicioID;
    private String descripcion;
    private double porcentaje;
    private int estado;

    public enum Columns {
        id,
        empresa_id,
        servicio_id,
        descripcion,
        porcentaje,
        estado
    }

    public static Factasas fromCursor(Cursor cursor) {
        Factasas factasas = new Factasas();
        factasas.setId(cursor.getInt((Columns.id.ordinal())));
        factasas.setEmpresaId(cursor.getInt((Columns.empresa_id.ordinal())));
        factasas.setServicioID(cursor.getInt((Columns.servicio_id.ordinal())));
        factasas.setDescripcion(cursor.getString((Columns.descripcion.ordinal())));
        factasas.setPorcentaje(cursor.getInt((Columns.porcentaje.ordinal())));
        factasas.setEstado(cursor.getInt((Columns.estado.ordinal())));
        return factasas;
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

    public int getServicioID() {
        return servicioID;
    }

    public void setServicioID(int servicioID) {
        this.servicioID = servicioID;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
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
}
