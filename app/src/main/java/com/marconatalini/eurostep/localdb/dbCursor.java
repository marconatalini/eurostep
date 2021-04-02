package com.marconatalini.eurostep.localdb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import androidx.collection.ArrayMap;

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

    public void saveRecord(String cod_lav, String ordine_lotto, String operatore, long seconds, float bilancelle, String carrello, long multiordine)
    {
        Log.d("meo", "saveRecord: " + ordine_lotto);
        ContentValues values = new ContentValues();
        values.put(db_eurostep.registro.COLUMN_NAME_LAVORAZIONE, cod_lav);
        values.put(db_eurostep.registro.COLUMN_NAME_ORDINE_LOTTO, ordine_lotto);
        values.put(db_eurostep.registro.COLUMN_NAME_OPERATORE, operatore);
        values.put(db_eurostep.registro.COLUMN_NAME_SECONDI, seconds);
        values.put(db_eurostep.registro.COLUMN_NAME_BILANCELLE, bilancelle);
        values.put(db_eurostep.registro.COLUMN_NAME_CARRELLO, carrello);
        values.put(db_eurostep.registro.COLUMN_NAME_MULTIORDINE, multiordine);
        db.insert(db_eurostep.registro.TABLE_NAME, null, values);
    }

    public ArrayMap getFirstRecord()
    {
        Cursor c = db.rawQuery("SELECT * FROM registro ORDER BY _id", null);

        ArrayMap record = new ArrayMap<>();

        if (c.moveToFirst()) {
            record.put("id", c.getLong(c.getColumnIndexOrThrow(db_eurostep.registro.COLUMN_NAME_REGISTRAZIONE_ID)));
            record.put("cod_lav", c.getString(c.getColumnIndexOrThrow(db_eurostep.registro.COLUMN_NAME_LAVORAZIONE)));
            record.put("ordine_lotto", c.getString(c.getColumnIndexOrThrow(db_eurostep.registro.COLUMN_NAME_ORDINE_LOTTO)));
            record.put("operatore", c.getString(c.getColumnIndexOrThrow(db_eurostep.registro.COLUMN_NAME_OPERATORE)));
            record.put("seconds", c.getLong(c.getColumnIndexOrThrow(db_eurostep.registro.COLUMN_NAME_SECONDI)));
            record.put("bilancelle", c.getLong(c.getColumnIndexOrThrow(db_eurostep.registro.COLUMN_NAME_BILANCELLE)));
            record.put("carrello", c.getString(c.getColumnIndexOrThrow(db_eurostep.registro.COLUMN_NAME_CARRELLO)));
            record.put("multiordine", c.getLong(c.getColumnIndexOrThrow(db_eurostep.registro.COLUMN_NAME_MULTIORDINE)));
            record.put("timestamp", c.getString(c.getColumnIndexOrThrow(db_eurostep.registro.COLUMN_NAME_TIMESTAMP)));
        }

        return record;
    }

    public void deleteRecord(long id)
    {
        db.execSQL(String.format("DELETE FROM registro WHERE _id =%d", id));
    }

    /*public void deleteFirstRecord()
    {
        Cursor c = db.rawQuery("SELECT _id, dati, timestamp FROM registro ORDER BY _id", null);

        if (c.moveToFirst()) {
            Long id = c.getLong(c.getColumnIndexOrThrow(db_eurostep.registro.COLUMN_NAME_REGISTRAZIONE_ID));
            db.execSQL(String.format("DELETE FROM registro WHERE _id =%d", id));
        }

    }*/

    public void playNotifica() {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(context, notification);
        r.play();
    }
}
