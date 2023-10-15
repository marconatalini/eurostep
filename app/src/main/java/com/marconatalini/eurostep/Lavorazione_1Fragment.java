package com.marconatalini.eurostep;


import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.JsonReader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.marconatalini.eurostep.entity.Lavorazione;
import com.marconatalini.eurostep.entity.LinkStep;
import com.marconatalini.eurostep.entity.Registrazione;
import com.marconatalini.eurostep.tool.Barcoder;

import static android.os.SystemClock.elapsedRealtime;

import static java.util.Collections.list;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class Lavorazione_1Fragment extends Fragment {

    private Lavorazione L;
    private long timerON;
    private TextView serverInfo, numeroOrdine, noteOrdine;
    private String ordine_lotto;
    private Button btnInizio, btnFine;
//    private String carrello;
//    private Boolean registrazioneInviata = false;
//    private Boolean ordine_incompleto = false;

    private IntentIntegrator integrator;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_lavorazione_1, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnInizio = view.findViewById(R.id.btn_inizio);
        btnFine = view.findViewById(R.id.btn_fine);
        serverInfo = view.findViewById(R.id.server_info);
        noteOrdine = view.findViewById(R.id.note_ordine);
        numeroOrdine = view.findViewById(R.id.numero_ordine);
        numeroOrdine.setText("Premi inizio.");

        integrator = new IntentIntegrator(getActivity());
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Scan a QR code");
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(true);

        if (getArguments() != null) {
            Lavorazione_1FragmentArgs args = Lavorazione_1FragmentArgs.fromBundle(getArguments());
            L = args.getLavorazione();

            View layout = view.getRootView();
            layout.setBackgroundColor(Color.parseColor(L.getColore()));

            if (L.getTipo().equals(Lavorazione.SOLO_FINE) || L.getTipo().equals(Lavorazione.SOLO_INIZIO)){
                btnFine.setVisibility(View.INVISIBLE);
            }
        }

        btnInizio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timerON = elapsedRealtime();
                switchButton();

                if (L.getTipo().equals(Lavorazione.TEMPORIZZATA_SENZA_NUMERO)) {
                    ordine_lotto = "999999_9";
                    Registrazione registrazione = new Registrazione(L.getCodice(), ordine_lotto, MainActivity.OPERATORE);
                    registrazione.sendDati(getContext(), serverInfo);
                } else {
                    integrator.forSupportFragment(Lavorazione_1Fragment.this).initiateScan();
                }
            }
        });

        TextWatcher numeroWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (s.length() == 8) {
                    String ordine_lotto = s.toString();
                    Barcoder bc = new Barcoder(ordine_lotto);
                    if (bc.checkBarcodeOrdine()) {
                        getClienteOrdine(bc.getNumeroOrdine().toString(), bc.getLottoOrdine().toString());
//                        get_note_avanzamento_ordine(bc.getNumeroOrdine().toString(), bc.getLottoOrdine().toString());
                    }
                }

            }
        };

        btnInizio.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!L.getTipo().equals(Lavorazione.TEMPORIZZATA_SENZA_NUMERO)) {
                    final EditText txtOrdine = new EditText(getActivity());
                    txtOrdine.addTextChangedListener(numeroWatcher);

                    AlertDialog numDialog = new AlertDialog.Builder(getActivity())
                            .setTitle("Inserimento manuale")
                            .setMessage("Ordine_lotto es.847003_A")
                            .setView(txtOrdine)
                            .setPositiveButton("Invia", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {

                                    timerON = elapsedRealtime();
                                    switchButton();
                                    sendDati(txtOrdine.getText().toString());
                                }
                            })

                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                }
                            })
                            .create();
                    numDialog.show();
                } else {
                    btnInizio.performClick();
                }
                return false;
            }
        });

        btnFine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long timerOFF = elapsedRealtime();
                long timeDelta = (timerOFF-timerON)/1000 + 1; //secondi lavoro
                boolean registrata = false;
                switchButton();
                numeroOrdine.setText("Premi inizio.");
                noteOrdine.setText("");
                Registrazione registrazione = new Registrazione(L.getCodice(), ordine_lotto, MainActivity.OPERATORE);
                registrazione.setSeconds(timeDelta);


                //Carrello richiesto
                if (L.getNeedCart().equals("1")){
                    getCarrello(registrazione, L.getCartCode());
                    registrata = true;
                } else {
                    //tipo 2: richiesta minuti
                    if (L.getTipo().equals(Lavorazione.TEMPORIZZATA_MANUALE)){
                        getMinuti(registrazione);
                        registrata = true;
                    }
                }

                //Has linkStep
                if (L.getLinkSteps().size()>0) {
                    try {
                        Registrazione clonedReg = (Registrazione)registrazione.clone();
                        askForLinkedLav(L, clonedReg);
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                }

                //Has checkComplete
                /*if (L.getCheckComplete().equals("1")) {
                    getPezziMancanti(registrazione);
                    registrata = true;
                }*/

                if (!registrata) {
                    registrazione.sendDati(getContext(), serverInfo);
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        if (btnFine.isEnabled()){
            btnFine.performClick();
        }
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result.getContents() != null) {
            sendDati(result.getContents());

        } else {
            // This is important, otherwise the result will not be passed to the fragment
            serverInfo.setText("Scansione codice ANNULLATA");
            switchButton();
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void sendDati(String barcode){

        Barcoder barcoder = new Barcoder(barcode);

        if (!barcoder.isValid()) {
            serverInfo.setText(String.format("Codice ERRATO (%s)! Riprova. Per inserimento manuale tieni premuto il tasto \"Inizio\"", barcode));
            switchButton();
        } else {
            ordine_lotto = barcoder.getOrdineLotto();
            numeroOrdine.setText(ordine_lotto);
            Registrazione registrazione = new Registrazione(L.getCodice(), ordine_lotto, MainActivity.OPERATORE);

            if (L.getTipo().equals(Lavorazione.SOLO_FINE)){
                registrazione.setSeconds(1); //fine lavoro
            }

            //Entrata in forno
            if (L.getTipo().equals(Lavorazione.SOLO_INIZIO) && L.getCodice().equals("V2")){
                getBilancelle(registrazione);
                return;
            }

            /*if (L.getTipo().equals(Lavorazione.SOLO_INIZIO) && L.getCodice().equals("A0")){
                getCarrello(registrazione);
                return;
            }*/

            //Carrello richiesto
            if (L.getNeedCart().equals("1") && registrazione.getSeconds()>0){
                getCarrello(registrazione, L.getCartCode());
            } else {
                registrazione.sendDati(getContext(), serverInfo);
            }

            get_note_avanzamento_ordine(barcoder.getNumeroOrdine().toString(), barcoder.getLottoOrdine());
        }
    }

    private void switchButton(){
        if (btnFine.getVisibility() == View.VISIBLE) {
            btnInizio.setEnabled(!btnInizio.isEnabled());
            btnFine.setEnabled(!btnFine.isEnabled());
        }
    }

    private void getMinuti(Registrazione registrazione){
        final EditText editText = new EditText(getActivity());
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        editText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle("Durata intervento in minuti (" + registrazione.getSeconds()/60 +") ?")
                .setView(editText)

                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        registrazione.sendDati(getContext(), serverInfo);
                    }
                })

                .setPositiveButton("Invia", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        long minuti = Long.parseLong(editText.getText().toString());
                        registrazione.setSeconds(minuti*60);
                        registrazione.sendDati(getContext(), serverInfo);
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void getBilancelle(Registrazione registrazione){
        final EditText editText = new EditText(getActivity());
        editText.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        editText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle("Quante bilancelle occupa?")
                .setView(editText)

                .setPositiveButton("Invia", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        float bilancelle = Float.parseFloat(editText.getText().toString());
                        registrazione.setBilancelle(bilancelle);
                        registrazione.sendDati(getContext(), serverInfo);
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void getPezziMancanti(Registrazione registrazione){
        final EditText editText = new EditText(getActivity());
        editText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle("Manca qualcosa?")
                .setView(editText)

                .setPositiveButton("Invia segnalazione", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String pezziMancanti = editText.getText().toString();
                        registrazione.setPezziMancanti(pezziMancanti); //inviati al server come note
                        registrazione.setCodice("R1");
                        registrazione.sendDati(getContext(), serverInfo);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void getCarrello(Registrazione registrazione, String cartCode){
        final EditText editText = new EditText(getActivity());
//        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        editText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(String.format("%s : numero carrello x%s?", registrazione.getOrdine_lotto(),cartCode))
                .setView(editText)

                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        registrazione.setCarrello(String.format("%s", cartCode));
                        registrazione.sendDati(getContext(), serverInfo);
                    }
                })

                .setPositiveButton("Invia", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
//                        String carrello = editText.getText().toString().toUpperCase().trim().replaceAll("\\s+","");
                        String carrello = editText.getText().toString();
                        registrazione.setCarrello(String.format("%s%s", carrello, cartCode));
                        registrazione.sendDati(getContext(), serverInfo);
                    }
                })
                ;

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void askForLinkedLav(Lavorazione lavorazione, Registrazione registrazione){

        final EditText editText = new EditText(getActivity());
        editText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        for (LinkStep linkstep: lavorazione.getLinkSteps()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setTitle(linkstep.getDescrizione())
                    .setMessage(linkstep.getDomanda())
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    });
            if (linkstep.getCodice().equals("R1")) {
                builder.setView(editText);
                builder.setPositiveButton("Invia", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String pezziMancanti = editText.getText().toString();
                        registrazione.setPezziMancanti(pezziMancanti); //inviati al server come note
                        registrazione.setCodice(linkstep.getCodice());
                        registrazione.sendDati(getContext(), serverInfo);
                    }
                });
            } else {
                builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        registrazione.setPezziMancanti(""); //inviati al server come note
                        registrazione.setCodice(linkstep.getCodice());
                        registrazione.sendDati(getContext(), serverInfo);
                    }
                });
            }

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    public void getClienteOrdine (String ordine, String lotto){
        String getURL = String.format("http://%s/api/cliente/%s/%s",
                MainActivity.WEBSERVER_IP, ordine, lotto);

        StringRequest sRequest = new StringRequest(Request.Method.GET, getURL,
                response -> {
                    Toast.makeText(getContext(), response.toString(),Toast.LENGTH_LONG).show();
                },
                error -> {
                    Toast.makeText(getContext(), error.toString(),Toast.LENGTH_LONG).show();
                    error.printStackTrace();
                });

        MySingleton.getInstance(getContext()).addToRequestque(sRequest);
    }

    public void get_note_avanzamento_ordine (String ordine, String lotto){
        String getURL = String.format("http://%s/api/note/%s/%s",
                MainActivity.WEBSERVER_IP, ordine, lotto);

        StringRequest sRequest = new StringRequest(Request.Method.GET, getURL,
                response -> {
                    noteOrdine.setText(json_to_note(response));
                },
                error -> {
                    Toast.makeText(getContext(), error.toString(),Toast.LENGTH_LONG).show();
                    error.printStackTrace();
                });

        MySingleton.getInstance(getContext()).addToRequestque(sRequest);
    }

