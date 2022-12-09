package com.marconatalini.eurostep.entity;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.marconatalini.eurostep.MainActivity;
import com.marconatalini.eurostep.MySingleton;
import com.marconatalini.eurostep.localdb.dbCursor;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class Registrazione implements Cloneable{

    private Context context;
    private String codice;
    private String ordine_lotto;
    private String operatore;
    private String carrello = "";
    private String pezziMancanti = "";
    private String note = "";
    private long seconds = 0;
    private float bilancelle = 0.0f;
    private boolean erroreInvioDati = false;
    private int multiordine = 1;
    dbCursor cursor;
    OnRecordSavedListener onRecordSavedListener;


    public Registrazione(String codice, String ordine_lotto, String operatore) {
        this.codice = codice;
        this.ordine_lotto = ordine_lotto;
        this.operatore = operatore;

    }

    @NonNull
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public int getMultiordine() {
        return multiordine;
    }

    public void setMultiordine(int multiordine) {
        this.multiordine = multiordine;
    }

    public void setCodice(String codice) {
        this.codice = codice;
    }

    public void setCarrello(String carrello) {
        this.carrello = carrello;
    }

    public void setPezziMancanti(String pezziMancanti) {
        this.pezziMancanti = pezziMancanti; //note
    }

    public long getSeconds() {
        return seconds;
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

        try {
            note = URLEncoder.encode(pezziMancanti, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            note = "Errore descrizione";
            e.printStackTrace();
        }

        String getURL = String.format("http://%s/online/%s/%s?&ordine_lotto=%s&seconds=%d&bilancelle=%s&carrello=%s&multiordine=%s&note=%s",
                        MainActivity.WEBSERVER_IP, operatore, codice, ordine_lotto, seconds, bilancelle, carrello, multiordine, note);
        Log.d("meo", "sendDati: " + getURL);

        ServerResponse.setText(String.format("Invio dati %s in corso...", ordine_lotto));
        StringRequest sRequest = new StringRequest(Request.Method.GET, getURL,
                response -> {
                    Toast.makeText(context, response.toString(),Toast.LENGTH_SHORT).show();
                    ServerResponse.setText(String.format("%s inviato! OK", ordine_lotto));
                },
                error -> {
                    cursor = new dbCursor(context);
                    cursor.saveRecord(codice, ordine_lotto, operatore, seconds, bilancelle, carrello,1); //salvo nel DB locale
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

    public String getOrdine_lotto() {
        return ordine_lotto;
    }
}

