package com.solunes.endeappbeni.activities;

import android.content.ContentValues;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.solunes.endeappbeni.R;
import com.solunes.endeappbeni.dataset.DBAdapter;
import com.solunes.endeappbeni.fragments.DataFragment;
import com.solunes.endeappbeni.models.DataModel;
import com.solunes.endeappbeni.models.Obs;
import com.solunes.endeappbeni.models.Resultados;
import com.solunes.endeappbeni.models.User;
import com.solunes.endeappbeni.networking.CallbackAPI;
import com.solunes.endeappbeni.networking.GetRequest;
import com.solunes.endeappbeni.networking.PostRequest;
import com.solunes.endeappbeni.networking.Token;
import com.solunes.endeappbeni.utils.GenLecturas;
import com.solunes.endeappbeni.utils.Urls;
import com.solunes.endeappbeni.utils.UserPreferences;

import org.json.JSONException;

import java.util.Hashtable;

import static com.solunes.endeappbeni.activities.MainActivity.prepareDataToPost;
import static com.solunes.endeappbeni.activities.MainActivity.processResponse;

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
                                    response = processResponse(getApplicationContext(),result);
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
        boolean isCalculo = DataFragment.calculo(getApplicationContext(),
                dataModel,
                finalTipoLectura,
                finalNuevaLectura,
                finalLecturaKwh,
                0,
                obs);
        if (isCalculo) {
            Log.e(TAG, "confirmarLectura: calculo exitoso: " + dataModel.getId());
            DataFragment.saveDataModel(getApplicationContext(), dataModel);
        } else {
            Log.e(TAG, "confirmarLectura: error en: " + dataModel.getId());
        }
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
