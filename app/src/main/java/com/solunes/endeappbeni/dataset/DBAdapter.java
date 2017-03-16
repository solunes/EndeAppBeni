package com.solunes.endeappbeni.dataset;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.util.Pair;

import com.solunes.endeappbeni.activities.MainActivity;
import com.solunes.endeappbeni.fragments.DataFragment;
import com.solunes.endeappbeni.models.DataModel;
import com.solunes.endeappbeni.models.DataObs;
import com.solunes.endeappbeni.models.DetalleFactura;
import com.solunes.endeappbeni.models.Factasas;
import com.solunes.endeappbeni.models.Factasascateg;
import com.solunes.endeappbeni.models.Factasrut;
import com.solunes.endeappbeni.models.FacturaDosificacion;
import com.solunes.endeappbeni.models.Historico;
import com.solunes.endeappbeni.models.ItemFacturacion;
import com.solunes.endeappbeni.models.LimitesMaximos;
import com.solunes.endeappbeni.models.MedEntreLineas;
import com.solunes.endeappbeni.models.Obs;
import com.solunes.endeappbeni.models.Parametro;
import com.solunes.endeappbeni.models.PrintObsData;
import com.solunes.endeappbeni.models.Resultados;
import com.solunes.endeappbeni.models.Tarifa;
import com.solunes.endeappbeni.models.User;
import com.solunes.endeappbeni.utils.Encrypt;
import com.solunes.endeappbeni.utils.GenLecturas;
import com.solunes.endeappbeni.utils.StatisticsItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Esta clase contiene funciones de consulta a la base de datos
 */
public class DBAdapter {

    private static final String TAG = "DBAdapter";

    private DBHelper dbHelper;
    private SQLiteDatabase db;

    public DBAdapter(Context context) {
        dbHelper = new DBHelper(context);
    }

