package com.labs.dm.bts.entity;

/**
 * Created by daniel on 2016-12-27.
 */

public class Record {

    public static final String NAME = "RECORD";
    private int id;
    private String name;

    public Record(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
}
