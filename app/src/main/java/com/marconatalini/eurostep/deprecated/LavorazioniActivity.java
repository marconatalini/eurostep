package com.marconatalini.eurostep.deprecated;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AlertDialog;
import androidx.collection.ArrayMap;

import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.marconatalini.eurostep.MainActivity;
import com.marconatalini.eurostep.MySingleton;
import com.marconatalini.eurostep.R;
import com.marconatalini.eurostep.localdb.dbCursor;
import com.marconatalini.eurostep.tool.Barcoder;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import static android.os.SystemClock.elapsedRealtime;
import static com.marconatalini.eurostep.MainActivity.OPERATORE;

public class LavorazioniActivity extends Activity {

    dbCursor cursor;
    long nMemReg;
    Button btn_lav, btn_finelav;
//    SocketTask socketTask;
    TextView ServerResponse;
    String ordine_lotto, cod_lav, tipo_lav;
    ListView lista_ordini;
    ArrayList<String> arrayList = new ArrayList<String>();
    ArrayAdapter<String> adapter;

    ProgressBar progressBar;
    private int mProgressStatus = 0;
    private Handler mHandler = new Handler();
    private Handler timeHandler = new Handler();
    Boolean isStart = false;

    private long timerON = 0, timerOFF = 0, timeDelta = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lavorazioni);
//        socketTask = new SocketTask(LavorazioniActivity.this);
        final IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Scan a barcode");
