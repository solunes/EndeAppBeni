package com.solunes.endeappbeni.activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Looper;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.solunes.endeappbeni.R;
import com.solunes.endeappbeni.adapters.PagerAdapter;
import com.solunes.endeappbeni.dataset.DBAdapter;
import com.solunes.endeappbeni.fragments.DataFragment;
import com.solunes.endeappbeni.models.DataModel;
import com.solunes.endeappbeni.models.MedEntreLineas;
import com.solunes.endeappbeni.models.User;
import com.solunes.endeappbeni.utils.UserPreferences;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.PrinterStatus;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;
import com.zebra.sdk.printer.ZebraPrinterLinkOs;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;

/**
 * Esta actividad muestra las lecturas de la aplicacion
 */
public class ReadingActivity extends AppCompatActivity implements DataFragment.OnFragmentListener, SearchView.OnQueryTextListener {

    private static final String TAG = "ReadingActivity";
    public static final String KEY_LAST_PAGER_PSOTION = "last_pager_position";

    private ZebraPrinter printer;
    private Connection connection;

    private ViewPager viewPager;
    private TabLayout tabLayout;
    private PagerAdapter adapter;
    private SearchView searchView;
    private MenuItem searchItem;
    private Menu menu;
    private RadioGroup radioGroup;
    private RadioButton radioCli;
    private Snackbar snackbar;
    private Toolbar toolbar;

    private ArrayList<DataModel> datas;
    private User user;

