package com.labs.dm.bts;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.labs.dm.bts.entity.Cell;
import com.labs.dm.bts.entity.DBManager;
import com.labs.dm.bts.entity.Record;
import com.labs.dm.bts.service.BtsService;
import com.labs.dm.bts.service.UploadService;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static android.telephony.PhoneStateListener.LISTEN_NONE;

public class MainActivity extends AppCompatActivity {
    private DBManager db;
    private boolean active = true;
    private MyPhoneStateListener listener;
    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        db = DBManager.getInstance(getApplicationContext());


        checkPermission();

        receiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                if ("XML_CREATED".equals(intent.getAction())) {
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    String filename = intent.getStringExtra("path");
                    File attachment = new File(filename);
                    Uri uri = Uri.fromFile(attachment);

                    shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                    shareIntent.setType("application/octet-stream");
                    Intent in = Intent.createChooser(shareIntent, "Send to...");
                    startActivityForResult(in, 123);
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction("XML_CREATED");
        registerReceiver(receiver, filter);

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123) {
            for (File file : getExternalCacheDir().listFiles()) {
                if (!file.isDirectory() && file.getName().endsWith(".xml")) {
                    file.delete();
                }
            }
        }
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

    long recordId;

    private void register() {
        final TelephonyManager telManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        recordId = db.createRecord();
        listener = new MyPhoneStateListener(recordId);
        int events = PhoneStateListener.LISTEN_CELL_LOCATION | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS | PhoneStateListener.LISTEN_CELL_INFO;
        telManager.listen(listener, events);
    }

    private void unregister() {
        final TelephonyManager telManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telManager.listen(listener, LISTEN_NONE);
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE}, 113);
        } else {
            onInit();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 113: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onInit();
                }
            }
        }
    }

    private void onInit() {
        init();
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
    public void onBackPressed() {
        moveTaskToBack(true);
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
        if (id == R.id.action_upload) {

            List<Record> list = db.getRecords();
            final Record[] records = list.toArray(new Record[list.size()]);

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
            LayoutInflater inflater = getLayoutInflater();
            View convertView = inflater.inflate(R.layout.custom, null);
            alertDialog.setView(convertView);
            alertDialog.setTitle("Select Record");
            RecordAdapter adapter = new RecordAdapter(this, android.R.layout.simple_list_item_1, records);

            alertDialog.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Record record = records[which];
                    System.out.println(record);

                    if (record.getId() >= 0) {
                        Intent serviceIntent = new Intent(MainActivity.this, UploadService.class);
                        serviceIntent.putExtra("recordId", record.getId());
                        startService(serviceIntent);
                    }
                    dialog.cancel();
                }
            });
            alertDialog.show();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class MyPhoneStateListener extends PhoneStateListener {

        private long recordId;

        public MyPhoneStateListener(long recordId) {
            this.recordId = recordId;
        }

        @Override
        public void onCellLocationChanged(CellLocation location) {
            super.onCellLocationChanged(location);
            Cell cell = getCellInfo(MainActivity.this);
            db.createEvent(recordId, cell);
            log(String.format("CID: %s, LAC: %s", cell.getCid(), cell.getLac()));
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

    public static Cell getCellInfo(Context context) {
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

        return new Cell(mcc, mnc, lac, cid);
    }
}
