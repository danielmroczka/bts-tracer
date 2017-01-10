package com.labs.dm.bts;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Xml;

import com.labs.dm.bts.entity.DBManager;
import com.labs.dm.bts.entity.Event;
import com.labs.dm.bts.entity.Record;

import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
        long recordId = intent.getLongExtra("recordId", -1);
        if (recordId >= 0) {

            DBManager db = DBManager.getInstance(this);
            Record record = db.getRecord((int) recordId);

            List<Event> events = db.getEvents((int) recordId);
            try {
                createXml(record, events);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }

//    private void send() {
//        Intent shareIntent = new Intent();
//        shareIntent.setAction(Intent.ACTION_SEND);
//        shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//
//        File sd = Environment.getExternalStorageDirectory();
//
//        File attachment = new File(sd, selectedFile);
//        Uri uri = Uri.fromFile(attachment);
//
//        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
//        shareIntent.setType("application/octet-stream");
//        getApplicationContext().startActivityForResult(Intent.createChooser(shareIntent, "Send to..."), 123);
//    }

    void createXml(Record record, List<Event> events) throws IOException {
        File sd = Environment.getExternalStorageDirectory();
        String filename = "bts.txt";

        FileOutputStream fos;

        fos = openFileOutput(filename, Context.MODE_APPEND);


        XmlSerializer serializer = Xml.newSerializer();
        serializer.setOutput(fos, "UTF-8");
        serializer.startDocument(null, Boolean.valueOf(true));
        serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

        serializer.startTag(null, "root");

        for (int j = 0; j < events.size(); j++) {

            serializer.startTag(null, "event");

            serializer.startTag(null, "CID");
            serializer.text(String.valueOf(events.get(j).getCell().getCid()));
            serializer.endTag(null, "CID");
            serializer.startTag(null, "LAC");
            serializer.text(String.valueOf(events.get(j).getCell().getLac()));
            serializer.endTag(null, "LAC");

            serializer.startTag(null, "timestamp");
            serializer.text((String.valueOf(events.get(j).getTimestamp())));
            serializer.endTag(null, "timestamp");

            serializer.endTag(null, "event");
        }
        serializer.endTag(null, "root");
        serializer.endDocument();

        serializer.flush();

        fos.close();

    }
}
