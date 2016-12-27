package bts.dm.labs.com.bts_tracer;

import android.content.Context;
import android.content.Intent;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.widget.TextView;

import java.util.Date;

/**
 * Created by daniel on 2016-12-26.
 */

public class MyPhoneListener extends PhoneStateListener {

    private final Context context;

    public MyPhoneListener(Context context) {
        this.context = context;
    }

    @Override
    public void onCellLocationChanged(CellLocation location) {
        super.onCellLocationChanged(location);
        context.sendBroadcast(new Intent("change_cell"));    }
}