    public DBAdapter open() throws SQLException {
        db = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    /**
     * Metodo para verificar la existencia del usuario
     *
     * @param username nombre de usuario
     * @param password contraseña del usuario
     * @return retorna un cursor para su posterior manejo
     */
    public Cursor checkUser(String seed, String username, String password) {
        open();
        // TODO: 25-11-16 encriptacion
        try {
            password = Encrypt.encrypt(seed, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Cursor cursor = db.query(DBHelper.USER_TABLE, null,
                User.Columns.LecCod.name() + " = '" + username + "' AND " + User.Columns.LecPas.name() + " = '" + password + "'",
                null, null, null, null, null);
        cursor.moveToFirst();

        return cursor;
    }

    /**
     * Metodo para obtener un usuario de la base de datos
     *
     * @param id LecId del usuario
     * @return Un objeto usuario
     */
    public User getUser(int id) {
        open();
        Cursor query = db.query(DBHelper.USER_TABLE, null, User.Columns.LecId.name() + " = " + id, null, null, null, null);
        query.moveToFirst();
        User user = User.fromCursor(query);
        query.close();
        return user;
    }

    /**
     * Metodo para eliminar tablas cuando descargan parametros fijos
     * Su proposito es eliminar datos anteriores y bajar los nuevos
     */
    public void clearTables() {
        open();
        db.delete(DBHelper.OBS_TABLE, null, null);
        db.delete(DBHelper.USER_TABLE, null, null);
        db.delete(DBHelper.TARIFA_TABLE, null, null);
        db.delete(DBHelper.ITEM_FACTURACION_TABLE, null, null);
        db.delete(DBHelper.PARAMETRO_TABLE, null, null);
        db.delete(DBHelper.PRINT_OBS_TABLE, null, null);
        db.delete(DBHelper.PRINT_OBS_DATA_TABLE, null, null);
        db.delete(DBHelper.LIMITES_MAXIMOS_TABLE, null, null);
    }

    /**
     * Metodo que elimina tablas cuando se cambia de usuario
     */
    public void clearTablesNoUser() {
        open();
        db.delete(DBHelper.DATA_TABLE, null, null);
        db.delete(DBHelper.HISTORICO_TABLE, null, null);
        db.delete(DBHelper.MED_ENTRE_LINEAS_TABLE, null, null);
        db.delete(DBHelper.DATA_OBS_TABLE, null, null);
        db.delete(DBHelper.PRINT_OBS_DATA_TABLE, null, null);
        db.delete(DBHelper.DETALLE_FACTURA_TABLE, null, null);
    }

    /**
     * Metodo que elimina tablas antes de descargar datos de lecturas
     */
    public void beforeDownloadData() {
        open();
        db.delete(DBHelper.DATA_TABLE, null, null);
        db.delete(DBHelper.DATA_OBS_TABLE, null, null);
        db.delete(DBHelper.PRINT_OBS_DATA_TABLE, null, null);
        db.delete(DBHelper.MED_ENTRE_LINEAS_TABLE, null, null);
        db.delete(DBHelper.DETALLE_FACTURA_TABLE, null, null);
        db.delete(DBHelper.HISTORICO_TABLE, null, null);
        db.delete(DBHelper.RESULTADOS_TABLE, null, null);
    }

    /**
     * Guarda un nuevo registro en la base de datos
     *
     * @param table  nombre de la tabla a la que se van a guardar datos
     * @param values los valores que se van a guardar
     */
    public void saveObject(String table, ContentValues values) {
        open();
        db.insert(table, null, values);
    }

    /**
     * Metodo para actualizar un registro de data_table
     *
     * @param client        id del data_table
     * @param contentValues valores que se van a guardar
     */
    public void updateData(int client, ContentValues contentValues) {
        open();
        db.update(DBHelper.DATA_TABLE, contentValues, DataModel.Columns.id.name() + " = " + client, null);
    }

    /**
     * Metodo para obtener todos los datos
     *
     * @return Una lista con todos los datos ordenados ascendentemente
     */
    public ArrayList<DataModel> getAllData() {
        open();
        ArrayList<DataModel> dataModels = new ArrayList<>();
        Cursor query = db.query(DBHelper.DATA_TABLE, null, null, null, null, null, DataModel.Columns.TlxOrdTpl.name() + " ASC");
        while (query.moveToNext()) {
            dataModels.add(DataModel.fromCursor(query));
        }
        query.close();
        return dataModels;
    }

    public Resultados getDataRes(int dataId) {
        open();
        Cursor query = db.query(DBHelper.RESULTADOS_TABLE, null, Resultados.Columns.general_id.name() + " = " + dataId, null, null, null, null);
        query.moveToFirst();
        Resultados resultados = null;
        if (query.getCount() > 0) {
            resultados = Resultados.fromCursor(query);
        }
        query.close();
        return resultados;
    }

    /**
     * Metodo que devuelve todos los datos que tengan estado de lectura 1
     * o estado de lectura 2, ordenados ascendentemente
     *
     * @return Una lista con todos los datos ordenados ascendentemente
     */
    public ArrayList<DataModel> getReady() {
        open();
        ArrayList<DataModel> dataModels = new ArrayList<>();
        Cursor query = db.query(DBHelper.DATA_TABLE, null,
                DataModel.Columns.estado_lectura.name() + " = 1 " +
                        "OR " + DataModel.Columns.estado_lectura.name() + " = 2",
                null, null, null, DataModel.Columns.TlxOrdTpl.name() + " ASC");
        while (query.moveToNext()) {
            dataModels.add(DataModel.fromCursor(query));
        }
        query.close();
        return dataModels;
    }

    /**
     * Metodo que devuelve datos que tengan cierto tipo de estado de lectura
     *
     * @param state Es el tipo de lectura que va venir como parametro para la consulta
     * @return retorna una lista de DataModel
     */
    public ArrayList<DataModel> getState(int state) {
        open();
        ArrayList<DataModel> dataModels = new ArrayList<>();
        Cursor query = db.query(DBHelper.DATA_TABLE, null,
                DataModel.Columns.estado_lectura.name() + " = " + state,
                null, null, null, DataModel.Columns.TlxImpAvi.name() + " ASC");
        while (query.moveToNext()) {
            dataModels.add(DataModel.fromCursor(query));
        }
        query.close();
        return dataModels;
    }

    /**
     * Metodo para obtener un solo dato
     *
     * @param idData id del dato
     * @return retorna un objeto DataModel
     */
    public DataModel getData(int idData) {
        open();
        Cursor query = db.query(DBHelper.DATA_TABLE, null, DataModel.Columns.id.name() + " = " + idData, null, null, null, null);
        query.moveToNext();
        DataModel dataModel = DataModel.fromCursor(query);
        query.close();
        return dataModel;
    }

    /**
     * Obtiene el primer data
     *
     * @return Retorna un objeto DataModel
     */
    public DataModel getFirstData() {
        open();
        Cursor query = db.query(DBHelper.DATA_TABLE, null, null, null, null, null, null);
        query.moveToFirst();
        DataModel dataModel = DataModel.fromCursor(query);
        query.close();
        return dataModel;
    }

    /**
     * Devuelve la cantidad de registros del data_table
     */
    public int getSizeData() {
        open();
        Cursor query = db.rawQuery("select count(*) from " + DBHelper.DATA_TABLE, null);
        query.moveToNext();
        int size = query.getInt(0);
        query.close();
        return size;
    }

    public int getSizeUser() {
        open();
        Cursor query = db.rawQuery("select count(*) from " + DBHelper.USER_TABLE, null);
        query.moveToNext();
        int size = query.getInt(0);
        query.close();
        return size;
    }

    /**
     * Devuelve la cantidad de datos guardados
     */
    public int getCountSave() {
        open();
        Cursor cursor = db.rawQuery("select count(*) from " + DBHelper.DATA_TABLE + " " +
                "where " + DataModel.Columns.estado_lectura.name() + " = 1 OR " +
                DataModel.Columns.estado_lectura.name() + " = 2", null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    /**
     * Devuelve la cantidad de datos impresos o leidos
     */
    public int getCountPrinted() {
        open();
        Cursor query = db.query(DBHelper.DATA_TABLE, null, DataModel.Columns.estado_lectura.name() + " = 1", null, null, null, null);
        int count = query.getCount();
        query.close();
        return count;
    }

    /**
     * Devuelve la cantidad de datos postergados
     */
    public int getCountPostponed() {
        open();
        Cursor query = db.query(DBHelper.DATA_TABLE, null, DataModel.Columns.estado_lectura.name() + " = 2", null, null, null, null);
        int count = query.getCount();
        query.close();
        return count;
    }

    /**
     * Obtiene todas las observaciones para seleccionar
     */
    public Cursor getObs() {
        open();
        Cursor query = db.query(DBHelper.OBS_TABLE, null, Obs.Columns.ObsAut.name() + " = 0", null, null, null, null);
        return query;
    }

    /**
     * Obtiene una obervaciones apartir de su id
     */
    public Cursor getObs(int obsCod) {
        open();
        Cursor query = db.query(DBHelper.OBS_TABLE, null, Obs.Columns.id.name() + " = " + obsCod, null, null, null, null);
        query.moveToNext();
        return query;
    }

    /**
     * Obtiene una obervacion apartir de su decripcion
     */
    public Cursor getObs(String desc) {
        open();
        Cursor query = db.query(DBHelper.OBS_TABLE, null, Obs.Columns.ObsDes.name() + " = '" + desc + "'", null, null, null, null);
        query.moveToNext();
        return query;
    }

    /**
     * Metodo que obtiene una lista de tipo DataObs
     *
     * @param data Es el id data para obtener la lista
     */
    public ArrayList<DataObs> getDataObsByCli(int data) {
        open();
        ArrayList<DataObs> objects = new ArrayList<>();
        Cursor cursor = db.query(DBHelper.DATA_OBS_TABLE, null, DataObs.Columns.general_id.name() + " = " + data, null, null, null, null);
        while (cursor.moveToNext()) {
            objects.add(DataObs.fromCursor(cursor));
        }
        cursor.close();
        return objects;
    }

    /**
     * Obtiene una observacion
     *
     * @param data Es el id data para para buscar la observacion
     * @return
     */
    public Obs getObsByCli(int data) {
        open();
        Cursor cursor = db.query(DBHelper.DATA_OBS_TABLE, null,
                DataObs.Columns.general_id.name() + " = " + data + " AND " +
                        "NOT " + DataObs.Columns.observacion_id.name() + " = 80 AND " +
                        "NOT " + DataObs.Columns.observacion_id.name() + " = 81",
                null, null, null, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            Cursor cursorObs = db.query(DBHelper.OBS_TABLE, null,
                    Obs.Columns.id.name() + " = " + cursor.getInt(DataObs.Columns.observacion_id.ordinal()),
                    null, null, null, null);
            cursorObs.moveToFirst();
            Obs obs = Obs.fromCursor(cursorObs);
            Log.e(TAG, "getObsByCli: " + obs.getObsDes());
            cursor.close();
            cursorObs.close();
            return obs;
        } else {
            Log.e(TAG, "getObsByCli: " + 0);
            cursor.close();
            return null;
        }
    }

    /**
     * Obtiene los rangos de cargo de enercia para cierta categoria
     * que estan en el rangp: 1 < x < 8
     */
    public ArrayList<Tarifa> getCargoEnergia(int categoria) {
        open();
        Cursor query = db.query(DBHelper.TARIFA_TABLE,
                null, Tarifa.Columns.categoria_tarifa_id.name() + " = " + categoria + "" +
                        " AND " + Tarifa.Columns.item_facturacion_id.name() + " > 1" +
                        " AND " + Tarifa.Columns.item_facturacion_id.name() + " < 8", null, null,
                null, Tarifa.Columns.kwh_desde.name() + " ASC");
        ArrayList<Tarifa> arrayList = new ArrayList<>();
        while (query.moveToNext()) {
            arrayList.add(Tarifa.fromCursor(query));
        }
        query.close();
        return arrayList;
    }

    /**
     * Metodo para buscar en data_table por numero de cliente o numero de medidor
     *
     * @param filter       filtro de busqueda
     * @param isCli        booleano para que se busque entre cliente o medidor
     * @param currentState un filtro extra para obtener un grupo de estados
     * @return retorna on cursor para su porterior manipulacion
     */
    public Cursor searchClienteMedidor(String filter, boolean isCli, int currentState) {
        open();
        String query;
        if (isCli) {
            query = DataModel.Columns.TlxCli.name() + " = '" + filter + "'";
        } else {
            query = DataModel.Columns.TlxNroMed.name() + " = '" + filter + "'";
        }
        if (currentState >= 0) {
            query = query + " AND ";
            switch (currentState) {
                case MainActivity.KEY_READY:
                    query = query + DataModel.Columns.estado_lectura.name() + " = 1 " +
                            "OR " + DataModel.Columns.estado_lectura.name() + " = 2";
                    break;
                case MainActivity.KEY_MISSING:
                    query = query + DataModel.Columns.estado_lectura.name() + " = 0";
                    break;
                case MainActivity.KEY_PRINT:
                    query = query + DataModel.Columns.estado_lectura.name() + " = 1";
                    break;
                case MainActivity.KEY_POSTPONED:
                    query = query + DataModel.Columns.estado_lectura.name() + " = 2";
                    break;
            }
        }
        Cursor cursor = db.query(DBHelper.DATA_TABLE, null, query, null, null, null, null);
        cursor.moveToNext();
        return cursor;
    }

    /**
     * Obtiene el valor de un parametro apartir de su codigo
     */
    public double getParametroValor(String codigo) {
        open();
        Cursor query = db.query(DBHelper.PARAMETRO_TABLE, null, Parametro.Columns.codigo.name() + " = '" + codigo + "'", null, null, null, null);
        query.moveToNext();
        int valor = query.getInt(Parametro.Columns.valor.ordinal());
        query.close();
        return valor;
    }

    /**
     * Obtiene el texto de un parametro apartir de su codigo
     */
    public String getParametroTexto(String codigo) {
        open();
        Cursor query = db.query(DBHelper.PARAMETRO_TABLE, null, Parametro.Columns.codigo.name() + " = '" + codigo + "'", null, null, null, null);
        query.moveToNext();
        String texto = query.getString(Parametro.Columns.texto.ordinal());
        query.close();
        return texto;
    }

    /**
     * Obtiene el importe de un cargo fijo de cierta categoria
     */
    public double getCargoFijo(int categoria) {
        open();
        Cursor cursor = db.query(DBHelper.TARIFA_TABLE, null, Tarifa.Columns.categoria_tarifa_id.name() + " = " + categoria
                + " AND " + Tarifa.Columns.item_facturacion_id.name() + " = 1", null, null, null, null);
        if (cursor.getCount() > 0) {
            cursor.moveToNext();
            double importe = cursor.getDouble(Tarifa.Columns.importe.ordinal());
            cursor.close();
            return importe;
        }
        cursor.close();
        return 0;
    }

    /**
     * Metodo que obtiene el cargo fijo de cierta categoria
     *
     * @return retorna el campo kwh_hasta del cargo fijo para el primer descuento
     */
    public int getCargoFijoDescuento(int categoria) {
        open();
        Cursor cursor = db.query(DBHelper.TARIFA_TABLE, null, Tarifa.Columns.categoria_tarifa_id.name() + " = " + categoria
                + " AND " + Tarifa.Columns.item_facturacion_id.name() + " = 1", null, null, null, null);
        if (cursor.getCount() > 0) {
            cursor.moveToNext();
            int kwhHasta = cursor.getInt(Tarifa.Columns.kwh_hasta.ordinal());
            cursor.close();
            return kwhHasta;
        }
        cursor.close();
        return 0;
    }

    /**
     * Metodo para obtener las estadisticas item con dos tipos de consulta diferente para cada tipo
     *
     * @return una lista de StatisticsItem dependiendo del param
     */
    public List<StatisticsItem> getSt(int param) {
        open();
        ArrayList<StatisticsItem> items = new ArrayList<>();
        if (param == 1) {
            Cursor cursor = db.query(DBHelper.DATA_TABLE, new String[]{"TlxTipLec", "count(TlxTipLec)"}, DataModel.Columns.estado_lectura.name() + " = 1 " +
                    "OR " + DataModel.Columns.estado_lectura.name() + " = 2", null, "TlxTipLec", null, null);
            while (cursor.moveToNext()) {
                items.add(new StatisticsItem(DataModel.getTipoLectura(cursor.getInt(0)), cursor.getInt(1)));
            }
            cursor.close();
        }
        if (param == 2) {
            Cursor cursor = db.rawQuery("select ot.ObsDes, count(ot.id)as cantidad from data_obs_table as dot join obs_table as ot " +
                    "where dot.observacion_id = ot.id " +
                    "group by ot.ObsDes, ot.id", null);
            while (cursor.moveToNext()) {
                items.add(new StatisticsItem(cursor.getString(0), cursor.getInt(1)));
            }
            cursor.close();
        }
        return items;
    }

    /**
     * Obtiene todas las observaciones de impresion
     */
    public Cursor getPrintObs() {
        open();
        Cursor cursor = db.query(DBHelper.PRINT_OBS_TABLE, null, null, null, null, null, null);
        return cursor;
    }

    /**
     * Obtiene una lista de observaciones de impresion de un dato
     */
    public ArrayList<PrintObsData> getPrintObsData(int idData) {
        open();
        ArrayList<PrintObsData> printObsDatas = new ArrayList<>();
        Cursor cursor = db.query(DBHelper.PRINT_OBS_DATA_TABLE, null, PrintObsData.Columns.general_id.name() + " = " + idData, null, null, null, null);
        while (cursor.moveToNext()) {
            printObsDatas.add(PrintObsData.fromCursor(cursor));
        }
        cursor.close();
        return printObsDatas;
    }

    /**
     * Obtiene una lista de detalles factura de cierto data
     */
    public ArrayList<DetalleFactura> getDetalleFactura(int idData) {
        open();
        ArrayList<DetalleFactura> detalleFacturas = new ArrayList<>();
        Cursor cursor = db.query(DBHelper.DETALLE_FACTURA_TABLE, null, DetalleFactura.Columns.general_id.name() + " = " + idData, null, null, null, null);
        while (cursor.moveToNext()) {
            detalleFacturas.add(DetalleFactura.fromCursor(cursor));
        }
        cursor.close();
        return detalleFacturas;
    }

    /**
     * Obtiene el importe de un objeto DetalleFactura apartir del id data y el id item
     */
    public double getDetalleFacturaImporte(int idData, int idItem) {
        open();
        Cursor cursor = db.query(DBHelper.DETALLE_FACTURA_TABLE, null,
                DetalleFactura.Columns.general_id.name() + " = " + idData + " AND " +
                        DetalleFactura.Columns.item_facturacion_id.name() + " = " + idItem, null, null, null, null);
        cursor.moveToNext();
        if (cursor.getCount() > 0) {
            return DetalleFactura.fromCursor(cursor).getImporte();
        }
        cursor.close();
        return 0;
    }

    /**
     * Obtiene un objeto DetalleFactura apartir de un id data y un id item
     */
    public DetalleFactura getDetalleFactura(int idData, int idItem) {
        open();
        Cursor cursor = db.query(DBHelper.DETALLE_FACTURA_TABLE, null,
                DetalleFactura.Columns.general_id.name() + " = " + idData + " AND " +
                        DetalleFactura.Columns.item_facturacion_id.name() + " = " + idItem, null, null, null, null);
        cursor.moveToNext();
        if (cursor.getCount() > 0) {
            return DetalleFactura.fromCursor(cursor);
        }
        cursor.close();
        return null;
    }

    /**
     * Metodo que crea un string array para las leyendas de impresion
     */
    public String[] getLeyenda() {
        open();
        String[] leyenda = new String[3];
        Cursor query = db.query(DBHelper.PARAMETRO_TABLE, null, Parametro.Columns.codigo.name() + " = '" + Parametro.Values.leyenda_1.name() + "'", null, null, null, null);
        query.moveToNext();
        leyenda[0] = Parametro.fromCursor(query).getTexto();
        query = db.query(DBHelper.PARAMETRO_TABLE, null, Parametro.Columns.codigo.name() + " = '" + Parametro.Values.leyenda_2.name() + "'", null, null, null, null);
        query.moveToNext();
        leyenda[1] = Parametro.fromCursor(query).getTexto();
        query = db.query(DBHelper.PARAMETRO_TABLE, null, Parametro.Columns.codigo.name() + " = '" + Parametro.Values.leyenda_3.name() + "'", null, null, null, null);
        query.moveToNext();
        leyenda[2] = Parametro.fromCursor(query).getTexto();
        query.close();
        return leyenda;
    }

    public String[] getLeyendaCobro() {
        open();
        String[] leyenda = new String[4];
        Cursor query = db.query(DBHelper.PARAMETRO_TABLE, null, Parametro.Columns.codigo.name() + " = '" + Parametro.Values.leyenda_1.name() + "'", null, null, null, null);
        query.moveToNext();
        leyenda[0] = Parametro.fromCursor(query).getTexto();
        query = db.query(DBHelper.PARAMETRO_TABLE, null, Parametro.Columns.codigo.name() + " = '" + Parametro.Values.leyenda_2.name() + "'", null, null, null, null);
        query.moveToNext();
        leyenda[1] = Parametro.fromCursor(query).getTexto();
        query = db.query(DBHelper.PARAMETRO_TABLE, null, Parametro.Columns.codigo.name() + " = '" + Parametro.Values.leyenda_3.name() + "'", null, null, null, null);
        query.moveToNext();
        leyenda[2] = Parametro.fromCursor(query).getTexto();
        query = db.query(DBHelper.PARAMETRO_TABLE, null, Parametro.Columns.codigo.name() + " = '" + Parametro.Values.leyenda_4.name() + "'", null, null, null, null);
        query.moveToNext();
        leyenda[3] = Parametro.fromCursor(query).getTexto();
        query.close();
        return leyenda;
    }

    public boolean validNewMedidor(int nroMed) {
        open();
        Cursor cursor1 = db.query(DBHelper.DATA_TABLE, null, DataModel.Columns.TlxNroMed.name() + " = " + nroMed, null, null, null, null);
        if (cursor1.getCount() > 0) {
            cursor1.close();
            return false;
        }
        Cursor cursor2 = db.query(DBHelper.MED_ENTRE_LINEAS_TABLE, null, MedEntreLineas.Columns.MelMed.name() + " = " + nroMed, null, null, null, null);
        if (cursor2.getCount() > 0) {
            cursor2.close();
            return false;
        }
        return true;
    }

    public ArrayList<MedEntreLineas> getMedEntreLineas() {
        open();
        ArrayList<MedEntreLineas> entreLineases = new ArrayList<>();
        Cursor cursor = db.query(DBHelper.MED_ENTRE_LINEAS_TABLE, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            entreLineases.add(MedEntreLineas.fromCursor(cursor));
        }
        cursor.close();
        return entreLineases;
    }

    public Historico getHistorico(int idData) {
        open();
        Cursor cursor = db.query(DBHelper.HISTORICO_TABLE, null, Historico.Columns.general_id.name() + " = " + idData, null, null, null, null);
        cursor.moveToNext();
        if (cursor.getCount() > 0) {
            return Historico.fromCursor(cursor);
        }
        cursor.close();
        return null;
    }

    public double getCargoPotencia(int categoria) {
        open();
        Cursor cursor = db.query(DBHelper.TARIFA_TABLE, null, Tarifa.Columns.categoria_tarifa_id + " = " + categoria
                + " AND " + Tarifa.Columns.item_facturacion_id + " = 41", null, null, null, null);
        cursor.moveToNext();
        if (cursor.getCount() > 0) {
            double importe = cursor.getDouble(Tarifa.Columns.importe.ordinal());
            cursor.close();
            return importe;
        }
        cursor.close();
        return -1;
    }

    public String getLlaveDosificacion(int are) {
        open();
        Cursor cursor = db.query(DBHelper.FACTURA_DOSIFICACION_TABLE, null,
                FacturaDosificacion.Columns.area_id + " = " + are, null, null, null, null);
        cursor.moveToFirst();
        String llaveDosificacion = cursor.getString(FacturaDosificacion.Columns.llave_dosificacion.ordinal());
        cursor.close();
        return llaveDosificacion;
    }

    public Pair<Double, Integer> getValorTAP(int factarutaActual, double importeConsumo) {
        double factaRUPorcent = 0;
        int factaRuAseo = -1;
        double importeTAP = 0;
        open();
        Cursor cursor = db.query(DBHelper.FACTASAS_TABLE, null, Factasas.Columns.servicio_id.name() + " = 1",
                null, null, null, null);
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            factaRUPorcent = cursor.getDouble(Factasas.Columns.porcentaje.ordinal());
        }

        cursor = db.query(DBHelper.FACTASRUT_TABLE, null, Factasrut.Columns.ruta.name() + " =" + factarutaActual,
                null, null, null, null);
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            factaRuAseo = cursor.getInt(Factasrut.Columns.aseo.ordinal());
            factaRUPorcent = cursor.getDouble(Factasrut.Columns.porcentaje.ordinal());
        }

        importeTAP = GenLecturas.roundDecimal(importeConsumo * factaRUPorcent,1);

        if (getParametroValor(Parametro.Values.factura_aseo.name()) > 0) {
            factaRuAseo = -1;
        }

        cursor.close();
        return Pair.create(importeTAP, factaRuAseo);
    }

    public double getImporteAseo(int conRuta, int categoria, int consumoFacturado, double importeConsumo) {
        double importeTas = 0;
        double factaRUPorcent = 0;
        double factaRUPorcent2 = 1;

        int factura_aseo_area = (int) getParametroValor(Parametro.Values.factura_aseo.name());
        int factaruaseo = 0;

        open();
        Cursor cursor = db.query(DBHelper.FACTASAS_TABLE, null, Factasas.Columns.servicio_id.name() + " = 2",
                null, null, null, null);
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            factaRUPorcent = cursor.getDouble(Factasas.Columns.porcentaje.ordinal());
        }
        if (factura_aseo_area > 0) {
            cursor = db.query(DBHelper.FACTASRUT_TABLE, null, Factasrut.Columns.ruta.name() + " = " + conRuta,
                    null, null, null, null);
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                factaRUPorcent2 = cursor.getDouble(Factasrut.Columns.porcentaje.ordinal());
                factaruaseo = cursor.getInt(Factasrut.Columns.aseo.ordinal());
            }
        }
        cursor.close();
        open();
        if (factura_aseo_area > 0 && factaRUPorcent2 == 0) {
            importeTas = 0;
        } else {
            if (factaRUPorcent == 0) {
                if (factura_aseo_area > 0 && factaruaseo == 0) {
                    importeTas = 0;
                } else {
                    cursor = db.query(DBHelper.FACTASASCATEG_TABLE, null, Factasascateg.Columns.categoria_id.name() + " = " + categoria +
                            " AND " + Factasascateg.Columns.rango_inicial + " <= " + consumoFacturado +
                            " AND " + Factasascateg.Columns.rango_final + " >= " + consumoFacturado, null, null, null, null);
                    cursor.moveToFirst();
                    if (cursor.getCount() > 0) {
                        importeTas = cursor.getDouble(Factasascateg.Columns.importe.ordinal());
                    }
                }
            } else {
                importeTas = GenLecturas.roundDecimal(importeConsumo * factaRUPorcent,1);
            }
        }
        cursor.close();
        return importeTas;
    }

    public DataModel getLastSaved() {
        open();
        Cursor query = db.query(DBHelper.DATA_TABLE, null, "NOT " + DataModel.Columns.estado_lectura.name() + " = " + DataFragment.estados_lectura.Pendiente.ordinal(), null, null, null, DataModel.Columns.TlxOrdTpl.name() + " ASC");
        if (query.getCount() > 0) {
            query.moveToLast();
            DataModel dataModel = DataModel.fromCursor(query);
            query.close();
            return dataModel;
        }
        query.close();
        return null;
    }

    public void orderPendents(int idDataLas, int idDataCurrent) {
        open();
        Cursor cursor = db.query(DBHelper.DATA_TABLE, null, DataModel.Columns.estado_lectura.name() + " = " + DataFragment.estados_lectura.Pendiente.ordinal() +
                " AND " + DataModel.Columns.TlxOrdTpl.name() + " > " + idDataLas +
                " AND " + DataModel.Columns.TlxOrdTpl.name() + " < " + idDataCurrent, null, null, null, DataModel.Columns.TlxOrdTpl.name() + " ASC");
        Cursor allData = db.query(DBHelper.DATA_TABLE, null, null, null, null, null, DataModel.Columns.TlxOrdTpl.name() + " ASC");
        allData.moveToLast();
        int lastOrdTpl = DataModel.fromCursor(allData).getTlxOrdTpl();
        allData.close();
        while (cursor.moveToNext()) {
            lastOrdTpl++;
            ContentValues cv = new ContentValues();
            cv.put(DataModel.Columns.TlxOrdTpl.name(), lastOrdTpl);
            db.update(DBHelper.DATA_TABLE, cv, DataModel.Columns.id.name() + " = " + cursor.getInt(DataModel.Columns.id.ordinal()), null);
        }
        cursor.close();
    }

    public ArrayList<DataModel> getAllDataToSend() {
        open();
        ArrayList<DataModel> dataModels = new ArrayList<>();
        Cursor query = db.query(DBHelper.DATA_TABLE, null, "NOT " + DataModel.Columns.estado_lectura.name() + " = " + DataFragment.estados_lectura.Pendiente.ordinal() + " AND " +
                DataModel.Columns.enviado.name() + " = " + DataModel.EstadoEnviado.no_enviado.ordinal(), null, null, null, DataModel.Columns.TlxOrdTpl.name() + " ASC");
        while (query.moveToNext()) {
            dataModels.add(DataModel.fromCursor(query));
        }
        query.close();
        return dataModels;
    }

    public void updateObject(String table, String colId, int detalleFacturaId, ContentValues values) {
        open();
        db.update(table, values, colId + " = " + detalleFacturaId, null);
    }

    public String getItemDescription(int idItem) {
        open();
        Cursor query = db.query(DBHelper.ITEM_FACTURACION_TABLE, null, ItemFacturacion.Columns.id.name() + " = " + idItem, null, null, null, null);
        query.moveToFirst();
        String desc = query.getString(ItemFacturacion.Columns.descripcion.ordinal());
        query.close();
        return desc;
    }

    public int getMaxKwh(int categoriaTarifa) {
        open();
        Cursor cursor = db.query(DBHelper.LIMITES_MAXIMOS_TABLE, null,
                LimitesMaximos.Columns.categoria_tarifa_id.name() + " = " + categoriaTarifa,
                null, null, null, null);
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            int maxKwh = cursor.getInt(LimitesMaximos.Columns.max_kwh.ordinal());
            cursor.close();
            return maxKwh;
        }
        cursor.close();
        return -1;
    }
}
