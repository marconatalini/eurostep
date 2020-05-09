package com.marconatalini.eurostep.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class Lavorazione implements Parcelable {

    private String descrizione;
    private String codice;
    private String tipo;
    private String colore;


    public static final String TEMPORIZZATA_SENZA_NUMERO = "0";
    public static final String TEMPORIZZATA = "1";
    public static final String TEMPORIZZATA_MULTIPLA = "2";
    public static final String SOLO_INIZIO = "I";
    public static final String SOLO_FINE = "F";

    public Lavorazione(String descrizione, String codice, String tipo, String colore) {
        this.descrizione = descrizione;
        this.codice = codice;
        this.tipo = tipo;
        this.colore = colore;
    }

    protected Lavorazione(Parcel in) {
        descrizione = in.readString();
        codice = in.readString();
        tipo = in.readString();
        colore = in.readString();
    }

    public static final Creator<Lavorazione> CREATOR = new Creator<Lavorazione>() {
        @Override
        public Lavorazione createFromParcel(Parcel in) {
            return new Lavorazione(in);
        }

        @Override
        public Lavorazione[] newArray(int size) {
            return new Lavorazione[size];
        }
    };

    @Override
    public String toString() {
        return "Lavorazione{" +
                "descrizione='" + descrizione + '\'' +
                ", codice='" + codice + '\'' +
                ", tipo='" + tipo + '\'' +
                '}';
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getCodice() {
        return codice;
    }

    public void setCodice(String codice) {
        this.codice = codice;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getColore() {
        return colore;
    }

    public void setColore(String colore) {
        this.colore = colore;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(descrizione);
        dest.writeString(codice);
        dest.writeString(tipo);
        dest.writeString(colore);
    }
}
