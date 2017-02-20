package com.solunes.endeappbeni.models;

import android.database.Cursor;

/**
 * Created by jhonlimaster on 15-02-17.
 */

public class Factasascateg {
    private int id;
    private int empresaId;
    private int lineaId;
    private int categoriaId;
    private String descripcion;
    private int rangoInicial;
    private int rangoFinal;
    private int importe;
    private int estado;

    public enum Columns {
        id,
        empresa_id,
        linea_id,
        categoria_id,
        descripcion,
        rango_inicial,
        rango_final,
        importe,
        estado
    }

    public static Factasascateg fromCursor(Cursor cursor) {
        Factasascateg factasascateg = new Factasascateg();
        factasascateg.setId(cursor.getInt(Columns.id.ordinal()));
        factasascateg.setEmpresaId(cursor.getInt(Columns.empresa_id.ordinal()));
        factasascateg.setLineaId(cursor.getInt(Columns.linea_id.ordinal()));
        factasascateg.setCategoriaId(cursor.getInt(Columns.categoria_id.ordinal()));
        factasascateg.setDescripcion(cursor.getString(Columns.descripcion.ordinal()));
        factasascateg.setRangoInicial(cursor.getInt(Columns.rango_inicial.ordinal()));
        factasascateg.setRangoFinal(cursor.getInt(Columns.rango_final.ordinal()));
        factasascateg.setImporte(cursor.getInt(Columns.importe.ordinal()));
        factasascateg.setEstado(cursor.getInt(Columns.estado.ordinal()));
        return factasascateg;
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

    public int getLineaId() {
        return lineaId;
    }

    public void setLineaId(int lineaId) {
        this.lineaId = lineaId;
    }

    public int getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(int categoriaId) {
        this.categoriaId = categoriaId;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getRangoInicial() {
        return rangoInicial;
    }

    public void setRangoInicial(int rangoInicial) {
        this.rangoInicial = rangoInicial;
    }

    public int getRangoFinal() {
        return rangoFinal;
    }

    public void setRangoFinal(int rangoFinal) {
        this.rangoFinal = rangoFinal;
    }

    public int getImporte() {
        return importe;
    }

    public void setImporte(int importe) {
        this.importe = importe;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }
}
