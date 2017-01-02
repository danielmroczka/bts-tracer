package com.labs.dm.bts.entity;

/**
 * Created by daniel on 2016-12-27.
 * <p>
 * MCC - a Mobile Country Code.
 * MNC - a Mobile Network Code.
 * LAC - Location Area Code
 * CID - Cell ID
 */

public class Cell {
    public static final String NAME = "CELL";

    private int id, mcc, mnc, lac, cid;

    private double lat, lon;

    public Cell(int mcc, int mnc, int lac, int cid) {
        this.mcc = mcc;
        this.mnc = mnc;
        this.lac = lac;
        this.cid = cid;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMcc() {
        return mcc;
    }

    public int getMnc() {
        return mnc;
    }

    public int getLac() {
        return lac;
    }

    public int getCid() {
        return cid;
    }
}
