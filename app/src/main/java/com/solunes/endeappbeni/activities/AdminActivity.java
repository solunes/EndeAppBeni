package com.solunes.endeappbeni.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Debug;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DebugUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.solunes.endeappbeni.BuildConfig;
import com.solunes.endeappbeni.R;
import com.solunes.endeappbeni.dataset.DBAdapter;
import com.solunes.endeappbeni.dataset.DBHelper;
import com.solunes.endeappbeni.models.Factasas;
import com.solunes.endeappbeni.models.Factasascateg;
import com.solunes.endeappbeni.models.Factasrut;
import com.solunes.endeappbeni.models.FacturaDosificacion;
import com.solunes.endeappbeni.models.ItemFacturacion;
import com.solunes.endeappbeni.models.LimitesMaximos;
import com.solunes.endeappbeni.models.Obs;
import com.solunes.endeappbeni.models.Parametro;
import com.solunes.endeappbeni.models.PrintObs;
import com.solunes.endeappbeni.models.Tarifa;
import com.solunes.endeappbeni.models.User;
import com.solunes.endeappbeni.networking.CallbackAPI;
import com.solunes.endeappbeni.networking.GetRequest;
import com.solunes.endeappbeni.networking.Token;
import com.solunes.endeappbeni.utils.FileUtils;
import com.solunes.endeappbeni.utils.Urls;
import com.solunes.endeappbeni.utils.UserPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Map;

/**
 * Esta activity controla el login del usuario
 */
public class AdminActivity extends AppCompatActivity {

    private static final String TAG = "AdminActivity";
    public static final String KEY_DOMAIN = "key_domain";
    public static final String KEY_PRINT_MANE = "key_print_name";
    public static final String KEY_AREA = "key_area";

    private User user;

    private EditText editDomain;
    private TextView nroDomain;

    private EditText editPrintName;
    private EditText editArea;
    private TextView printName;
    private TextView area;
    private String url;

