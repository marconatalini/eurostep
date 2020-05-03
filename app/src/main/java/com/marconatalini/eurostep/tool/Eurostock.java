package com.marconatalini.eurostep.tool;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.marconatalini.eurostep.MainActivity;

public class Eurostock {

    private Integer ordine;
    private String lotto;
    private String note;
    private String soluzione;
    private String operatore;

    public Eurostock (Integer ordine, String lotto, String operatore, String note, String soluzione)
    {
        this.ordine = ordine;
        this.lotto = lotto;
        this.note = note;
        this.soluzione = soluzione;
        this.operatore = operatore;
    }

    public Intent IntentOpenWebPage() {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.encodedPath(MainActivity.FOTO_UPLOAD_URI);
        uriBuilder.appendQueryParameter("ordine", String.valueOf(ordine));
        uriBuilder.appendQueryParameter("lotto", lotto);
        uriBuilder.appendQueryParameter("note", note);
        uriBuilder.appendQueryParameter("soluzione", soluzione);
        uriBuilder.appendQueryParameter("operatore", operatore);
        Uri webpage = Uri.parse(uriBuilder.build().toString());
        Log.d("meo", uriBuilder.build().toString());
        return new Intent(Intent.ACTION_VIEW, webpage);
    }
}
