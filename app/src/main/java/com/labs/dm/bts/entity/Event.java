package com.labs.dm.bts.entity;

/**
 * Created by daniel on 2016-12-27.
 */

public class Event {

    public static final String NAME = "EVENT";

    private long id;
    private long timestamp;
    private long cellId;
    private long recordId;

    public Event(long timestamp, long cellId, long recordId) {
        this.timestamp = timestamp;
        this.cellId = cellId;
        this.recordId = recordId;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getCellId() {
        return cellId;
    }

    public long getRecordId() {
        return recordId;
    }
}