//        integrator.setCameraId(0);  // Use a specific camera front of the device
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(true);

        ServerResponse = (TextView) findViewById(R.id.Server_response);
        cursor = new dbCursor(this.getBaseContext());
        nMemReg = cursor.getRegistrazioniLocali();
        ServerResponse.setText(String.format("Hai %d registrazioni in memoria.", nMemReg));

        progressBar = (ProgressBar) findViewById(R.id.progressBar);


        final Spinner spinner_lav = (Spinner) findViewById(R.id.lista_lavorazioni);
        ArrayAdapter<CharSequence> lav_adapter = ArrayAdapter.createFromResource(this,
                R.array.Lavorazioni, android.R.layout.simple_spinner_item);
        lav_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_lav.setAdapter(lav_adapter);
        spinner_lav.performClick();

        spinner_lav.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (getTipoLav().equals("0")){
                    String str_item = parent.getItemAtPosition(position).toString();
                    btn_lav.setText("Inizio " + str_item.substring(0, str_item.length()-6));
                }

                if (getTipoLav().equals("M")){
                    String str_item = parent.getItemAtPosition(position).toString();
                    btn_lav.setText(str_item.substring(0, str_item.length()-6));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        lista_ordini = (ListView) findViewById(R.id.lista_ordini);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayList);
        lista_ordini.setAdapter(adapter);

        btn_lav = (Button) findViewById(R.id.btn_lavorazione);
        btn_finelav = (Button) findViewById(R.id.btn_fine_lavoro);

        btn_lav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner_lav.setEnabled(false);
                //Log.d("meo",getCodLav() + "|" + getTipoLav());
                switch (getTipoLav()){
                    case "0" : //inizio lavoro senza numero
                        timerON = elapsedRealtime();
                        switchButton();
                        ordine_lotto = "999999_9";
//                        socketTask.sendDati(getCodLav(), ordine_lotto , OPERATORE, 0);
                        sendDati(getCodLav(), ordine_lotto , OPERATORE, 0, 0);
                        arrayList.add(ordine_lotto + " in lavorazione...");
                        adapter.notifyDataSetChanged();
                        break;
                    case "1" : //inizio lavoro temporizzato
                        timerON = elapsedRealtime();
                        switchButton();
                        integrator.initiateScan();
                        break;
                    case "2" : //aggiunta primo ordine
                        btn_lav.setText("Aggiungi Ordine");
                        integrator.initiateScan();
                        btn_finelav.setEnabled(true);
                        break;

                    case "I" : // inizio lavoro non temporizzato
                        timeDelta = 0;
                        integrator.initiateScan();
                        break;

                    case "F" : // fine lavoro non temporizzato
                        timeDelta = 1;
                        integrator.initiateScan();
                        break;

                    case "M" : // invio dati dalla memoria
                        if (nMemReg > 0){
                            ArrayMap record = cursor.getFirstRecord();
                            sendDatiBatch(record);
                        } else {
                            Toast.makeText(LavorazioniActivity.this, "Memoria VUOTA",Toast.LENGTH_LONG).show();
                            InitGUI();
                        }
                        break;
                }
            }
        });

        btn_lav.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!getTipoLav().equals("0")){
                    final EditText txtOrdine = new EditText(LavorazioniActivity.this);
                    new AlertDialog.Builder(LavorazioniActivity.this)
                            .setTitle("Inserimento manuale")
                            .setMessage("Ordine_lotto es.847003_A")
                            .setView(txtOrdine)
                            .setPositiveButton("Invia", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    ordine_lotto = txtOrdine.getText().toString();
//                                    Boolean check = socketTask.checkBarcodeOrdine(ordine_lotto);
                                    Boolean check = new Barcoder(ordine_lotto).checkBarcodeOrdine();
                                    if (!check) {
                                        ServerResponse.setText("Scansione codice ANNULLATA");
                                        InitGUI();
                                    } else {
                                        switch (getTipoLav()){
                                            case "0" : //inizio lavoro senza numero
                                                btn_lav.callOnClick();
                                                break;
                                            case "1" : //inizio lavoro temporizzato
                                                timerON = elapsedRealtime();
                                                switchButton();
                                                InviaDatiAlServer();
                                                break;
                                            case "2" : //aggiunta primo ordine
                                                btn_lav.setText("Aggiungi Ordine");
                                                InviaDatiAlServer();
                                                btn_finelav.setEnabled(true);
                                                break;

                                            case "I" : // inizio lavoro non temporizzato
                                                timeDelta = 0;
                                                InviaDatiAlServer();
                                                break;

                                            case "F" : // fine lavoro non temporizzato
                                                timeDelta = 1;
                                                InviaDatiAlServer();
                                                break;

                                        }
                                    }
                                }
                            })

                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                }
                            })
                            .show();
                }

                return false;
            }
        });

        btn_finelav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (getTipoLav()){
                    case "0":
                    case "1": //fine lavoro temporizzato
                        timerOFF = elapsedRealtime();
                        timeDelta = (timerOFF-timerON)/1000 + 1; //secondi lavoro
                        switchButton();
                        fineLavori(timeDelta);
                        break;

                    case "2" : //fine ordini aperti
                        fineLavori(1);
                        break;
                }
                InitGUI();
            }
        });

        //Controllo che alle 18.05 sia tutto chiuso
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE,1);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hour = 18;
        int minute = 5;
        int seconds = 0;
        cal.set(year,month,day,hour,minute,seconds);
        Log.d("meo","CAL gettime: " + cal.getTime().toString());

        Timer opoff = new Timer("opoff");
        opoff.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.d("meo","ESEGUO!");
                if (btn_finelav.isEnabled()){
                    finish();
                }
            }
        },cal.getTime());


    }


    private void InitGUI() {
        Spinner spinner_lav = (Spinner) findViewById(R.id.lista_lavorazioni);
        spinner_lav.setEnabled(true);
        btn_finelav.setEnabled(false);
        btn_lav.setEnabled(true);
        btn_lav.setText("Scegli Ordine");
        progressBar.setIndeterminate(false);
        isStart = false;
        mProgressStatus = 0;
        timeDelta = 0;
    }

    @Override
    public void onBackPressed(){
        if (arrayList.size() > 0){
            Toast.makeText(LavorazioniActivity.this,"Premi FINE LAVORO prima di uscire",Toast.LENGTH_SHORT).show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result.getContents() != null) {
            String bc = result.getContents();

            Boolean check = new Barcoder(bc).checkBarcodeOrdine();

            if (bc == null || !check) {
                ServerResponse.setText("Scansione codice ANNULLATA");
                InitGUI();
            } else {
                ordine_lotto = bc.substring(0,8);
                InviaDatiAlServer();
            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void getBilancelle(){

        final EditText editText = new EditText(LavorazioniActivity.this);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        editText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        AlertDialog.Builder builder = new AlertDialog.Builder(LavorazioniActivity.this)
                .setTitle("Quante bilancelle occupa?")
                .setView(editText)

                .setPositiveButton("Invia", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
//                        socketTask.sendDati(getCodLav(), ordine_lotto, OPERATORE, timeDelta, "bilancelle="+editText.getText());
                        float bilancelle = Float.parseFloat(editText.getText().toString());
                        sendDati(getCodLav(), ordine_lotto, OPERATORE, timeDelta, bilancelle);
                        InitGUI();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void InviaDatiAlServer() {
        //Entrata in forno
        if (!ordine_lotto.equals("") && getTipoLav().equals("I") && getCodLav().equals("V2")){
            getBilancelle();
            return;
        }

        if (!ordine_lotto.equals("") && !arrayList.contains(ordine_lotto + " in lavorazione...")) {
//            socketTask.sendDati(getCodLav(), ordine_lotto, OPERATORE, timeDelta);
            sendDati(getCodLav(), ordine_lotto, OPERATORE, timeDelta, 0);
            if (!getTipoLav().equals("I") && !getTipoLav().equals("F") ){
                arrayList.add(ordine_lotto + " in lavorazione...");
                adapter.notifyDataSetChanged();
                isStart = true;
            } else {
                InitGUI();
            }
        }
    }

    private String getCodLav(){
        Spinner spinner_lav = (Spinner) findViewById(R.id.lista_lavorazioni);
        String selezione = spinner_lav.getSelectedItem().toString();
        cod_lav = selezione.substring(selezione.length()-5,selezione.length()-3);
        return cod_lav;
    };

    private String getTipoLav(){
        Spinner spinner_lav = (Spinner) findViewById(R.id.lista_lavorazioni);
        String selezione = spinner_lav.getSelectedItem().toString();
        tipo_lav = selezione.substring(selezione.length()-2,selezione.length()-1);
        return tipo_lav;
    };

    private void switchButton(){
        btn_finelav.setEnabled(!btn_finelav.isEnabled());
        btn_lav.setEnabled(!btn_lav.isEnabled());
    }

    private void fineLavori(long timeDelta){
        for (int i = 0; i < arrayList.size(); ++i){
            Log.d("meo","Invio l'ordine :" + arrayList.get(i));
            ordine_lotto = arrayList.get(i).substring(0,8);
//            socketTask.sendDati(getCodLav(), ordine_lotto, OPERATORE, timeDelta);
            sendDati(getCodLav(), ordine_lotto, OPERATORE, timeDelta, 0);
        }
        arrayList.clear();
        adapter.notifyDataSetChanged();
    }

    private void sendDati (String cod_lav, String ordine_Lotto, String operatore, long seconds, float bilancelle){
        String getURL =
                String.format("http://%s/online/%s/%s?&ordine_lotto=%s&seconds=%d&bilancelle=%s",
                        MainActivity.WEBSERVER_IP, operatore, cod_lav, ordine_Lotto, seconds, bilancelle);
        Log.d("meo", getURL);
        ServerResponse.setText(String.format("Invio dati %s ... attendi", ordine_Lotto));

        StringRequest sRequest = new StringRequest(Request.Method.GET, getURL,
                response -> {
                    Toast.makeText(LavorazioniActivity.this, response.toString(),Toast.LENGTH_LONG).show();
                    ServerResponse.setText(String.format("%s inviato! OK", ordine_Lotto));
                },
                error -> {
                    cursor.saveRecord(cod_lav, ordine_Lotto, operatore, seconds, bilancelle); //salvo nel DB locale
                    ServerResponse.setText(String.format("ERRORE: %s salvato in memoria. ", ordine_Lotto));
                    Toast.makeText(LavorazioniActivity.this, error.toString(),Toast.LENGTH_LONG).show();
                    error.printStackTrace();
                });

        MySingleton.getInstance(LavorazioniActivity.this).addToRequestque(sRequest);
    }

    private void sendDatiBatch (ArrayMap record) {
        if (record.isEmpty()) {
            return;
        }

        String timestamp = null;
        long id = (long) record.get("id");

        try {
            timestamp = URLEncoder.encode((String) record.get("timestamp"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String getURL = String.format("http://%s/online/%s/%s?&ordine_lotto=%s&seconds=%d&bilancelle=%s&registrato_il=%s",
                MainActivity.WEBSERVER_IP, record.get("operatore"), record.get("cod_lav"), record.get("ordine_lotto"),
                record.get("seconds"), record.get("bilancelle"), timestamp);
        ServerResponse.setText(String.format("Invio dati memoria ... attendi"));
        Log.d("meo", getURL);
        StringRequest sRequest = new StringRequest(Request.Method.GET, getURL,
                response -> {
                    cursor.deleteRecord(id);
                    nMemReg -= 1;
                    Toast.makeText(LavorazioniActivity.this, response.toString(),Toast.LENGTH_LONG).show();
                    if (nMemReg > 0) {
                        ServerResponse.setText(String.format("%d registrazioni in memoria", nMemReg));
                    }

                    if (nMemReg == 0) {
                        ServerResponse.setText(String.format("Memoria svuotata!"));
                        InitGUI();
                    }
                },
                error -> {
                    Toast.makeText(LavorazioniActivity.this, error.toString(),Toast.LENGTH_LONG).show();
                    error.printStackTrace();
                    ServerResponse.setText(String.format("ERRORE: Riprova più tardi. "));
                });

        MySingleton.getInstance(LavorazioniActivity.this).addToRequestque(sRequest);
    }



    @Override
    protected void onDestroy() {
        Log.d("meo","Activity DISTRUTTA");
        cursor.close();
        switch (getTipoLav()){
            case "0":
            case "1": //fine lavoro temporizzato
                timerOFF = elapsedRealtime();
                timeDelta = (timerOFF-timerON)/1000 + 1; //secondi lavoro
                switchButton();
                fineLavori(timeDelta);
                break;

            case "2" : //fine ordini aperti
                fineLavori(1);
                break;
        }
        super.onDestroy();
    }
}
