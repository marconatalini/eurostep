package com.marconatalini.eurostep;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ListeActivity extends Activity implements View.OnClickListener{

    Button dati_inviati, cianfrinatura, verniciatura, cnc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liste);

        dati_inviati = (Button) findViewById(R.id.btn_dati_inviati);
        cianfrinatura = (Button) findViewById(R.id.btn_ordini_cianfrinato);
        verniciatura = (Button) findViewById(R.id.btn_verniciatura);
        cnc = (Button) findViewById(R.id.btn_ordini_cnc);

        dati_inviati.setOnClickListener(this);
        cianfrinatura.setOnClickListener(this);
        verniciatura.setOnClickListener(this);
        cnc.setOnClickListener(this);

        /*Button webBtn = findViewById(R.id.btn_web);
        webBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListeActivity.this, WebActivity.class);
                String getUrl = String.format("http://%s/api/registrazioni/%s", MainActivity.WEBSERVER_IP, MainActivity.OPERATORE);
                intent.putExtra("URL", getUrl);
                startActivity(intent);
            }
        });*/
    }



    @Override
    public void onClick(View v) {
//        Intent lista = new Intent(ListeActivity.this, Lista_ordini_la.class);
//        lista.putExtra("json_url","http://"+ MainActivity.WEBSERVER_IP +"/lista_ordiniLA.php");

        Intent intent = new Intent(ListeActivity.this, WebActivity.class);
        String getUrl = "";

        if (v == dati_inviati) {
            getUrl = String.format("http://%s/api/registrazioni/%s", MainActivity.WEBSERVER_IP, MainActivity.OPERATORE);
//            lista.putExtra("json_url","http://"+ MainActivity.WEBSERVER_IP +"/registrazioni/"+MainActivity.OPERATORE);
//            lista.putExtra("titolo_lista", "Registro fine lavoro");
//            lista.putExtra("phpquery", "DatiInviati");
        }
        if (v == cianfrinatura) {
            getUrl = String.format("http://%s/api/listaordini/cianfrinatura", MainActivity.WEBSERVER_IP);
//            lista.putExtra("json_url","http://"+ MainActivity.WEBSERVER_IP +"/ordiniCianfrinatura");
//            lista.putExtra("titolo_lista", "Ordini da cianfrinare");
//            lista.putExtra("phpquery", "ArrivoInCianfrinatura");
        }
        if (v == verniciatura) {
            getUrl = String.format("http://%s/api/listaordini/verniciatura", MainActivity.WEBSERVER_IP);
//            lista.putExtra("json_url","http://"+ MainActivity.WEBSERVER_IP +"/ordiniVerniciatura");
//            lista.putExtra("titolo_lista", "Ordini già levigati");
//            lista.putExtra("phpquery", "ArrivoInVerniciatura");
        }
        if (v == cnc) {
            getUrl = String.format("http://%s/api/listaordini/cnc", MainActivity.WEBSERVER_IP);
//            lista.putExtra("json_url","http://"+ MainActivity.WEBSERVER_IP +"/ordiniCNC");
//            lista.putExtra("titolo_lista", "Ordini con lavorazioni");
//            lista.putExtra("phpquery", "OrdiniCNC");
        }

        intent.putExtra("URL", getUrl);
        startActivity(intent);
    }
}
