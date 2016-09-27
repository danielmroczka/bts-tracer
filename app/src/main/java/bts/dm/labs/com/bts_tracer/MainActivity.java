package bts.dm.labs.com.bts_tracer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
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

import static android.telephony.PhoneStateListener.LISTEN_NONE;

public class MainActivity extends AppCompatActivity {

    private MyPhoneStateListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        final TelephonyManager telManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        int events = PhoneStateListener.LISTEN_CELL_LOCATION;
        listener = new MyPhoneStateListener();
        telManager.listen(listener, events);

        Intent serviceIntent = new Intent(this, BtsService.class);
        startService(serviceIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        final TelephonyManager telManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telManager.listen(listener, LISTEN_NONE);
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

        return super.onOptionsItemSelected(item);
    }


    private class MyPhoneStateListener extends PhoneStateListener {

        @Override
        public void onCellLocationChanged(CellLocation location) {
            super.onCellLocationChanged(location);
            final TextView view = (TextView) findViewById(R.id.view);
            SimpleDateFormat formatter = new SimpleDateFormat("hh:mm:ss sss");
            formatter.format(new Date());
            view.append(formatter.format(new Date()));
            view.append("\t");
            view.append(getCellInfo(MainActivity.this));
            view.append("\n");
        }
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
