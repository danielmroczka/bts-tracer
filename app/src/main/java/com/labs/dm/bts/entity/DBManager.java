package com.labs.dm.bts.entity;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by daniel on 2016-12-27.
 */

public class DBManager extends SQLiteOpenHelper {

    public final static String DB_NAME = "bts.db";
    private static final int DB_VERSION = 1;
    private static DBManager instance;

    public static synchronized DBManager getInstance(Context context) {
        if (instance == null) {
            instance = new DBManager(context);
        }
        return instance;
    }

    private DBManager(Context context, String name) {
        super(context, name, null, DB_VERSION);
    }

    private DBManager(Context context) {
        this(context, DB_NAME);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table RECORD(id INTEGER PRIMARY KEY, name TEXT)");
        db.execSQL("create table CELL(id INTEGER PRIMARY KEY, mcc INTEGER, mnc INTEGER, lac INTEGER, cid INTEGER, lat REAL, lon REAL)");
        db.execSQL("create table EVENT(id INTEGER PRIMARY KEY, cellId INTEGER, recordId INTEGER, FOREIGN KEY(cellId) REFERENCES CELL(id), FOREIGN KEY(recordId) REFERENCES RECORD(id))");
        // CREATE INDEX
        db.execSQL("create unique index CELL_UNIQUE_IDX on CELL(mcc, mnc, lac, cid)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public long createRecord() {
        ContentValues content = new ContentValues();
        content.put("name", new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()));
        return getWritableDatabase().insert(Record.NAME, null, content);
    }

    public long createCell(Cell cell) {
        ContentValues content = new ContentValues();
        content.put("mcc", cell.getMcc());
        content.put("mnc", cell.getMnc());
        content.put("lac", cell.getLac());
        content.put("cid", cell.getCid());
        return getWritableDatabase().insert(Cell.NAME, null, content);
    }

    public long createEvent(Event event) {
        ContentValues content = new ContentValues();
        content.put("timestamp", System.currentTimeMillis());
        content.put("cell", event.getCellId());
        content.put("record", event.getRecordId());
        return getWritableDatabase().insert(Event.NAME, null, content);
    }

    public long createEvent(long recordId, Cell cell) {
        int cellId = getCell(cell);

        if (cellId == -1) {
            cellId = (int) createCell(cell);
        }

        Event event = new Event(System.currentTimeMillis(), cellId, recordId);
        return createEvent(event);
    }

    private int getCell(Cell cell) {
        int id = -1;
        Cursor cursor = null;
        try {
            cursor = getReadableDatabase().query(Cell.NAME, null, "mcc=" + cell.getMcc() + " and mnc=" + cell.getMnc() + " and lac=" + cell.getLac() + " and cid=" + cell.getCid(), null, null, null, null);
            if (cursor.getCount() == 1) {
                cursor.moveToFirst();
                id = cursor.getInt(0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return id;
    }
}
