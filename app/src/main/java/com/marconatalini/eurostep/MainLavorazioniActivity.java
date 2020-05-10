package com.marconatalini.eurostep;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.integration.android.IntentIntegrator;
import com.marconatalini.eurostep.entity.Registrazione;
import com.marconatalini.eurostep.localdb.dbCursor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.collection.ArrayMap;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class MainLavorazioniActivity extends AppCompatActivity  implements Registrazione.OnRecordSavedListener {

    private Toolbar toolbar;
    private FloatingActionButton fab;
    dbCursor cursor;
    long savedRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_lavorazioni);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //find registrazioni locali
        cursor = new dbCursor(this);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendDatiBatch();
            }
        });

        onRecordSaved();
    }

    private void sendDatiBatch () {
        ArrayMap record = null;

        if (savedRecord >= 0) {
            record = cursor.getFirstRecord();
        }

        String timestamp = null;
        long id = (long) record.get("id");

        try {
            timestamp = URLEncoder.encode((String) record.get("timestamp"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String getURL = String.format("http://%s/online/%s/%s?&ordine_lotto=%s&seconds=%d&bilancelle=%s&registrato_il=%s",
                MainActivity.WEBSERVER_IP, record.get("operatore"), record.get("cod_lav"), record.get("ordine_lotto"),
                record.get("seconds"), record.get("bilancelle"), timestamp);

        snackMsg("Invio dati memoria ... attendi");
        Log.d("meo", getURL);
        StringRequest sRequest = new StringRequest(Request.Method.GET, getURL,
                response -> {
                    cursor.deleteRecord(id);
                    savedRecord -= 1;
                    onRecordSaved();
                },
                error -> {
                    snackMsg(error.toString().substring(0,30) + "... Riprova più tardi");
                    error.printStackTrace();
                });

        MySingleton.getInstance(MainLavorazioniActivity.this).addToRequestque(sRequest);
    }

    @Override
    public void onRecordSaved() {
//        dbCursor cursor = new dbCursor(this);
        savedRecord = cursor.getRegistrazioniLocali();
        if (savedRecord > 0) {
            cursor.playNotifica();
            String msg = String.format("Hai %s registrazioni in memoria.", savedRecord);
            snackMsg(msg);
            fab.setVisibility(View.VISIBLE);
        } else {
            fab.setVisibility(View.INVISIBLE);
        }
    }

    private void snackMsg(String msg){
        Snackbar.make(fab, msg, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }
}
