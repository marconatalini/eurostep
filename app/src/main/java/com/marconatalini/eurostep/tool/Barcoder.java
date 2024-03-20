package com.marconatalini.eurostep.tool;

import android.util.Log;
import android.widget.TextView;

import com.marconatalini.eurostep.MainActivity;
import com.marconatalini.eurostep.entity.Lavorazione;
import com.marconatalini.eurostep.entity.Registrazione;

import java.util.Objects;
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
            this.lottoOrdine = s.substring(-1);
            }
        }
    }

    public Boolean isValid() {
        return this.isValid;
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
        if (this.lottoOrdine.equals("")){
            return this.numeroOrdine + "_0";
        } else {
            return this.numeroOrdine + "_" + this.lottoOrdine;
        }
    }

    public Boolean checkBarcodeOrdine (){ //No underscore

        if (this.barcode.equals("")) return Boolean.FALSE;

        if (this.barcode.length() == 6) { //Ordine barre 200xxx
            this.numeroOrdine = Integer.valueOf(this.barcode);
            if (numeroOrdine > 200000 && numeroOrdine < 500000) {
                this.lottoOrdine = String.valueOf(0);
                return Boolean.TRUE;
            }
        }

        Pattern pOrdine = Pattern.compile("(^[985]\\d{5})[ _]([\\dA-Z])");
        Matcher matchNumero= pOrdine.matcher(this.barcode);

        if (!matchNumero.find()){
            return Boolean.FALSE;
        } else {
            this.numeroOrdine = Integer.valueOf(Objects.requireNonNull(matchNumero.group(1)));
            this.lottoOrdine = matchNumero.group(2);
            return Boolean.TRUE;
        }
    }

    public Boolean isOrdineTelai (){ //No underscore
        if (this.barcode == null) return Boolean.FALSE;

        Pattern pOrdine = Pattern.compile("^[89]\\d{5}[ _][\\dA-Z]$");
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