//    @RequiresApi(api = Build.VERSION_CODES.O)
    private String json_to_note(String response) {
        StringBuilder note = new StringBuilder();

        try {
            JSONArray arr = new JSONArray(response);
            
            for (int i = 0; i < arr.length(); i++)
            {
                // [{"numeroOrdine":886564,"lottoOrdine":"0","note":"pos1 telaio","descrizione":"MARCO NATALINI",
                // "timestamp":{"date":"2022-12-08 11:04:04.000000","timezone_type":3,"timezone":"UTC"}}]
                String data = arr.getJSONObject(i).getJSONObject("timestamp").getString("date");
                String mese = data.substring(5,7);
                String giorno = data.substring(8,10);
                String ora = data.substring(11,13);
                String minuto = data.substring(14,16);

//                LocalDateTime date = LocalDateTime.parse(data, DateTimeFormatter.ofPattern("y-M-d H:m:s.n"));
//                note.append(date.format(DateTimeFormatter.ofPattern("dd/MM H:mm")));
                note.append(giorno).append("/").append(mese).append(" ore ").append(ora).append(":").append(minuto);
                note.append(" "); //operatore
                note.append(nikname(arr.getJSONObject(i).getString("descrizione"))); //operatore
                note.append("\n"); //operatore
                note.append(arr.getJSONObject(i).getString("note"));
                note.append("\n"); //operatore

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return note.toString();
    }

    private String nikname(String name)
    {
        String[] dati = name.split("\\s+");
        if (dati.length >= 2) {
            return dati[0] + " " + dati[1].charAt(0) + ".";
        }

        return dati[0];
    }

}
