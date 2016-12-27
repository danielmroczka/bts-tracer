package bts.dm.labs.com.bts_tracer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import bts.dm.labs.com.bts_tracer.entity.DBManager;

import static android.telephony.PhoneStateListener.LISTEN_NONE;

public class MainActivity extends AppCompatActivity {
    private DBManager db;
    private boolean active = true;
    private MyPhoneStateListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        db = DBManager.getInstance(getApplicationContext());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                active = !active;
                if (active) {
                    register();
                } else {
                    unregister();
                }
                Snackbar.make(view, active ? "Start recording" : "Stop recording", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        checkPermission();

    }

    private void init() {
        TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        log("Operator: " + manager.getNetworkOperatorName() +
                        "(" + manager.getNetworkCountryIso() + ")" +
                        "\nSIM operator: " + manager.getSimOperatorName() +
                        "(" + manager.getSimCountryIso() + ")" +
                        "\nPhone no: " + manager.getLine1Number()

                , false);
    }

    private void register() {
        final TelephonyManager telManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        listener = new MyPhoneStateListener();
        int events = PhoneStateListener.LISTEN_CELL_LOCATION | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS | PhoneStateListener.LISTEN_CELL_INFO;
        telManager.listen(listener, events);
    }

    private void unregister() {
        final TelephonyManager telManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telManager.listen(listener, LISTEN_NONE);
    }

    private void checkPermission() {
        //TODO for Marshmallow:
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {

            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 124);
            }
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {

            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 123);
            }

        } else {
            onInit();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 123:
            case 124: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    onInit();

                } else {
                }
                return;
            }

        }
    }

    private void onInit() {
        init();
        register();
        Intent serviceIntent = new Intent(this, BtsService.class);
        startService(serviceIntent);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregister();
        Intent serviceIntent = new Intent(this, BtsService.class);
        stopService(serviceIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_exit) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private class MyPhoneStateListener extends PhoneStateListener {

        @Override
        public void onCellLocationChanged(CellLocation location) {
            super.onCellLocationChanged(location);
            log(getCellInfo(MainActivity.this));
        }

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            int signalStrengthValue;
            if (signalStrength.isGsm()) {
                if (signalStrength.getGsmSignalStrength() != 99) {
                    signalStrengthValue = signalStrength.getGsmSignalStrength() * 2 - 113;
                } else {
                    signalStrengthValue = signalStrength.getGsmSignalStrength();
                }
            } else {
                signalStrengthValue = signalStrength.getCdmaDbm();
            }
            log("RSSI= " + signalStrengthValue + "dBm");
        }

        @Override
        public void onCellInfoChanged(List<CellInfo> cellInfo) {
            super.onCellInfoChanged(cellInfo);

            if (cellInfo != null) {
                log("cell info : " + cellInfo.size());
            }
        }
    }

    private void log(String text) {
        log(text, true);
    }

    private void log(String text, boolean timestamp) {
        final TextView view = (TextView) findViewById(R.id.view);
        if (timestamp) {
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
            formatter.format(new Date());
            view.append(formatter.format(new Date()));
            view.append("\t");
        }
        view.append(text);
        view.append("\n");
    }

    public static String getCellInfo(Context context) {
        final TelephonyManager tel = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        int mnc = 0, mcc = 0, lac = 0, cid = 0;
        String networkOperator = tel.getNetworkOperator();
        if (!TextUtils.isEmpty(networkOperator)) {
            mcc = Integer.parseInt(networkOperator.substring(0, 3));
            mnc = Integer.parseInt(networkOperator.substring(3));
        }

        if (tel.getCellLocation() instanceof GsmCellLocation) {
            lac = ((GsmCellLocation) tel.getCellLocation()).getLac();
            cid = ((GsmCellLocation) tel.getCellLocation()).getCid();
        } else if (tel.getCellLocation() instanceof CdmaCellLocation) {
            lac = ((CdmaCellLocation) tel.getCellLocation()).getSystemId();
            cid = ((CdmaCellLocation) tel.getCellLocation()).getBaseStationId();
        }

        return String.format("CID: %s, LAC: %s", cid, lac);
    }
}
