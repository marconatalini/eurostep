package com.marconatalini.eurostep;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.collection.ArrayMap;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.marconatalini.eurostep.entity.Registrazione;
import com.marconatalini.eurostep.localdb.dbCursor;

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

        String getURL = String.format("http://%s/online/%s/%s?&ordine_lotto=%s&seconds=%d&bilancelle=%s&carrello=%s&registrato_il=%s",
                MainActivity.WEBSERVER_IP, record.get("operatore"), record.get("cod_lav"), record.get("ordine_lotto"),
                record.get("seconds"), record.get("bilancelle"), record.get("carrello"), timestamp);

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

        int timeout = 5000;
        int retries = 0;
        RetryPolicy policy = new DefaultRetryPolicy(timeout, retries, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        sRequest.setRetryPolicy(policy);

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