    private AlertDialog.Builder builder;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));

        editDomain = (EditText) findViewById(R.id.edit_domain);
        editArea = (EditText) findViewById(R.id.edit_area);
        Button btnSaveDomain = (Button) findViewById(R.id.btn_save_domain);
        Button btnFixParams = (Button) findViewById(R.id.btn_fix_params);
        Button btnArea = (Button) findViewById(R.id.btn_area);
        nroDomain = (TextView) findViewById(R.id.label_nro_domain);
        area = (TextView) findViewById(R.id.label_area);
        if (UserPreferences.getInt(getApplicationContext(), KEY_AREA) != 0) {
            area.setText("Número de área: " + UserPreferences.getInt(getApplicationContext(), KEY_AREA));
        }
        url = UserPreferences.getString(getApplicationContext(), KEY_DOMAIN);
        if (url != null) {
            nroDomain.setText("Url: " + url);
            editDomain.setText(url);
        }
        Log.e(TAG, "onCreate: " + getExternalFilesDir(null));

        // obtener usuario
        final int id_user = getIntent().getExtras().getInt("id_user");
        final DBAdapter dbAdapter = new DBAdapter(this);
        user = dbAdapter.getUser(id_user);
        dbAdapter.close();
        btnSaveDomain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!editDomain.getText().toString().isEmpty()) {
                    // guardar url del servidor
                    url = editDomain.getText().toString();
                    UserPreferences.putString(getApplicationContext(), KEY_DOMAIN, url);
                    nroDomain.setText("Url: " + url);
                }
            }
        });
        btnArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserPreferences.putInt(getApplicationContext(), KEY_AREA, Integer.parseInt(editArea.getText().toString()));
                area.setText("Número de área: " + UserPreferences.getInt(getApplicationContext(), KEY_AREA));
            }
        });
        btnFixParams.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                // mensaje de descarga de parametros fijos
                builder = new AlertDialog.Builder(AdminActivity.this);
                progressDialog = new ProgressDialog(AdminActivity.this);
                builder.setTitle("Administrador");
                builder.setPositiveButton("Aceptar", null);
                progressDialog.setMessage("Descargando....");
                progressDialog.setCancelable(false);
                if (UserPreferences.getInt(getApplicationContext(), KEY_AREA) == 0) {
                    Snackbar.make(view, "No hay número de área ", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (url == null) {
                    Snackbar.make(view, "No hay url del servidor", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                // previamente a la consulta del servidor, se obtiene un Token
                Token.getToken(getApplicationContext(), user, new Token.CallbackToken() {
                    @Override
                    public void onSuccessToken() {
                        parametrosRequest();
                    }

                    @Override
                    public void onFailToken() {
                        progressDialog.dismiss();
                    }
                });
                progressDialog.show();
            }
        });

        Button btnImport = (Button) findViewById(R.id.btn_import);
        btnImport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                Log.e(TAG, "onClick: request permossion method");
                requestPermissions();
            }
        });

        Button test = (Button) findViewById(R.id.btn_test);
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AdminActivity.this, TestActivity.class).putExtra("user_id", id_user));
            }
        });
        if (BuildConfig.DEBUG){
            test.setVisibility(View.VISIBLE);
        }

        // se muestra el nombre de la impresora
        printName = (TextView) findViewById(R.id.label_print_name);
        String string = UserPreferences.getString(getApplicationContext(), KEY_PRINT_MANE);
        if (string != null) {
            printName.setText("Impresora: " + string);
        }
        editPrintName = (EditText) findViewById(R.id.edit_print_name);
        Button buttonPrintName = (Button) findViewById(R.id.btn_print_name);
        buttonPrintName.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // se guarda el nombre de la impresora si no hay
                String printname = editPrintName.getText().toString();
                if (!printname.isEmpty()) {
                    UserPreferences.putString(getApplicationContext(), KEY_PRINT_MANE, printname);
                    printName.setText("Impresora: " + printname);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                finish();
                startActivity(new Intent(this, LoginActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Metodo para procesar lo obtenido de los parametros fijos
     *
     * @param context contexto de la aplicacion para hacer consultas a la base de datos
     * @param result  es una cadena json que contiene de los datos de parametros fijos
     * @throws JSONException excepcion si el string result no tiene formato json
     */
    public static void processResultFixParams(Context context, String result) throws JSONException {
        JSONObject jsonObject = new JSONObject(result);

        // guarda tarifas
        JSONArray tarifas = jsonObject.getJSONArray("tarifas");
        DBAdapter dbAdapter = new DBAdapter(context);
        dbAdapter.clearTables();
        for (int i = 0; i < tarifas.length(); i++) {
            JSONObject object = tarifas.getJSONObject(i);
            ContentValues values = new ContentValues();
            values.put(Tarifa.Columns.id.name(), object.getInt(Tarifa.Columns.id.name()));
            values.put(Tarifa.Columns.categoria_tarifa_id.name(), object.getInt(Tarifa.Columns.categoria_tarifa_id.name()));
            values.put(Tarifa.Columns.item_facturacion_id.name(), object.getInt(Tarifa.Columns.item_facturacion_id.name()));
            try {
                values.put(Tarifa.Columns.kwh_desde.name(), object.getInt(Tarifa.Columns.kwh_desde.name()));
            } catch (JSONException e) {
                values.put(Tarifa.Columns.kwh_desde.name(), 0);
            }
            try {
                values.put(Tarifa.Columns.kwh_hasta.name(), object.getInt(Tarifa.Columns.kwh_hasta.name()));
            } catch (JSONException e) {
                values.put(Tarifa.Columns.kwh_hasta.name(), 0);
            }
            values.put(Tarifa.Columns.importe.name(), object.getDouble(Tarifa.Columns.importe.name()));
            // guardar values
            dbAdapter.saveObject(DBHelper.TARIFA_TABLE, values);
        }

        // guarda usuarios
        JSONArray usuarios = jsonObject.getJSONArray("usuarios");
        for (int i = 0; i < usuarios.length(); i++) {
            JSONObject object = usuarios.getJSONObject(i);
            ContentValues values = new ContentValues();
            values.put(User.Columns.LecId.name(), object.getInt(User.Columns.LecId.name()));
            values.put(User.Columns.LecNom.name(), object.getString(User.Columns.LecNom.name()).trim());
            values.put(User.Columns.LecCod.name(), object.getString(User.Columns.LecCod.name()).trim());
            values.put(User.Columns.LecPas.name(), object.getString(User.Columns.LecPas.name()).trim());
            values.put(User.Columns.LecNiv.name(), object.getInt(User.Columns.LecNiv.name()));
            values.put(User.Columns.LecAsi.name(), object.getInt(User.Columns.LecAsi.name()));
            values.put(User.Columns.LecAct.name(), object.getInt(User.Columns.LecAct.name()));
            values.put(User.Columns.AreaCod.name(), object.getInt(User.Columns.AreaCod.name()));
            // guardar values
            dbAdapter.saveObject(DBHelper.USER_TABLE, values);
        }

        // guarda observaciones
        JSONArray observaciones = jsonObject.getJSONArray("observaciones");
        for (int i = 0; i < observaciones.length(); i++) {
            JSONObject object = observaciones.getJSONObject(i);
            ContentValues values = new ContentValues();
            values.put(Obs.Columns.id.name(), object.getInt(Obs.Columns.id.name()));
            values.put(Obs.Columns.ObsDes.name(), object.getString(Obs.Columns.ObsDes.name()).trim());
            values.put(Obs.Columns.ObsTip.name(), object.getInt(Obs.Columns.ObsTip.name()));
            values.put(Obs.Columns.ObsInd.name(), object.getInt(Obs.Columns.ObsInd.name()));
            values.put(Obs.Columns.ObsLec.name(), object.getInt(Obs.Columns.ObsLec.name()));
            values.put(Obs.Columns.ObsFac.name(), object.getInt(Obs.Columns.ObsFac.name()));
            values.put(Obs.Columns.ObsCond.name(), object.getInt(Obs.Columns.ObsCond.name()));
            // guardar values
            dbAdapter.saveObject(DBHelper.OBS_TABLE, values);
        }

        // guarda parametros
        JSONArray parametros = jsonObject.getJSONArray("parametros");
        for (int i = 0; i < parametros.length(); i++) {
            JSONObject object = parametros.getJSONObject(i);
            ContentValues values = new ContentValues();
            values.put(Parametro.Columns.id.name(), object.getInt(Parametro.Columns.id.name()));
            values.put(Parametro.Columns.codigo.name(), object.getString(Parametro.Columns.codigo.name()).trim());
            try {
                values.put(Parametro.Columns.valor.name(), object.getInt(Parametro.Columns.valor.name()));
            } catch (JSONException e) {
                values.put(Parametro.Columns.valor.name(), 0);
            }
            try {
                values.put(Parametro.Columns.texto.name(), object.getString(Parametro.Columns.texto.name()).trim());
            } catch (JSONException e) {
                values.put(Parametro.Columns.texto.name(), "");
            }
            // guardar values
            dbAdapter.saveObject(DBHelper.PARAMETRO_TABLE, values);
        }

        // guarda items de facturacion
        JSONArray itemFacturacion = jsonObject.getJSONArray("item_facturacion");
        for (int i = 0; i < itemFacturacion.length(); i++) {
            JSONObject object = itemFacturacion.getJSONObject(i);
            ContentValues values = new ContentValues();
            values.put(ItemFacturacion.Columns.id.name(), object.getInt(ItemFacturacion.Columns.id.name()));
            values.put(ItemFacturacion.Columns.codigo.name(), object.getInt(ItemFacturacion.Columns.codigo.name()));
            values.put(ItemFacturacion.Columns.concepto.name(), object.getInt(ItemFacturacion.Columns.concepto.name()));
            values.put(ItemFacturacion.Columns.descripcion.name(), object.getString(ItemFacturacion.Columns.descripcion.name()).trim());
            values.put(ItemFacturacion.Columns.estado.name(), object.getInt(ItemFacturacion.Columns.estado.name()));
            values.put(ItemFacturacion.Columns.credito_fiscal.name(), object.getInt(ItemFacturacion.Columns.credito_fiscal.name()));
            // guardar values
            dbAdapter.saveObject(DBHelper.ITEM_FACTURACION_TABLE, values);
        }

        // guarda observaciones de impresion
        JSONArray observacionesImp = jsonObject.getJSONArray("observaciones_imp");
        for (int i = 0; i < observacionesImp.length(); i++) {
            JSONObject object = observacionesImp.getJSONObject(i);
            ContentValues values = new ContentValues();
            values.put(PrintObs.Columns.id.name(), object.getInt(PrintObs.Columns.id.name()));
            values.put(PrintObs.Columns.ObiDes.name(), object.getString(PrintObs.Columns.ObiDes.name()).trim());
            // guardar values
            dbAdapter.saveObject(DBHelper.PRINT_OBS_TABLE, values);
        }

        //  guarda la dosificacion de facturas
        JSONArray facturaDosificacion = jsonObject.getJSONArray("factura_dosificacion");
        for (int i = 0; i < facturaDosificacion.length(); i++) {
            JSONObject object = facturaDosificacion.getJSONObject(i);
            ContentValues values = new ContentValues();
            values.put(FacturaDosificacion.Columns.id.name(), object.getInt(FacturaDosificacion.Columns.id.name()));
            values.put(FacturaDosificacion.Columns.area_id.name(), object.getInt(FacturaDosificacion.Columns.area_id.name()));
            values.put(FacturaDosificacion.Columns.numero.name(), object.getInt(FacturaDosificacion.Columns.numero.name()));
            values.put(FacturaDosificacion.Columns.comprobante.name(), object.getInt(FacturaDosificacion.Columns.comprobante.name()));
            values.put(FacturaDosificacion.Columns.numero_autorizacion.name(), object.getInt(FacturaDosificacion.Columns.numero_autorizacion.name()));
            values.put(FacturaDosificacion.Columns.numero_factura.name(), object.getInt(FacturaDosificacion.Columns.numero_factura.name()));
            values.put(FacturaDosificacion.Columns.estado.name(), object.getInt(FacturaDosificacion.Columns.estado.name()));
            values.put(FacturaDosificacion.Columns.fecha_inicial.name(), object.getString(FacturaDosificacion.Columns.fecha_inicial.name()).trim());
            values.put(FacturaDosificacion.Columns.fecha_limite_emision.name(), object.getString(FacturaDosificacion.Columns.fecha_limite_emision.name()).trim());
            values.put(FacturaDosificacion.Columns.llave_dosificacion.name(), object.getString(FacturaDosificacion.Columns.llave_dosificacion.name()).trim());
            values.put(FacturaDosificacion.Columns.leyenda1.name(), object.getString(FacturaDosificacion.Columns.leyenda1.name()).trim());
            values.put(FacturaDosificacion.Columns.leyenda1.name(), object.getString(FacturaDosificacion.Columns.leyenda1.name()).trim());
            values.put(FacturaDosificacion.Columns.actividad_economica.name(), object.getString(FacturaDosificacion.Columns.actividad_economica.name()).trim());
            // guardar values
            dbAdapter.saveObject(DBHelper.FACTURA_DOSIFICACION_TABLE, values);
        }

        JSONArray factasas = jsonObject.getJSONArray("factasas");
        for (int i = 0; i < factasas.length(); i++) {
            JSONObject object = factasas.getJSONObject(i);
            ContentValues values = new ContentValues();
            values.put(Factasas.Columns.id.name(), object.getInt(Factasas.Columns.id.name()));
            values.put(Factasas.Columns.empresa_id.name(), object.getInt(Factasas.Columns.empresa_id.name()));
            values.put(Factasas.Columns.servicio_id.name(), object.getInt(Factasas.Columns.servicio_id.name()));
            values.put(Factasas.Columns.descripcion.name(), object.getString(Factasas.Columns.descripcion.name()));
            values.put(Factasas.Columns.porcentaje.name(), object.getDouble(Factasas.Columns.porcentaje.name()));
            values.put(Factasas.Columns.estado.name(), object.getInt(Factasas.Columns.estado.name()));
            dbAdapter.saveObject(DBHelper.FACTASAS_TABLE, values);
        }

        JSONArray factasascateg = jsonObject.getJSONArray("factasascateg");
        for (int i = 0; i < factasascateg.length(); i++) {
            JSONObject object = factasascateg.getJSONObject(i);
            ContentValues values = new ContentValues();
            values.put(Factasascateg.Columns.id.name(), object.getInt(Factasascateg.Columns.id.name()));
            values.put(Factasascateg.Columns.empresa_id.name(), object.getInt(Factasascateg.Columns.empresa_id.name()));
            values.put(Factasascateg.Columns.linea_id.name(), object.getInt(Factasascateg.Columns.linea_id.name()));
            values.put(Factasascateg.Columns.categoria_id.name(), object.getInt(Factasascateg.Columns.categoria_id.name()));
            values.put(Factasascateg.Columns.descripcion.name(), object.getString(Factasascateg.Columns.descripcion.name()));
            try {
                values.put(Factasascateg.Columns.rango_inicial.name(), object.getInt(Factasascateg.Columns.rango_inicial.name()));
            } catch (JSONException e) {
                values.put(Factasascateg.Columns.rango_inicial.name(), 0);
            }
            try {
                values.put(Factasascateg.Columns.importe.name(), object.getInt(Factasascateg.Columns.importe.name()));
            } catch (JSONException e) {
                values.put(Factasascateg.Columns.importe.name(), 0);
            }
            values.put(Factasascateg.Columns.rango_final.name(), object.getInt(Factasascateg.Columns.rango_final.name()));
            values.put(Factasascateg.Columns.estado.name(), object.getInt(Factasascateg.Columns.estado.name()));
            dbAdapter.saveObject(DBHelper.FACTASASCATEG_TABLE, values);
        }

        JSONArray factasrut = jsonObject.getJSONArray("factasrut");
        for (int i = 0; i < factasrut.length(); i++) {
            JSONObject object = factasrut.getJSONObject(i);
            ContentValues values = new ContentValues();
            values.put(Factasrut.Columns.id.name(), object.getInt(Factasrut.Columns.id.name()));
            values.put(Factasrut.Columns.empresa_id.name(), object.getInt(Factasrut.Columns.empresa_id.name()));
            values.put(Factasrut.Columns.ruta.name(), object.getInt(Factasrut.Columns.ruta.name()));
            values.put(Factasrut.Columns.porcentaje.name(), object.getDouble(Factasrut.Columns.porcentaje.name()));
            values.put(Factasrut.Columns.estado.name(), object.getInt(Factasrut.Columns.estado.name()));
            values.put(Factasrut.Columns.descripcion.name(), object.getString(Factasrut.Columns.descripcion.name()));
            values.put(Factasrut.Columns.aseo.name(), object.getInt(Factasrut.Columns.aseo.name()));
            dbAdapter.saveObject(DBHelper.FACTASRUT_TABLE, values);
        }

        // guarda los limites maximos
        JSONArray limitesMaximos = jsonObject.getJSONArray("limites_maximos");
        for (int i = 0; i < limitesMaximos.length(); i++) {
            JSONObject object = limitesMaximos.getJSONObject(i);
            ContentValues values = new ContentValues();
            values.put(LimitesMaximos.Columns.id.name(), object.getInt(LimitesMaximos.Columns.id.name()));
            values.put(LimitesMaximos.Columns.categoria_tarifa_id.name(), object.getInt(LimitesMaximos.Columns.categoria_tarifa_id.name()));
            values.put(LimitesMaximos.Columns.max_kwh.name(), object.getInt(LimitesMaximos.Columns.max_kwh.name()));
            values.put(LimitesMaximos.Columns.max_bs.name(), object.getInt(LimitesMaximos.Columns.max_bs.name()));
            // guardar values
            dbAdapter.saveObject(DBHelper.LIMITES_MAXIMOS_TABLE, values);
        }
    }

    /**
     * Este metodo hace la consulta el servidor para obtener los paratros fijos
     */
    private void parametrosRequest() {
        new GetRequest(getApplicationContext(), Urls.urlParametros(getApplicationContext(), UserPreferences.getInt(getApplicationContext(), AdminActivity.KEY_AREA)), new CallbackAPI() {
            @Override
            public void onSuccess(String result, int statusCode) {
                try {
                    processResultFixParams(getApplicationContext(), result);
                    progressDialog.dismiss();
                    builder.setMessage("Los datos de tarifas, observaciones y usuarios han sido descargados");
                    builder.show();
                    UserPreferences.putLong(getApplicationContext(), MainActivity.KEY_RATE, Calendar.getInstance().getTimeInMillis());
                    int month = Calendar.getInstance().get(Calendar.MONTH);
                    UserPreferences.putInt(getApplicationContext(), MainActivity.KEY_RATE_MONTH, month);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailed(String reason, int statusCode) {
                builder.setMessage(reason);
                builder.show();
                progressDialog.dismiss();
            }
        }).execute();
    }

    private void importDB(final View view) {
        FileUtils.importDB(getApplicationContext(), new FileUtils.FileUtilsCallback() {
            @Override
            public void suceess() {
                Snackbar.make(view, "Se importó correctamente", Snackbar.LENGTH_SHORT).show();
            }

            @Override
            public void error() {
                Snackbar.make(view, "Hubo un error al importar", Snackbar.LENGTH_SHORT).show();
            }

            @Override
            public void noSD() {
                Snackbar.make(view, "No se encuentra una tarjeta SD", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 7;

    private void requestPermissions() {
        Log.e(TAG, "requestPermissions: " + ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE));
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Log.e(TAG, "requestPermissions: show an explanation");

            } else {

                // No explanation needed, we can request the permission.
                Log.e(TAG, "requestPermissions: no show a explanation");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_WRITE_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            importDB(nroDomain);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Log.e(TAG, "onRequestPermissionsResult: garantizado");
                    importDB(nroDomain);

                } else {
                    finish();
                }
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
