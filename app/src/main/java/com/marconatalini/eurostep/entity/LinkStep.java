package com.marconatalini.eurostep.entity;

public class LinkStep {

    private final String codice;
    private final String descrizione;
    private final String domanda;

    public LinkStep(String codice, String descrizione, String domanda) {

        this.codice = codice;
        this.descrizione = descrizione;
        this.domanda = domanda;
    }

    public String getCodice() {
        return codice;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public String getDomanda() {
        return domanda;
    }
}
