package com.labs.dm.bts.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Xml;

import com.labs.dm.bts.entity.DBManager;
import com.labs.dm.bts.entity.Event;
import com.labs.dm.bts.entity.Record;

import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Created by Daniel Mroczka on 02-Jan-17.
 */

public class UploadService extends IntentService {

    public UploadService() {
        super("UploadService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int recordId = intent.getIntExtra("recordId", -1);
        if (recordId >= 0) {

            DBManager db = DBManager.getInstance(this);
            Record record = db.getRecord(recordId);

            List<Event> events = db.getEvents(recordId);
            try {
                File out = createXml(record, events);
                Intent xml = new Intent("XML_CREATED");
                xml.putExtra("path", out.getPath());
                sendBroadcast(xml);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }

    private File createXml(Record record, List<Event> events) throws IOException {
        String filename = getExternalCacheDir() + "/bts_" + record.getName() + ".xml";
        FileOutputStream fos;

        File out = new File(filename);
        fos = new FileOutputStream(out);

        XmlSerializer serializer = Xml.newSerializer();
        serializer.setOutput(fos, "UTF-8");
        serializer.startDocument(null, Boolean.valueOf(true));
        serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

        serializer.startTag(null, "root");
        serializer.attribute("", "record", record.getName());
        for (int j = 0; j < events.size(); j++) {
            serializer.startTag(null, "event");
            serializer.attribute("", "time", new Date(events.get(j).getTimestamp()).toString());
            serializer.startTag(null, "CID");
            serializer.text(String.valueOf(events.get(j).getCell().getCid()));
            serializer.endTag(null, "CID");
            serializer.startTag(null, "LAC");
            serializer.text(String.valueOf(events.get(j).getCell().getLac()));
            serializer.endTag(null, "LAC");
            serializer.endTag(null, "event");
        }
        serializer.endTag(null, "root");
        serializer.endDocument();

        serializer.flush();

        fos.close();

        return out;
    }
}
