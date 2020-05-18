package com.marconatalini.eurostep.entity;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.marconatalini.eurostep.MainActivity;
import com.marconatalini.eurostep.MySingleton;
import com.marconatalini.eurostep.localdb.dbCursor;

public class Registrazione {

    private Context context;
    private String codice;
    private String ordine_lotto;
    private String operatore;
    private long seconds = 0;
    private float bilancelle = 0.0f;
    private boolean erroreInvioDati = false;
    dbCursor cursor;
    OnRecordSavedListener onRecordSavedListener;


    public Registrazione(String codice, String ordine_lotto, String operatore) {
        this.codice = codice;
        this.ordine_lotto = ordine_lotto;
        this.operatore = operatore;

    }

    public void setSeconds(long seconds) {
        this.seconds = seconds;
    }

    public void setBilancelle(float bilancelle) {
        this.bilancelle = bilancelle;
    }

    public void sendDati (Context context, TextView ServerResponse){
        try {
            onRecordSavedListener = (Registrazione.OnRecordSavedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnRecordSavedListener");
        }

        String getURL = String.format("http://%s/online/%s/%s?&ordine_lotto=%s&seconds=%d&bilancelle=%s",
                        MainActivity.WEBSERVER_IP, operatore, codice, ordine_lotto, seconds, bilancelle);
        Log.d("meo", "sendDati: " + getURL);
        ServerResponse.setText(String.format("Invio dati %s in corso...", ordine_lotto));
        StringRequest sRequest = new StringRequest(Request.Method.GET, getURL,
                response -> {
                    Toast.makeText(context, response.toString(),Toast.LENGTH_SHORT).show();
                    ServerResponse.setText(String.format("%s inviato! OK", ordine_lotto));
                },
                error -> {
                    cursor = new dbCursor(context);
                    cursor.saveRecord(codice, ordine_lotto, operatore, seconds, bilancelle); //salvo nel DB locale
                    ServerResponse.setText(String.format("ERRORE: %s salvato in memoria. ", ordine_lotto));
                    Toast.makeText(context, error.toString(),Toast.LENGTH_LONG).show();
                    error.printStackTrace();
                    onRecordSavedListener.onRecordSaved();
                });

        int timeout = 5000;
        int retries = 0;
        RetryPolicy policy = new DefaultRetryPolicy(timeout, retries, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        sRequest.setRetryPolicy(policy);

        MySingleton.getInstance(context).addToRequestque(sRequest);
    }

    // Container Activity must implement this interface
    public interface OnRecordSavedListener {
        //n = numero da sommare ai record in memoria
        void onRecordSaved();
    }

}