    private int currentState = -1;
    private int printState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);

        DBAdapter dbAdapter = new DBAdapter(this);
        user = dbAdapter.getUser(UserPreferences.getInt(this, LoginActivity.KEY_LOGIN_ID));
        datas = new ArrayList<>();
        if (getIntent().getExtras() != null) {
            int filter = getIntent().getExtras().getInt(MainActivity.KEY_FILTER);
            currentState = filter;
            switch (filter) {
                case MainActivity.KEY_READY:
                    datas = dbAdapter.getReady();
                    break;
                case MainActivity.KEY_MISSING:
                    datas = dbAdapter.getState(0);
                    break;
                case MainActivity.KEY_PRINT:
                    datas = dbAdapter.getState(1);
                    break;
                case MainActivity.KEY_POSTPONED:
                    datas = dbAdapter.getState(2);
                    break;
            }
        } else {
            datas = dbAdapter.getAllData();
        }
        adapter = new PagerAdapter(getSupportFragmentManager(), datas.size(), datas, user.getLecId());
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        for (int i = 0; i < adapter.getCount(); i++) {
            TabLayout.Tab tabAt = tabLayout.getTabAt(i);
            View inflate = LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
            TextView tabText = (TextView) inflate.findViewById(R.id.textview_custom_tab);
            tabText.setText(i + 1 + "");
            if (datas.get(i).getEstadoLectura() != DataFragment.estados_lectura.Pendiente.ordinal()
                    && datas.get(i).getEstadoLectura() != DataFragment.estados_lectura.PostergadoTmp.ordinal()) {
                tabText.setTextColor(getResources().getColor(android.R.color.white));
            }
            tabAt.setCustomView(inflate);
        }
        dbAdapter.close();

        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                doConnection();
                Looper.loop();
                Looper.myLooper().quit();
            }
        }).start();

        int idData = UserPreferences.getInt(getApplicationContext(), KEY_LAST_PAGER_PSOTION);
        for (int i = 0; i < datas.size(); i++) {
            if (datas.get(i).getId() == idData) {
                viewPager.setCurrentItem(i);
                break;
            }
        }

        radioGroup = (RadioGroup) findViewById(R.id.radio_group);
        radioCli = (RadioButton) findViewById(R.id.cli_radio);
    }

    @Override
    public void onTabListener() {
        View customView = tabLayout.getTabAt(viewPager.getCurrentItem()).getCustomView();
        TextView textTab = (TextView) customView.findViewById(R.id.textview_custom_tab);
        textTab.setTextColor(getResources().getColor(android.R.color.white));
    }

    @Override
    public void onPrinting(final String srcToPrint) {
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                sendLabelToPrint(srcToPrint);
                Looper.loop();
                Looper.myLooper().quit();
            }
        }).start();
    }

    @Override
    public void onAjusteOrden(int idData) {
        DBAdapter dbAdapter = new DBAdapter(this);
        DataModel dataLastSaved = dbAdapter.getLastSaved();
        DataModel dataCurrent = dbAdapter.getData(idData);
        dbAdapter.orderPendents(dataCurrent.getTlxOrdTpl());
        if (dataLastSaved == null) {
            dataCurrent.setTlxOrdTpl(0);
        } else {
            dataCurrent.setTlxOrdTpl(dataLastSaved.getTlxOrdTpl() + 1);
        }
        ContentValues values = new ContentValues();
        values.put(DataModel.Columns.TlxOrdTpl.name(), dataCurrent.getTlxOrdTpl());
        dbAdapter.updateData(dataCurrent.getId(), values);
        dbAdapter.close();
    }

    @Override
    public void onNextPage() {
        int currentItem = viewPager.getCurrentItem();
        if (currentItem < adapter.getCount()) {
            viewPager.setCurrentItem(currentItem + 1);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                doDisconnect();
                finish();
                return true;
            case R.id.action_search:
                return true;
            case R.id.action_print:
                reconnect();
                return true;
        }
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        UserPreferences.putInt(this, KEY_LAST_PAGER_PSOTION, datas.get(viewPager.getCurrentItem()).getId());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        doDisconnect();
    }

    private void doDisconnect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                disconnect();
            }
        }).start();
    }

    private void doConnection() {
        printer = connect();
    }

    private String getMacAddress() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Log.e(TAG, "getMacAddress: " + bluetoothAdapter);
        if (bluetoothAdapter != null) {
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    String printerName = UserPreferences.getString(getApplicationContext(), AdminActivity.KEY_PRINT_MANE);
                    if (device.getName().equalsIgnoreCase(printerName)) {
                        Log.e(TAG, "onBluetooth: " + device.getName() + " - " + device.getAddress());
                        return device.getAddress();
                    }
                }
            }
        } else {
            snackbar.setText("Bluetooth apagado");
        }
        return null;
    }

    public ZebraPrinter connect() {
        Log.e(TAG, "Connecting... ");
        snackbar = Snackbar.make(viewPager, "Conectando con la impresora", Snackbar.LENGTH_LONG);
        snackbar.show();
        connection = null;
        String macAddress = getMacAddress();
        connection = new BluetoothConnection(macAddress);
//        SettingsHelper.saveBluetoothAddress(this, getMacAddressFieldText());

        try {
            Log.e(TAG, "connect: " + connection);
            Log.e(TAG, "connect: " + connection.isConnected());
            connection.open();
            Log.e(TAG, "Connected");
            checkStatus(1);
        } catch (ConnectionException e) {
            Log.d(TAG, "Comm Error! Disconnecting", e);
            sleeper(500);
            checkStatus(0);
            disconnect();
        }

        ZebraPrinter printer = null;

        if (connection.isConnected()) {
            try {
                printer = ZebraPrinterFactory.getInstance(connection);
            } catch (ConnectionException | ZebraPrinterLanguageUnknownException e) {
                printer = null;
                sleeper(500);
                disconnect();
            }
        }

        return printer;
    }

    private void reconnect() {
        Log.e(TAG, "print state: " + printState);
        if (printState == 0) {
            new Thread(new Runnable() {
                public void run() {
                    Looper.prepare();
                    doConnection();
                    Looper.loop();
                    Looper.myLooper().quit();
                }
            }).start();
        } else {
            Snackbar.make(viewPager, "Impresora ya conectada", Snackbar.LENGTH_SHORT).show();
        }
    }

    public void disconnect() {
        try {
            Log.e(TAG, "Disconnecting");
            if (connection != null) {
                connection.close();
            }
            checkStatus(0);
            Log.e(TAG, "Not Connected");
        } catch (ConnectionException e) {
            Log.e(TAG, "COMM Error! Disconnected");
        }
    }

    private void sendLabelToPrint(String label) {
        if (connection.isConnected()) {
            try {
                ZebraPrinterLinkOs linkOsPrinter = ZebraPrinterFactory.createLinkOsPrinter(printer);
                PrinterStatus printerStatus = (linkOsPrinter != null) ? linkOsPrinter.getCurrentStatus() : printer.getCurrentStatus();
                if (printerStatus.isReadyToPrint) {
                    connection.write(label.getBytes("ISO-8859-1"));
                    Log.e(TAG, "sending data");
                    Snackbar.make(viewPager, "Imprimiendo", Snackbar.LENGTH_LONG).show();
                } else if (printerStatus.isHeadOpen) {
                    Log.e(TAG, "printer head open");
                    Snackbar.make(viewPager, "Cabezal abierto, cierrelo y vuelva a imprimir", Snackbar.LENGTH_LONG).show();
                } else if (printerStatus.isPaused) {
                    Log.e(TAG, "printer is paused");
                    Snackbar.make(viewPager, "Impresora pausada", Snackbar.LENGTH_LONG).show();
                } else if (printerStatus.isPaperOut) {
                    Log.e(TAG, "printer media out");
                    Snackbar.make(viewPager, "Impresora sin papel", Snackbar.LENGTH_LONG).show();
                }
                sleeper(500);
                if (connection instanceof BluetoothConnection) {
                    String friendlyName = ((BluetoothConnection) connection).getFriendlyName();
                    Log.e(TAG, friendlyName);
                    sleeper(500);
                }
            } catch (ConnectionException e) {
                Log.d(TAG, e.getMessage(), e);
                checkStatus(0);
            } catch (UnsupportedEncodingException e) {
                Log.d(TAG, "sendLabelToPrint: ", e);
            }
        } else {
            checkStatus(0);
        }
    }

    private void sleeper(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_search, menu);
        searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setInputType(InputType.TYPE_CLASS_NUMBER);
        searchView.setOnQueryTextListener(this);
        searchView.setIconifiedByDefault(false);
        searchView.setSubmitButtonEnabled(false);
        searchView.setQueryHint("Cliente o Medidor");

        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                radioGroup.setVisibility(View.VISIBLE);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                radioGroup.setVisibility(View.GONE);
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        String filter = !TextUtils.isEmpty(query) ? query : null;
        DBAdapter dbAdapter = new DBAdapter(this);
        boolean isCli = radioCli.isChecked();
        Cursor cursor = dbAdapter.searchClienteMedidor(filter, isCli, currentState);
        if (cursor.getCount() > 0) {
            DataModel dataModel = DataModel.fromCursor(cursor);
            viewPager.setCurrentItem(adapter.getItemPosition(dataModel));
            searchItem.collapseActionView();
            searchView.onActionViewCollapsed();
        } else {
            Snackbar.make(searchView, "no hay conincidencias", Snackbar.LENGTH_SHORT).show();
        }
        cursor.close();
        dbAdapter.close();
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    public Hashtable<String, String> prepareDataToPost() {
        Hashtable<String, String> params = new Hashtable<>();

        DBAdapter dbAdapter = new DBAdapter(this);
        ArrayList<DataModel> allData = dbAdapter.getAllDataToSend();

        params.put("UsrCod", String.valueOf(user.getLecCod()));

        for (DataModel dataModel : allData) {
            String json = DataModel.getJsonToSend(dataModel,
                    dbAdapter.getDataObsByCli(dataModel.getId()),
                    dbAdapter.getPrintObsData(dataModel.getId()),
                    dbAdapter.getDetalleFactura(dataModel.getId()));
            Log.e(TAG, "prepareDataToPost json: " + json);
            params.put("" + (dataModel.getTlxCli()), json);
        }

        try {
            ArrayList<MedEntreLineas> entreLineasList = dbAdapter.getMedEntreLineas();
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < entreLineasList.size(); i++) {
                MedEntreLineas entreLineas = entreLineasList.get(i);
                jsonArray.put(i, entreLineas.toJson());
            }
            Log.e(TAG, "prepareDataToPost: mel: " + jsonArray.toString());
            params.put("med_entre_lineas", jsonArray.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        dbAdapter.close();
        return params;
    }

    private void checkStatus(int status) {
        printState = status;

        if (printState == 1) {
            Snackbar.make(viewPager, "Impresora conectada", Snackbar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(viewPager, "Impresora desconectada, intente reconectar", Snackbar.LENGTH_LONG).show();
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MenuItem item = menu.getItem(0);
                if (printState == 0) {
                    item.setIcon(getResources().getDrawable(R.drawable.ic_no_print_white_24dp));
                } else {
                    item.setIcon(getResources().getDrawable(R.drawable.ic_print_white_24dp));
                }
            }
        });
    }


}
