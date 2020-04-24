package com.marconatalini.eurostep.tool;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Barcoder {

    private final String barcode;
    private Integer numeroOrdine;
    private String lottoOrdine;

    public Barcoder(String barcode) {
        this.barcode = barcode;
        this.numeroOrdine = 0;
        this.lottoOrdine = "";
        splitBarcode(barcode);
    }

    private void splitBarcode(String s){
        if (s.length() == 6) {
            this.numeroOrdine = Integer.valueOf(s);
        }

        if (s.length() >= 8) {
            this.numeroOrdine = Integer.valueOf(s.substring(0,6));
            this.lottoOrdine = s.substring(7,8);
        }
    }

    public Integer getNumeroOrdine()
    {
        return this.numeroOrdine;
    }

    public String getLottoOrdine()
    {
        return this.lottoOrdine;
    }

    public Boolean checkBarcodeOrdine (){ //No underscore
        if (this.barcode == null) return Boolean.FALSE;

        Pattern pOrdine = Pattern.compile("^[85]\\d{5}[ _][\\dA-Z]$");
        Matcher matchNumero= pOrdine.matcher(this.barcode.substring(0,8));

        if (!matchNumero.find()){
            return Boolean.FALSE;
        } else {
            return Boolean.TRUE;
        }
    }

    public Boolean isOrdineTelai (){ //No underscore
        if (this.barcode == null) return Boolean.FALSE;

        Pattern pOrdine = Pattern.compile("^8\\d{5}[ _][\\dA-Z]$");
        Matcher matchNumero= pOrdine.matcher(this.barcode);

        if (!matchNumero.find()){
            return Boolean.FALSE;
        } else {
            return Boolean.TRUE;
        }
    }

    public Boolean isOrdinePersiane (){ //No underscore
        if (this.barcode == null) return Boolean.FALSE;

        Pattern pOrdine = Pattern.compile("^5\\d{5}[ _][\\dA-Z]$");
        Matcher matchNumero= pOrdine.matcher(this.barcode);

        if (!matchNumero.find()){
            return Boolean.FALSE;
        } else {
            return Boolean.TRUE;
        }
    }

    public Boolean isOrdineBarre (){ //No underscore
        if (this.barcode == null) return Boolean.FALSE;

        Pattern pOrdine = Pattern.compile("^\\d{6}$");
        Matcher matchNumero= pOrdine.matcher(this.barcode);

        if (!matchNumero.find()){
            return Boolean.FALSE;
        } else {
            return Boolean.TRUE;
        }
    }

}
