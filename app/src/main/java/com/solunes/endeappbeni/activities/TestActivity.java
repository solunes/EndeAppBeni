package com.solunes.endeappbeni.activities;

import android.content.ContentValues;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.solunes.endeappbeni.R;
import com.solunes.endeappbeni.dataset.DBAdapter;
import com.solunes.endeappbeni.dataset.DBHelper;
import com.solunes.endeappbeni.fragments.DataFragment;
import com.solunes.endeappbeni.models.DataModel;
import com.solunes.endeappbeni.models.DataObs;
import com.solunes.endeappbeni.models.DetalleFactura;
import com.solunes.endeappbeni.models.Historico;
import com.solunes.endeappbeni.models.Obs;
import com.solunes.endeappbeni.models.Resultados;
import com.solunes.endeappbeni.models.User;
import com.solunes.endeappbeni.networking.CallbackAPI;
import com.solunes.endeappbeni.networking.GetRequest;
import com.solunes.endeappbeni.networking.PostRequest;
import com.solunes.endeappbeni.networking.Token;
import com.solunes.endeappbeni.utils.GenLecturas;
import com.solunes.endeappbeni.utils.StringUtils;
import com.solunes.endeappbeni.utils.Urls;
import com.solunes.endeappbeni.utils.UserPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;

import static com.solunes.endeappbeni.activities.MainActivity.prepareDataToPost;

public class TestActivity extends AppCompatActivity {

