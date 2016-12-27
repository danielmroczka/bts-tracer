package bts.dm.labs.com.bts_tracer.entity;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
        db.execSQL("create table CELL_LOG(id INTEGER PRIMARY KEY, mcc INTEGER, mnc INTEGER, lac INTEGER, cid INTEGER, lat REAL, lon REAL, cellgroup INTEGER, status INTEGER, FOREIGN KEY(cellgroup) REFERENCES CELL_GROUP(id) ON DELETE CASCADE)");
        // CREATE INDEX
        db.execSQL("create unique index CELL_LOG_UNIQUE_IDX on CELL_LOG(mcc, mnc, lac, cid, cellgroup)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
