package com.marconatalini.eurostep.localdb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class dbCursor {

    Context context;
    SQLiteDatabase db;

    public dbCursor(Context context) {
        this.context = context;
        db_eurostep.EurostepDbHelper mDbHelper = new db_eurostep.EurostepDbHelper(context);
        this.db = mDbHelper.getWritableDatabase();
    }

    public void close()
    {
        db.close();
    }

    public long getRegistrazioniLocali()
    {
        long res = 0;
        Cursor c = db.rawQuery("SELECT count(*) FROM registro",null);
        if (c.moveToFirst()){
            res = c.getLong(0);
        }

        return res;
    }

    public void saveRecord(String dati)
    {
        ContentValues values = new ContentValues();
        values.put(db_eurostep.registro.COLUMN_NAME_DATI, dati);
        db.insert(db_eurostep.registro.TABLE_NAME, null, values);
    }

    public String getFirstRecord()
    {
        Cursor c = db.rawQuery("SELECT _id, dati, timestamp FROM registro ORDER BY _id", null);

        String str_dati = null;
        String str_time = null;

        if (c.moveToFirst()) {
            Long id = c.getLong(c.getColumnIndexOrThrow(db_eurostep.registro.COLUMN_NAME_REGISTRAZIONE_ID));
            str_dati = c.getString(c.getColumnIndexOrThrow(db_eurostep.registro.COLUMN_NAME_DATI));
            str_time = c.getString(c.getColumnIndexOrThrow(db_eurostep.registro.COLUMN_NAME_TIMESTAMP));
        }

        if (str_dati.length() > 0) {
            URL url= null;
            URI uri= null;
            try {
                url = new URL(String.format("%s&registrato_il=%s",str_dati, str_time));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            try {
                assert url != null;
                uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

            assert uri != null;
            return uri.toASCIIString();
        }

        return null;
    }

    public void deleteFirstRecord()
    {
        Cursor c = db.rawQuery("SELECT _id, dati, timestamp FROM registro ORDER BY _id", null);

        if (c.moveToFirst()) {
            Long id = c.getLong(c.getColumnIndexOrThrow(db_eurostep.registro.COLUMN_NAME_REGISTRAZIONE_ID));
            db.execSQL(String.format("DELETE FROM registro WHERE _id =%d", id));
        }

    }

    public void playNotifica() {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(context, notification);
        r.play();
    }
}