    private static final String TAG = "TestActivity";
    private DBAdapter dbAdapter;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        dbAdapter = new DBAdapter(getApplicationContext());
        int user_id = getIntent().getExtras().getInt("user_id");
        user = dbAdapter.getUser(user_id);
        user.setLecCod("user1");
        Button testDownload = (Button) findViewById(R.id.btn_test_download);
        testDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                download();
            }
        });
        Button testRun = (Button) findViewById(R.id.btn_test_run);
        testRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startTest();
            }
        });

        Button testSend = (Button) findViewById(R.id.btn_test_send);
        testSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendRes();
            }
        });
    }

    private void sleeper(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public void download() {
        final String url = Urls.urlDescarga(getApplicationContext(), UserPreferences.getInt(getApplicationContext(), AdminActivity.KEY_AREA), user.getLecCod());
        Token.getToken(getApplicationContext(), user, new Token.CallbackToken() {
            @Override
            public void onSuccessToken() {
                new GetRequest(getApplicationContext(), url, new CallbackAPI() {
                    @Override
                    public void onSuccess(final String result, int statusCode) {
                        Runnable runSaveData = new Runnable() {

                            @Override
                            public void run() {
                                boolean response = false;
                                try {
                                    response = processResponse(result);
                                } catch (JSONException e) {
                                    Log.e(TAG, "onSuccess: ", e);
                                }
                                Log.e(TAG, "run: res: " + response);
//                                if (response) {
//                                    UserPreferences.putBoolean(getApplicationContext(), KEY_WAS_UPLOAD, false);
//                                    UserPreferences.putInt(getApplicationContext(), ReadingActivity.KEY_LAST_PAGER_PSOTION, 0);
//                                    UserPreferences.putLong(MainActivity.this, KEY_DOWNLOAD, Calendar.getInstance().getTimeInMillis());
//                                } else {
//                                    Snackbar.make(view, "No hay datos en la descarga", Snackbar.LENGTH_SHORT).show();
//                                }
//                                progressDialog.dismiss();
                            }
                        };
                        new Thread(runSaveData).start();
                    }

                    @Override
                    public void onFailed(String reason, int statusCode) {
                        Log.e(TAG, "onFailed: " + reason);
//                        progressDialog.setOnDismissListener(null);
//                        progressDialog.dismiss();
//                        Snackbar.make(view, reason, Snackbar.LENGTH_SHORT).show();
                    }
                }).execute();
            }

            @Override
            public void onFailToken() {
                Log.e(TAG, "onFailToken: ");
//                progressDialog.setOnDismissListener(null);
//                progressDialog.dismiss();
            }
        });
    }

    private boolean processResponse(String result) throws JSONException {
        JSONArray results = new JSONArray(result);
        DBAdapter dbAdapter = new DBAdapter(this);
        dbAdapter.beforeDownloadData();
        for (int i = 0; i < results.length(); i++) {
            JSONObject object = results.getJSONObject(i);
            ContentValues values = new ContentValues();

            values.put(DataModel.Columns.id.name(), object.getInt(DataModel.Columns.id.name()));
            values.put(DataModel.Columns.TlxRem.name(), object.getInt(DataModel.Columns.TlxRem.name()));
            values.put(DataModel.Columns.TlxAre.name(), object.getInt(DataModel.Columns.TlxAre.name()));
            values.put(DataModel.Columns.TlxRutO.name(), object.getInt(DataModel.Columns.TlxRutO.name()));
            values.put(DataModel.Columns.TlxRutA.name(), object.getInt(DataModel.Columns.TlxRutA.name()));
            values.put(DataModel.Columns.TlxAno.name(), object.getInt(DataModel.Columns.TlxAno.name()));
            values.put(DataModel.Columns.TlxMes.name(), object.getInt(DataModel.Columns.TlxMes.name()));
            values.put(DataModel.Columns.TlxCli.name(), object.getDouble(DataModel.Columns.TlxCli.name()));
            values.put(DataModel.Columns.TlxDav.name(), object.getInt(DataModel.Columns.TlxDav.name()));
            values.put(DataModel.Columns.TlxEstCli.name(), object.getInt(DataModel.Columns.TlxEstCli.name()));
            values.put(DataModel.Columns.TlxOrdTpl.name(), object.getInt(DataModel.Columns.TlxOrdTpl.name()));
            values.put(DataModel.Columns.TlxNom.name(), object.getString(DataModel.Columns.TlxNom.name()).trim());
            values.put(DataModel.Columns.TlxDir.name(), object.getString(DataModel.Columns.TlxDir.name()).trim());
            values.put(DataModel.Columns.TlxCtaAnt.name(), object.getString(DataModel.Columns.TlxCtaAnt.name()).trim());
            values.put(DataModel.Columns.TlxCtg.name(), object.getString(DataModel.Columns.TlxCtg.name()).trim());
            values.put(DataModel.Columns.TlxCtgTap.name(), object.getInt(DataModel.Columns.TlxCtgTap.name()));
            values.put(DataModel.Columns.TlxCtgAseo.name(), object.getInt(DataModel.Columns.TlxCtgAseo.name()));
            values.put(DataModel.Columns.TlxNroMed.name(), object.getString(DataModel.Columns.TlxNroMed.name()).trim());
            values.put(DataModel.Columns.TlxNroDig.name(), object.getInt(DataModel.Columns.TlxNroDig.name()));
            values.put(DataModel.Columns.TlxFacMul.name(), object.getDouble(DataModel.Columns.TlxFacMul.name()));
            values.put(DataModel.Columns.TlxFecAnt.name(), object.getString(DataModel.Columns.TlxFecAnt.name()).trim());
            values.put(DataModel.Columns.TlxFecLec.name(), object.getString(DataModel.Columns.TlxFecLec.name()).trim());
            values.put(DataModel.Columns.TlxUltInd.name(), object.getInt(DataModel.Columns.TlxUltInd.name()));
            values.put(DataModel.Columns.TlxConPro.name(), object.getInt(DataModel.Columns.TlxConPro.name()));
            values.put(DataModel.Columns.TlxTipLec.name(), object.getInt(DataModel.Columns.TlxTipLec.name()));
            values.put(DataModel.Columns.TlxSgl.name(), object.getString(DataModel.Columns.TlxSgl.name()).trim());
            values.put(DataModel.Columns.TlxTipDem.name(), object.getInt(DataModel.Columns.TlxTipDem.name()));
            values.put(DataModel.Columns.TlxOrdSeq.name(), object.getInt(DataModel.Columns.TlxOrdSeq.name()));
            values.put(DataModel.Columns.TlxLeePot.name(), object.getInt(DataModel.Columns.TlxLeePot.name()));
            values.put(DataModel.Columns.TlxCotaseo.name(), object.getInt(DataModel.Columns.TlxCotaseo.name()));
            values.put(DataModel.Columns.TlxTap.name(), object.getDouble(DataModel.Columns.TlxTap.name()));
            values.put(DataModel.Columns.TlxPotCon.name(), object.getInt(DataModel.Columns.TlxPotCon.name()));
            values.put(DataModel.Columns.TlxPotFac.name(), object.getInt(DataModel.Columns.TlxPotFac.name()));
            values.put(DataModel.Columns.TlxCliNit.name(), object.getString(DataModel.Columns.TlxCliNit.name()));
            values.put(DataModel.Columns.TlxFecCor.name(), object.getString(DataModel.Columns.TlxFecCor.name()).trim());
            values.put(DataModel.Columns.TlxFecVto.name(), object.getString(DataModel.Columns.TlxFecVto.name()).trim());
            values.put(DataModel.Columns.TlxFecproEmi.name(), object.getString(DataModel.Columns.TlxFecproEmi.name()).trim());
            values.put(DataModel.Columns.TlxFecproMed.name(), object.getString(DataModel.Columns.TlxFecproMed.name()).trim());
            values.put(DataModel.Columns.TlxTope.name(), object.getInt(DataModel.Columns.TlxTope.name()));
            values.put(DataModel.Columns.TlxLeyTag.name(), object.getInt(DataModel.Columns.TlxLeyTag.name()));
            values.put(DataModel.Columns.TlxTpoTap.name(), object.getInt(DataModel.Columns.TlxTpoTap.name()));
            values.put(DataModel.Columns.TlxKwhAdi.name(), object.getInt(DataModel.Columns.TlxKwhAdi.name()));
            values.put(DataModel.Columns.TlxImpAvi.name(), object.getInt(DataModel.Columns.TlxImpAvi.name()));
            values.put(DataModel.Columns.TlxTipImp.name(), object.getInt(DataModel.Columns.TlxTipImp.name()));
            values.put(DataModel.Columns.TlxCarFac.name(), object.getInt(DataModel.Columns.TlxCarFac.name()));
            values.put(DataModel.Columns.TlxDeuEneC.name(), object.getInt(DataModel.Columns.TlxDeuEneC.name()));
            values.put(DataModel.Columns.TlxDeuEneI.name(), object.getDouble(DataModel.Columns.TlxDeuEneI.name()));
            values.put(DataModel.Columns.TlxDeuAseC.name(), object.getInt(DataModel.Columns.TlxDeuAseC.name()));
            values.put(DataModel.Columns.TlxDeuAseI.name(), object.getDouble(DataModel.Columns.TlxDeuAseI.name()));
            values.put(DataModel.Columns.TlxFecEmi.name(), object.getString(DataModel.Columns.TlxFecEmi.name()).trim());
            values.put(DataModel.Columns.TlxUltPag.name(), object.getString(DataModel.Columns.TlxUltPag.name()).trim());
            values.put(DataModel.Columns.TlxEstado.name(), object.getInt(DataModel.Columns.TlxEstado.name()));
            values.put(DataModel.Columns.TlxUltObs.name(), object.getString(DataModel.Columns.TlxUltObs.name()).trim());
            values.put(DataModel.Columns.TlxActivi.name(), object.getString(DataModel.Columns.TlxActivi.name()).trim());
            values.put(DataModel.Columns.TlxCiudad.name(), object.getString(DataModel.Columns.TlxCiudad.name()).trim());
            values.put(DataModel.Columns.TlxFacNro.name(), object.getString(DataModel.Columns.TlxFacNro.name()).trim());
            values.put(DataModel.Columns.TlxNroAut.name(), object.getString(DataModel.Columns.TlxNroAut.name()).trim());
            values.put(DataModel.Columns.TlxCodCon.name(), object.getString(DataModel.Columns.TlxCodCon.name()).trim());
            values.put(DataModel.Columns.TlxFecLim.name(), object.getString(DataModel.Columns.TlxFecLim.name()).trim());
            values.put(DataModel.Columns.TlxKwhDev.name(), object.getInt(DataModel.Columns.TlxKwhDev.name()));
            values.put(DataModel.Columns.TlxUltTipL.name(), object.getInt(DataModel.Columns.TlxUltTipL.name()));
            values.put(DataModel.Columns.TlxCliNew.name(), object.getInt(DataModel.Columns.TlxCliNew.name()));
            values.put(DataModel.Columns.TlxEntEne.name(), object.getInt(DataModel.Columns.TlxEntEne.name()));
            values.put(DataModel.Columns.TlxDecEne.name(), object.getInt(DataModel.Columns.TlxDecEne.name()));
            values.put(DataModel.Columns.TlxEntPot.name(), object.getInt(DataModel.Columns.TlxEntPot.name()));
            values.put(DataModel.Columns.TlxDecPot.name(), object.getInt(DataModel.Columns.TlxDecPot.name()));
            values.put(DataModel.Columns.TlxDemPot.name(), object.getString(DataModel.Columns.TlxDemPot.name()).trim());
            values.put(DataModel.Columns.TlxPotTag.name(), object.getInt(DataModel.Columns.TlxPotTag.name()));
            values.putAll(stringNull(DataModel.Columns.TlxPreAnt1.name(), object.getString(DataModel.Columns.TlxPreAnt1.name()).trim()));
            values.putAll(stringNull(DataModel.Columns.TlxPreAnt2.name(), object.getString(DataModel.Columns.TlxPreAnt2.name()).trim()));
            values.putAll(stringNull(DataModel.Columns.TlxPreAnt3.name(), object.getString(DataModel.Columns.TlxPreAnt3.name()).trim()));
            values.putAll(stringNull(DataModel.Columns.TlxPreAnt4.name(), object.getString(DataModel.Columns.TlxPreAnt4.name()).trim()));
            values.putAll(stringNull(DataModel.Columns.TlxDebAuto.name(), object.getString(DataModel.Columns.TlxDebAuto.name()).trim()));
            values.putAll(stringNull(DataModel.Columns.TlxRecordatorio.name(), object.getString(DataModel.Columns.TlxRecordatorio.name()).trim()));
            values.put(DataModel.Columns.estado_lectura.name(), 0);
            values.put(DataModel.Columns.enviado.name(), 0);
            values.put(DataModel.Columns.TlxDignidad.name(), object.getInt(DataModel.Columns.TlxDignidad.name()));
            dbAdapter.saveObject(DBHelper.DATA_TABLE, values);

            try {
                JSONObject historico = object.getJSONObject("historico");
                Log.e(TAG, "processResponse: " + historico.toString());
                ContentValues valuesH = new ContentValues();
                valuesH.put(Historico.Columns.id.name(), historico.getInt(Historico.Columns.id.name()));
                valuesH.put(Historico.Columns.general_id.name(), historico.getInt(Historico.Columns.general_id.name()));
                valuesH.putAll(stringNull(Historico.Columns.ConMes01.name(), historico.getString(Historico.Columns.ConMes01.name()).trim()));
                valuesH.putAll(stringNull(Historico.Columns.ConMes02.name(), historico.getString(Historico.Columns.ConMes02.name()).trim()));
                valuesH.putAll(stringNull(Historico.Columns.ConMes03.name(), historico.getString(Historico.Columns.ConMes03.name()).trim()));
                valuesH.putAll(stringNull(Historico.Columns.ConMes04.name(), historico.getString(Historico.Columns.ConMes04.name()).trim()));
                valuesH.putAll(stringNull(Historico.Columns.ConMes05.name(), historico.getString(Historico.Columns.ConMes05.name()).trim()));
                valuesH.putAll(stringNull(Historico.Columns.ConMes06.name(), historico.getString(Historico.Columns.ConMes06.name()).trim()));
                valuesH.putAll(stringNull(Historico.Columns.ConMes07.name(), historico.getString(Historico.Columns.ConMes07.name()).trim()));
                valuesH.putAll(stringNull(Historico.Columns.ConMes08.name(), historico.getString(Historico.Columns.ConMes08.name()).trim()));
                valuesH.putAll(stringNull(Historico.Columns.ConMes09.name(), historico.getString(Historico.Columns.ConMes09.name()).trim()));
                valuesH.putAll(stringNull(Historico.Columns.ConMes10.name(), historico.getString(Historico.Columns.ConMes10.name()).trim()));
                valuesH.putAll(stringNull(Historico.Columns.ConMes11.name(), historico.getString(Historico.Columns.ConMes11.name()).trim()));
                valuesH.putAll(stringNull(Historico.Columns.ConMes12.name(), historico.getString(Historico.Columns.ConMes12.name()).trim()));
                valuesH.put(Historico.Columns.ConKwh01.name(), historico.getInt(Historico.Columns.ConKwh01.name()));
                valuesH.put(Historico.Columns.ConKwh02.name(), historico.getInt(Historico.Columns.ConKwh02.name()));
                valuesH.put(Historico.Columns.ConKwh03.name(), historico.getInt(Historico.Columns.ConKwh03.name()));
                valuesH.put(Historico.Columns.ConKwh04.name(), historico.getInt(Historico.Columns.ConKwh04.name()));
                valuesH.put(Historico.Columns.ConKwh05.name(), historico.getInt(Historico.Columns.ConKwh05.name()));
                valuesH.put(Historico.Columns.ConKwh06.name(), historico.getInt(Historico.Columns.ConKwh06.name()));
                valuesH.put(Historico.Columns.ConKwh07.name(), historico.getInt(Historico.Columns.ConKwh07.name()));
                valuesH.put(Historico.Columns.ConKwh08.name(), historico.getInt(Historico.Columns.ConKwh08.name()));
                valuesH.put(Historico.Columns.ConKwh09.name(), historico.getInt(Historico.Columns.ConKwh09.name()));
                valuesH.put(Historico.Columns.ConKwh10.name(), historico.getInt(Historico.Columns.ConKwh10.name()));
                valuesH.put(Historico.Columns.ConKwh11.name(), historico.getInt(Historico.Columns.ConKwh11.name()));
                valuesH.put(Historico.Columns.ConKwh12.name(), historico.getInt(Historico.Columns.ConKwh12.name()));
                dbAdapter.saveObject(DBHelper.HISTORICO_TABLE, valuesH);
            } catch (Exception e) {
                Log.e(TAG, "historico nulo", e);
            }

            JSONObject resultado = object.getJSONObject("resultados");
            ContentValues valuesRes = new ContentValues();
            valuesRes.put(Resultados.Columns.id.name(), resultado.getInt(Resultados.Columns.id.name()));
            valuesRes.put(Resultados.Columns.general_id.name(), resultado.getInt(Resultados.Columns.general_id.name()));
            valuesRes.put(Resultados.Columns.lectura.name(), resultado.getInt(Resultados.Columns.lectura.name()));
            valuesRes.put(Resultados.Columns.lectura_potencia.name(), resultado.getDouble(Resultados.Columns.lectura_potencia.name()));
            valuesRes.put(Resultados.Columns.observacion.name(), resultado.getDouble(Resultados.Columns.observacion.name()));
            dbAdapter.saveObject(DBHelper.RESULTADOS_TABLE, valuesRes);

            JSONArray detalleFacturaArray = object.getJSONArray("detalle_factura");
            Log.e(TAG, "processResponse: " + detalleFacturaArray.toString());
            for (int j = 0; j < detalleFacturaArray.length(); j++) {
                JSONObject detalleFactura = detalleFacturaArray.getJSONObject(j);
                ContentValues valuesDF = new ContentValues();
                valuesDF.put(DetalleFactura.Columns.id.name(), detalleFactura.getInt(DetalleFactura.Columns.id.name()));
                valuesDF.put(DetalleFactura.Columns.general_id.name(), detalleFactura.getInt(DetalleFactura.Columns.general_id.name()));
                valuesDF.put(DetalleFactura.Columns.item_facturacion_id.name(), detalleFactura.getInt(DetalleFactura.Columns.item_facturacion_id.name()));
                valuesDF.put(DetalleFactura.Columns.importe.name(), detalleFactura.getDouble(DetalleFactura.Columns.importe.name()));
                valuesDF.put(DetalleFactura.Columns.imp_redondeo.name(), detalleFactura.getDouble(DetalleFactura.Columns.imp_redondeo.name()));
                dbAdapter.saveObject(DBHelper.DETALLE_FACTURA_TABLE, valuesDF);
            }
        }
        dbAdapter.close();
        return results.length() > 0;
    }

    private void inicio(DataModel dataModel, int lecturaEnergia, int obsCod) {
        final Obs obs = Obs.fromCursor(dbAdapter.getObs(obsCod));
        dbAdapter.close();

        // obtener tipo de lectura
        int tipoLectura = dataModel.getTlxTipLec();
        if (obs.getId() != 104) {
            tipoLectura = obs.getObsLec();
        }

        if (obs.getObsInd() == 1) {
            lecturaEnergia = dataModel.getTlxUltInd();
        } else if (obs.getObsInd() == 2) {
            lecturaEnergia = 0;
        }

        methodPequeñaMedianaDemanda(dataModel, lecturaEnergia, tipoLectura, obs);
    }

    private void methodPequeñaMedianaDemanda(DataModel dataModel, int lecturaEnergia, int tipoLectura, final Obs obs) {

        int nuevaLectura;
        int lecturaKwh;

        // obtener lectura de energia y verificar digitos
        nuevaLectura = lecturaEnergia;
        if (nuevaLectura > dataModel.getTlxTope()) {
            return;
        }
        nuevaLectura = DataFragment.correccionDeDigitos(nuevaLectura, dataModel.getTlxDecEne());
//        }

        // correccion si es que el consumo estimado tiene un indice mayor a cero, se vuelve una lectura normal
        if (tipoLectura == 9 && nuevaLectura == 0) {
            tipoLectura = 3;
        }

        // Calcular la lectura en Kwh segun el tipo de lectura
        if (tipoLectura == 3) {
            lecturaKwh = dataModel.getTlxConPro();
        } else {
            if (nuevaLectura < dataModel.getTlxUltInd()) {
                if ((dataModel.getTlxUltInd() - nuevaLectura) <= 10) {
                    nuevaLectura = dataModel.getTlxUltInd();
                }
            }
            lecturaKwh = GenLecturas.lecturaNormal(dataModel.getTlxUltInd(), nuevaLectura, dataModel.getTlxNroDig());
        }

        // Verificacion si el estado de cliente es cortado o suspendido y se introduce el mismo indice al anterior, se posterga
        if (dataModel.getTlxEstCli() == 3 || dataModel.getTlxEstCli() == 5) {
            if (dataModel.getTlxUltInd() == nuevaLectura) {
                tipoLectura = 5;
            }
        }

        // si hay alerta y el tipo de lectura no es postergada
        confirmarLectura(dataModel, tipoLectura, nuevaLectura, lecturaKwh, obs);
    }

    private void confirmarLectura(DataModel dataModel, int finalTipoLectura, int finalNuevaLectura, int finalLecturaKwh, Obs obs) {
        boolean isCalculo = calculo(dataModel, finalTipoLectura, finalNuevaLectura, finalLecturaKwh, obs);
        if (isCalculo) {
            Log.e(TAG, "confirmarLectura: calculo exitoso: " + dataModel.getId());
            saveLectura(dataModel, obs);
        } else {
            Log.e(TAG, "confirmarLectura: error en: " + dataModel.getId());
        }
    }

    private boolean calculo(DataModel dataModel, int tipoLectura, int nuevaLectura, int lectura, Obs obs) {
        // revisar si no es reimpresion, para no realizar el calculo de nuevo
        if (obs.getObsFac() == 0) {
            dataModel.setTlxImpAvi(0);
        }

        // verificacion de limites maximos para consumos muy elevados
        int maxKwh = dbAdapter.getMaxKwh(dataModel.getTlxCtg());
        dbAdapter.close();
        if (maxKwh == -1) {
//                Toast.makeText(getContext(), "No hay un límite máximo para el consumo", Toast.LENGTH_LONG).show();
            return false;
        }
        if (lectura >= maxKwh) {
            tipoLectura = 5;
        }

        if (tipoLectura == 5) {
            dataModel.setTlxNvaLec(nuevaLectura);
//                if (dataModel.getTlxTipDem() == 2) {
//                    int potenciaLeida = 0;
//                    if (!inputPotenciaReading.getText().toString().isEmpty()) {
//                        potenciaLeida = Integer.valueOf(inputPotenciaReading.getText().toString());
//                    }
//                    potenciaLeida = correccionPotencia(dataModel.getTlxDemPot(), potenciaLeida, dataModel.getTlxDecPot());
//                    dataModel.setTlxPotLei(potenciaLeida);
//                }
            dataModel.setTlxTipLec(tipoLectura);
            dataModel.setTlxImpAvi(0);
            return true;
        }

        // correccion para consumo promedio
        if (tipoLectura == 3) {
            dataModel.setTlxNvaLec(dataModel.getTlxUltInd());
            dataModel.setTlxKwhDev(lectura);
        } else {
            dataModel.setTlxNvaLec(nuevaLectura);
        }
        dataModel.setTlxTipLec(tipoLectura);
        // multiplicar la lectura con el multiplicador de energia
        lectura = (int) (lectura * dataModel.getTlxFacMul());
//        Log.e(TAG, "calculo: consumo " + lectura);
        dataModel.setTlxConsumo(lectura);

        // correccion de kwh a devolver sino es consumo promedio o lectura ajustada
        if (dataModel.getTlxKwhDev() > 0 && tipoLectura != 3) {
            lectura = lectura - dataModel.getTlxKwhDev();
            if (lectura > 0) {
                dataModel.setTlxKwhDev(0);
            } else {
                dataModel.setTlxKwhDev(Math.abs(lectura));
                lectura = 0;
            }
        }

        // lectura final
        if (dataModel.getTlxKwhAdi() > 0 && tipoLectura != 3) {
            lectura = lectura + dataModel.getTlxKwhAdi();
            dataModel.setTlxKwhAdi(0);
        }
        dataModel.setTlxConsFacturado(lectura);
//        Log.e(TAG, "calculo: lectura final " + lectura);

        // obtener cargo fijo de la base de datos para la categoria
        double cargoFijo = dbAdapter.getCargoFijo(dataModel.getTlxCtg());
        dbAdapter.close();
        // redondeo del cargo fijo
        cargoFijo = DetalleFactura.crearDetalle(getApplicationContext(), dataModel.getId(), 1, cargoFijo);
        dataModel.setTlxCarFij(cargoFijo);

        // obtener y calcular el importe de energia por rangos
        double importeEnergia = GenLecturas.importeEnergia(getApplicationContext(), lectura, dataModel.getTlxCtg(), dataModel.getId());
        dataModel.setTlxImpEn(importeEnergia);

        double importeConsumo = GenLecturas.round(dataModel.getTlxCarFij() + dataModel.getTlxImpEn() + dataModel.getTlxImpPot());

        // verificar la tarifa dignidad, calcular, guardar el importe en detalle facturacion
        double tarifaDignidad = 0;
        if (dataModel.getTlxDignidad() == 1) {
            tarifaDignidad = GenLecturas.tarifaDignidad(getApplicationContext(), lectura, importeConsumo);
            tarifaDignidad = DetalleFactura.crearDetalle(getApplicationContext(), dataModel.getId(), 192, tarifaDignidad);
            dataModel.setTlxDesTdi(tarifaDignidad);
        }

        // verificar que hay ley 1886 calcular su importe y guardarlo en detalle facturacion
        double ley1886 = 0;
        if (dataModel.getTlxLeyTag() == 1) {
            ley1886 = GenLecturas.ley1886(getApplicationContext(), lectura, dataModel.getTlxCtg());
            ley1886 = DetalleFactura.crearDetalle(getApplicationContext(), dataModel.getId(), 195, ley1886);
            dataModel.setTlxLey1886(ley1886);
        }

        // calcular consumo total
        double totalConsumo = GenLecturas.totalConsumo(importeConsumo, tarifaDignidad);

        // array de ids de items facturacion de detalle facturacion
        // se pueden agregar mas cargos al array usando el item_facturacion_id
        ArrayList<Integer> integers = new ArrayList<>();
        integers.add(461); // intereses por mora
        integers.add(181); // cargo por conexion
        integers.add(186); // cargo por reconexion
        integers.add(185); // cargo por rehabilitacion
        double cargoExtraTotal = 0;
        for (Integer itemId : integers) {
            double cargoExtra = dbAdapter.getDetalleFacturaImporte(dataModel.getId(), itemId);
            dbAdapter.close();
            cargoExtra = DetalleFactura.crearDetalle(getApplicationContext(), dataModel.getId(), itemId, cargoExtra);
            cargoExtraTotal += cargoExtra;
        }

        // calculo del importe total del suministro
        double totalSuministro = GenLecturas.totalSuministro(totalConsumo, dataModel.getTlxLey1886(), cargoExtraTotal);

        // calculo de suministro tap y suministro por aseo
        Pair<Double, Integer> resSuministroTap = GenLecturas.totalSuministroTap(dataModel, getApplicationContext(), importeConsumo);
        double totalSuministroTap = resSuministroTap.first;
        if (totalSuministroTap < 0) {
            Toast.makeText(getApplicationContext(), "No hay tarifa para el TAP", Toast.LENGTH_LONG).show();
            return false;
        }
        totalSuministroTap = DetalleFactura.crearDetalle(getApplicationContext(), dataModel.getId(), 153, totalSuministroTap);
        double totalSuministroAseo = 0;
        if (resSuministroTap.second < 0) {
            totalSuministroAseo = GenLecturas.totalSuministroAseo(dataModel, getApplicationContext(), importeConsumo);
            if (totalSuministroAseo == -1) {
                Toast.makeText(getApplicationContext(), "No hay tarifa para el aseo", Toast.LENGTH_LONG).show();
                return false;
            }
        }
        totalSuministroAseo = DetalleFactura.crearDetalle(getApplicationContext(), dataModel.getId(), 171, totalSuministroAseo);
        dataModel.setTlxImpFac(totalSuministro);
        dataModel.setTlxImpTap(totalSuministroTap);
        dataModel.setTlxImpAse(totalSuministroAseo);

        // calculo de importe a facturar
        double importeTotalFactura = GenLecturas.totalFacturar(totalSuministro, dataModel.getTlxImpTap(), dataModel.getTlxImpAse());
        double carDep = dbAdapter.getDetalleFacturaImporte(dataModel.getId(), 427);
        dbAdapter.close();
        double importeMesCancelar = importeTotalFactura + carDep;
        dataModel.setTlxImpTot(GenLecturas.roundDecimal(importeMesCancelar + dataModel.getTlxDeuEneI() + dataModel.getTlxDeuAseI(), 1));
//            dataModel.setTlxCodCon(getControlCode(dataModel));
        if (dataModel.getTlxTipLec() != 5) {
            dataModel.setEstadoLectura(DataFragment.estados_lectura.Leido.ordinal());
        } else {
            dataModel.setEstadoLectura(DataFragment.estados_lectura.Postergado.ordinal());
        }
        return true;
    }

    private void sendRes() {
        Hashtable<String, String> params = prepareDataToPost(getApplicationContext(), user);
        new PostRequest(getApplicationContext(), params, null, Urls.urlSubida(getApplicationContext()), new CallbackAPI() {
            @Override
            public void onSuccess(String result, int statusCode) {
                Log.e(TAG, "onSuccess: " + result);
                Toast.makeText(TestActivity.this, "Ressultados enviados", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(String reason, int statusCode) {
                Log.e(TAG, "onFailed: " + reason);
                Toast.makeText(TestActivity.this, reason, Toast.LENGTH_SHORT).show();
            }
        }).execute();
    }

    private void saveLectura(DataModel dataModel, Obs obs) {
        ContentValues cv = new ContentValues();
        Calendar calendar = Calendar.getInstance();

        cv.put(DataModel.Columns.TlxHorLec.name(), StringUtils.getHumanHour(calendar.getTime()));
        cv.put(DataModel.Columns.TlxNvaLec.name(), dataModel.getTlxNvaLec());
        cv.put(DataModel.Columns.TlxImpEn.name(), dataModel.getTlxImpEn());
        cv.put(DataModel.Columns.TlxConsumo.name(), dataModel.getTlxConsumo());
        cv.put(DataModel.Columns.TlxConsFacturado.name(), dataModel.getTlxConsFacturado());
        cv.put(DataModel.Columns.TlxImpTot.name(), dataModel.getTlxImpTot());
        cv.put(DataModel.Columns.TlxImpPot.name(), dataModel.getTlxImpPot());

        cv.put(DataModel.Columns.TlxDesTdi.name(), dataModel.getTlxDesTdi());
        cv.put(DataModel.Columns.TlxLey1886.name(), dataModel.getTlxLey1886());
        cv.put(DataModel.Columns.TlxPotLei.name(), dataModel.getTlxPotLei());
        cv.put(DataModel.Columns.TlxUltObs.name(), dataModel.getTlxUltObs());
        cv.put(DataModel.Columns.TlxPreNue1.name(), dataModel.getTlxPreNue1());
        cv.put(DataModel.Columns.TlxPreNue2.name(), dataModel.getTlxPreNue2());
        cv.put(DataModel.Columns.TlxPreNue3.name(), dataModel.getTlxPreNue3());
        cv.put(DataModel.Columns.TlxPreNue4.name(), dataModel.getTlxPreNue4());

        cv.put(DataModel.Columns.TlxKwInst.name(), dataModel.getTlxKwInst());
        cv.put(DataModel.Columns.TlxReactiva.name(), dataModel.getTlxReactiva());
        cv.put(DataModel.Columns.TlxKwhAlto.name(), dataModel.getTlxKwhAlto());
        cv.put(DataModel.Columns.TlxKwhMedio.name(), dataModel.getTlxKwhMedio());
        cv.put(DataModel.Columns.TlxKwhBajo.name(), dataModel.getTlxKwhBajo());
        cv.put(DataModel.Columns.TlxDemAlto.name(), dataModel.getTlxDemAlto());
        cv.put(DataModel.Columns.TlxHoraAlto.name(), dataModel.getTlxHoraAlto());
        cv.put(DataModel.Columns.TlxFechaAlto.name(), dataModel.getTlxFechaAlto());
        cv.put(DataModel.Columns.TlxDemMedio.name(), dataModel.getTlxDemMedio());
        cv.put(DataModel.Columns.TlxHoraMedio.name(), dataModel.getTlxHoraMedio());
        cv.put(DataModel.Columns.TlxFechaMedio.name(), dataModel.getTlxFechaMedio());
        cv.put(DataModel.Columns.TlxDemBajo.name(), dataModel.getTlxDemBajo());
        cv.put(DataModel.Columns.TlxHoraBajo.name(), dataModel.getTlxHoraBajo());
        cv.put(DataModel.Columns.TlxFechaBajo.name(), dataModel.getTlxFechaBajo());

        cv.put(DataModel.Columns.TlxImpFac.name(), dataModel.getTlxImpFac());
        cv.put(DataModel.Columns.TlxCarFij.name(), dataModel.getTlxCarFij());
        cv.put(DataModel.Columns.TlxImpTap.name(), dataModel.getTlxImpTap());
        cv.put(DataModel.Columns.TlxImpAse.name(), dataModel.getTlxImpAse());
        cv.put(DataModel.Columns.TlxKwhDev.name(), dataModel.getTlxKwhDev());
        cv.put(DataModel.Columns.TlxRecordatorio.name(), dataModel.getTlxRecordatorio());
        cv.put(DataModel.Columns.estado_lectura.name(), dataModel.getEstadoLectura());

        cv.put(DataModel.Columns.TlxCodCon.name(), dataModel.getTlxCodCon());
        cv.put(DataModel.Columns.TlxTipLec.name(), dataModel.getTlxTipLec());
        cv.put(DataModel.Columns.TlxImpAvi.name(), dataModel.getTlxImpAvi());

        dbAdapter.updateData(dataModel.getId(), cv);
        dbAdapter.close();

        cv = new ContentValues();
        cv.put(DataObs.Columns.general_id.name(), dataModel.getId());
        cv.put(DataObs.Columns.observacion_id.name(), obs.getId());
        dbAdapter.saveObject(DBHelper.DATA_OBS_TABLE, cv);
        dbAdapter.close();
    }

    private ContentValues stringNull(String key, String string) {
        ContentValues values = new ContentValues();
        if (string.equalsIgnoreCase("null")) {
            values.putNull(key);
        } else {
            values.put(key, string);
        }
        return values;
    }

    private void startTest() {
        new AsyncTask<Boolean, Void, Boolean>() {


            @Override
            protected Boolean doInBackground(Boolean... booleen) {
//                ArrayList<Integer> integers = new ArrayList<>();
//                integers.add(2985);
//                integers.add(3036);
//                integers.add(5529);
//                for (int id : integers) {
//                    DataModel data = dbAdapter.getData(id);
//                    Resultados dataRes = dbAdapter.getDataRes(data.getId());
//                    inicio(data, dataRes.getLectura(), dataRes.getObservacion());
//                }

                for (DataModel dataModel : dbAdapter.getAllData()) {
                    Resultados dataRes = dbAdapter.getDataRes(dataModel.getId());
                    inicio(dataModel, dataRes.getLectura(), dataRes.getObservacion());
                    sleeper(5);
                }
                Log.e(TAG, "doInBackground: finish");

                return Boolean.TRUE;
            }

        }.execute();
    }
}
