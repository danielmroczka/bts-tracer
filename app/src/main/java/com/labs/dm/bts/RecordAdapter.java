package com.labs.dm.bts;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.labs.dm.bts.entity.Record;

/**
 * Created by daniel on 2017-01-11.
 */

public class RecordAdapter extends ArrayAdapter<Record> {

    private Context context;
    private Record[] values;

    public RecordAdapter(Context context, int resource, Record[] values) {
        super(context, resource, values);
        this.context = context;
        this.values = values;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView label = new TextView(context);
        label.setTextColor(Color.BLACK);
        label.setText(values[position].getName());
        return label;
    }
}
