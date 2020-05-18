package com.marconatalini.eurostep.deprecated;

import android.app.SearchManager;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.marconatalini.eurostep.MainActivity;
import com.marconatalini.eurostep.MySingleton;
import com.marconatalini.eurostep.R;
import com.marconatalini.eurostep.tool.DateFromJsonTimestampString;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

public class DettOrdineLaActivity extends AppCompatActivity {

    TextView ordine_lotto, cliente, riferimento, scadenza, finitura,
            cornici, complementari, tagli, montaggi, lav_cnc, lamiere;

//    String json_url_dett = "http://" + MainActivity.WEBSERVER_IP + "/dettaglio_ordineLA.php";
//    String json_url_step = "http://" + MainActivity.WEBSERVER_IP + "/avanzamento_ordineLA.php";

    ListView lista_step;
    ArrayList<String> record_step = new ArrayList<String>();
    ArrayAdapter<String> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dett_ordine_la);

        handleIntent(getIntent());

        ordine_lotto = (TextView) findViewById(R.id.ordine_lotto);
        cliente = (TextView) findViewById(R.id.cliente);
        riferimento = (TextView) findViewById(R.id.riferimento);
        scadenza = (TextView) findViewById(R.id.scadenza);
        finitura = (TextView) findViewById(R.id.finitura);
        cornici = (TextView) findViewById(R.id.n_cornici);
        complementari = (TextView) findViewById(R.id.n_complementari);
        tagli = (TextView) findViewById(R.id.n_tagli);
        montaggi = (TextView) findViewById(R.id.n_montaggi);
        lav_cnc = (TextView) findViewById(R.id.n_lavCNC);
        lamiere = (TextView) findViewById(R.id.n_lamiere);
        lista_step = (ListView) findViewById(R.id.listview_step_produzione);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, record_step);
        lista_step.setAdapter(adapter);


        /*Bundle extras = getIntent().getExtras();
        String numero = "888888";
        String lotto = "0";

        if (extras != null) {
            numero = extras.getString("numero");
            lotto = extras.getString("lotto");
        } else {
            Toast.makeText(this,"NESSUN ordine da cercare", Toast.LENGTH_SHORT).show();
            finish();
        }*/


    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            //use the query to search your data somehow

            String Numero = "";
            String Lotto = "";

            if (query != null) {
                Numero = query.substring(0, 6);
                Lotto = query.substring(7, 8);
            } else {
                Bundle extras = getIntent().getExtras();
                Numero = extras.getString("numero");
                Lotto = extras.getString("lotto");
            }

            String json_url_dett = "http://" + MainActivity.WEBSERVER_IP + "/dettaglio/" + Numero + "/" + Lotto;

            final String finalNumero = Numero;
            final String finalLotto = Lotto;
            StringRequest jsonObjectRequest = new StringRequest(Request.Method.GET, json_url_dett,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);

                                ordine_lotto.setText(String.format("%s_%s", jsonObject.getString("numero"), jsonObject.getString("lotto")));
                                cliente.setText(jsonObject.getString("xragsoc"));
                                riferimento.setText(String.format("Rif. %s", jsonObject.getString("riferimento")));
//                                SimpleDateFormat in_format = new SimpleDateFormat("yyyy-MM-dd", Locale.ITALIAN);
                                SimpleDateFormat out_format = new SimpleDateFormat("dd MMMM yyyy", Locale.ITALIAN);
                                Date DataScadenza = new DateFromJsonTimestampString(jsonObject.getString("dataScadenzaProduzione")).getDate();
                                scadenza.setText(out_format.format(DataScadenza));
                                finitura.setText(jsonObject.getString("finitura"));
                                cornici.setText(jsonObject.getString("nCornici"));
                                complementari.setText(jsonObject.getString("nComplementari"));
                                tagli.setText(jsonObject.getString("nTagli"));
                                montaggi.setText(jsonObject.getString("nMontaggi"));
                                lav_cnc.setText(jsonObject.getString("nLavorazioniCNC"));
                                lamiere.setText(jsonObject.getString("nLamiere"));

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(DettOrdineLaActivity.this, error.getMessage().toString(), Toast.LENGTH_LONG).show();
                    error.printStackTrace();
                }
            }) {

                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    //Creating parameters
                    Map<String, String> params = new Hashtable<String, String>();

                    //Adding parameters
                    params.put("numero", finalNumero);
                    params.put("lotto", finalLotto);

                    //returning parameters
                    return params;
                }
            };

            String json_url_step = "http://" + MainActivity.WEBSERVER_IP + "/avanzamento/" + Numero + "/" + Lotto;

            StringRequest jsonArrayRequest = new StringRequest(Request.Method.GET, json_url_step,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            int count = 0;
                            JSONArray jsonArray = null;
                            try {
                                jsonArray = new JSONArray(response);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            while (jsonArray != null && count < jsonArray.length()) {
                                try {
                                    JSONObject jsonObject = jsonArray.getJSONObject(count);
                                    SimpleDateFormat in_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ITALIAN);
                                    SimpleDateFormat out_format = new SimpleDateFormat("dd/MM HH:mm", Locale.ITALIAN);
                                    Date dataStep = new DateFromJsonTimestampString(jsonObject.getString("timestamp")).getDate();

                                    boolean bool_fine = jsonObject.getBoolean("inizioFine");
                                    String stato = "";
                                    if (bool_fine == true) {
                                        stato = "FINE";
                                    } else {
                                        stato = "inizio";
                                    }

                                    record_step.add(String.format("%s %s %s %s",out_format.format(dataStep), stato, jsonObject.getString("descrizione"), jsonObject.getString("operatore")));
                                    count++;

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            adapter.notifyDataSetChanged();
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(DettOrdineLaActivity.this, error.getMessage().toString(), Toast.LENGTH_LONG).show();
                    error.printStackTrace();
                }
            }) {
/*
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    //Creating parameters
                    Map<String, String> params = new Hashtable<String, String>();

                    //Adding parameters
                    params.put("numero", finalNumero);
                    params.put("lotto", finalLotto);

                    //returning parameters
                    return params;
                }
*/
            };

            MySingleton.getInstance(DettOrdineLaActivity.this).addToRequestque(jsonObjectRequest);
            MySingleton.getInstance(DettOrdineLaActivity.this).addToRequestque(jsonArrayRequest);
        }
    }


}

