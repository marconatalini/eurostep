package com.marconatalini.eurostep.localdb;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Marco on 03/06/2016.
 */
public class db_eurostep {
    // To prevent someone from accidentally instantiating the class,
    // give it an empty constructor.
    public db_eurostep() {}

    private static final String TEXT_TYPE = " TEXT";
    private static final String DATETIME_TYPE = " DATETIME";
    private static final String TIMESTAMP_TYPE = " TIMESTAMP";
    private static final String BLOB_TYPE = " BLOB";
    private static final String INT_TYPE = " INTEGER";
    private static final String REAL_TYPE = " REAL";
    private static final String COMMA_SEP = ",";

    /* Inner class that defines the table contents */
    public static abstract class registro implements BaseColumns {
    public static final String TABLE_NAME = "registro";
    public static final String COLUMN_NAME_REGISTRAZIONE_ID = "_id";
    public static final String COLUMN_NAME_LAVORAZIONE = "lavorazione";
    public static final String COLUMN_NAME_ORDINE_LOTTO = "ordine_lotto";
    public static final String COLUMN_NAME_OPERATORE = "operatore";
    public static final String COLUMN_NAME_SECONDI = "secondi";
    public static final String COLUMN_NAME_BILANCELLE = "bilancelle";
    public static final String COLUMN_NAME_CARRELLO = "carrello";
    public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
    }

    private static final String SQL_CREATE_REGISTRO =
            "CREATE TABLE " + registro.TABLE_NAME + " (" +
                    registro.COLUMN_NAME_REGISTRAZIONE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    registro.COLUMN_NAME_LAVORAZIONE + TEXT_TYPE + COMMA_SEP +
                    registro.COLUMN_NAME_ORDINE_LOTTO + TEXT_TYPE + COMMA_SEP +
                    registro.COLUMN_NAME_OPERATORE + TEXT_TYPE + COMMA_SEP +
                    registro.COLUMN_NAME_SECONDI + INT_TYPE + COMMA_SEP +
                    registro.COLUMN_NAME_BILANCELLE + REAL_TYPE + COMMA_SEP +
                    registro.COLUMN_NAME_CARRELLO + TEXT_TYPE + COMMA_SEP +
                    registro.COLUMN_NAME_TIMESTAMP + TIMESTAMP_TYPE + " DEFAULT (DATETIME(CURRENT_TIMESTAMP))" +
                    " )";

    private static final String SQL_DELETE_REGISTRO =
            "DROP TABLE IF EXISTS " + registro.TABLE_NAME;

    public static class EurostepDbHelper extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.
        public static final int DATABASE_VERSION = 8;
        public static final String DATABASE_NAME = "eurostep.db";

        public EurostepDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_REGISTRO);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(SQL_DELETE_REGISTRO);
            onCreate(db);
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }

        public ArrayList<Cursor> getData(String Query){
            //get writable database
            SQLiteDatabase sqlDB = this.getWritableDatabase();
            String[] columns = new String[] { "message" };
            //an array list of cursor to save two cursors one has results from the query
            //other cursor stores error message if any errors are triggered
            ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
            MatrixCursor Cursor2= new MatrixCursor(columns);
            alc.add(null);
            alc.add(null);

            try{
                String maxQuery = Query ;
                //execute the query results will be save in Cursor c
                Cursor c = sqlDB.rawQuery(maxQuery, null);

                //add value to cursor2
                Cursor2.addRow(new Object[] { "Success" });

                alc.set(1,Cursor2);
                if (null != c && c.getCount() > 0) {

                    alc.set(0,c);
                    c.moveToFirst();

                    return alc ;
                }
                return alc;
            } catch(SQLException sqlEx){
                Log.d("printing exception", sqlEx.getMessage());
                //if any exceptions are triggered save the error message to cursor an return the arraylist
                Cursor2.addRow(new Object[] { ""+sqlEx.getMessage() });
                alc.set(1,Cursor2);
                return alc;
            } catch(Exception ex){
                Log.d("printing exception", ex.getMessage());

                //if any exceptions are triggered save the error message to cursor an return the arraylist
                Cursor2.addRow(new Object[] { ""+ex.getMessage() });
                alc.set(1,Cursor2);
                return alc;
            }
        }
    }
}

