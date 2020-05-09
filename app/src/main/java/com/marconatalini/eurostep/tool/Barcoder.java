package com.marconatalini.eurostep.tool;

import android.util.Log;
import android.widget.TextView;

import com.marconatalini.eurostep.MainActivity;
import com.marconatalini.eurostep.entity.Lavorazione;
import com.marconatalini.eurostep.entity.Registrazione;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Barcoder {

    private String barcode = "";
    private Integer numeroOrdine = 0;
    private String lottoOrdine = "";
    private Boolean isValid = false;

    public Barcoder() {
    }

    public Barcoder(String barcode) {
        this.barcode = barcode;
        this.isValid = checkBarcodeOrdine();
    }

    private void splitBarcode(String s){

        if (s.length() >= 6) {
            this.numeroOrdine = Integer.valueOf(s.substring(0,6));
            if (s.length() == 8) {
            this.lottoOrdine = s.substring(7,8);
            }
        }
    }

    public Boolean isValid() {
        return isValid;
    }

    public Integer getNumeroOrdine()
    {
        return this.numeroOrdine;
    }

    public String getLottoOrdine()
    {
        return this.lottoOrdine;
    }

    public String getOrdineLotto()
    {
        if (lottoOrdine == ""){
            return numeroOrdine + "_0";
        } else {
            return numeroOrdine + "_" + lottoOrdine;
        }
    }

    public Boolean checkBarcodeOrdine (){ //No underscore
        try {
            splitBarcode(barcode);
        } catch (Exception e) {
            return Boolean.FALSE;
        }

        if (barcode == null) return Boolean.FALSE;

        Pattern pOrdine = Pattern.compile("^[85]\\d{5}[ _][\\dA-Z]$");
        Matcher matchNumero= pOrdine.matcher(barcode.substring(0,8));

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
