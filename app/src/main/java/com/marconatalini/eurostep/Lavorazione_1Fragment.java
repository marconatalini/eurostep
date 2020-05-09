package com.marconatalini.eurostep;


import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.marconatalini.eurostep.entity.Lavorazione;
import com.marconatalini.eurostep.entity.Registrazione;
import com.marconatalini.eurostep.tool.Barcoder;

import static android.os.SystemClock.elapsedRealtime;

public class Lavorazione_1Fragment extends Fragment {

    private Lavorazione L;
    private long timerON;
    private TextView serverInfo, numeroOrdine;
    private String ordine_lotto;
    private Button btnInizio, btnFine;

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

        btnInizio.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!L.getTipo().equals(Lavorazione.TEMPORIZZATA_SENZA_NUMERO)) {
                    final EditText txtOrdine = new EditText(getActivity());
                    new AlertDialog.Builder(getActivity())
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
                            .show();
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
                switchButton();
                numeroOrdine.setText("Premi inizio.");
                Registrazione registrazione = new Registrazione(L.getCodice(), ordine_lotto, MainActivity.OPERATORE);
                registrazione.setSeconds(timeDelta);
                registrazione.sendDati(getContext(), serverInfo);
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
            /*String bc = result.getContents();

            Boolean check = new Barcoder(bc).isValid();

            if (!check) {
                serverInfo.setText(String.format("Codice ERRATO (%s): Prova inserimento manuale tenendo premuto il tasto \"Inizio\"", bc));
                switchButton();
            } else {
                ordine_lotto = bc.substring(0,8);
                numeroOrdine.setText(ordine_lotto);
                Registrazione registrazione = new Registrazione(L.getCodice(), ordine_lotto, MainActivity.OPERATORE);

                if (L.getTipo().equals(Lavorazione.SOLO_INIZIO) && L.getCodice().equals("V2")){
                    getBilancelle(registrazione);
                    return;
                }

                if (L.getTipo().equals(Lavorazione.SOLO_FINE)){
                    registrazione.setSeconds(1); //fine lavoro
                }
                registrazione.sendDati(getContext(), serverInfo);
            }*/
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            serverInfo.setText("Scansione codice ANNULLATA");
            switchButton();
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void sendDati(String barcode){

        Barcoder barcoder = new Barcoder(barcode);

        Log.d("meo", "sendDati: " + barcode);

        if (!barcoder.isValid()) {
            serverInfo.setText(String.format("Codice ERRATO (%s)! Riprova. Per inserimento manuale tieni premuto il tasto \"Inizio\"", barcode));
            switchButton();
        } else {
            ordine_lotto = barcoder.getOrdineLotto();
            numeroOrdine.setText(ordine_lotto);
            Registrazione registrazione = new Registrazione(L.getCodice(), ordine_lotto, MainActivity.OPERATORE);

            if (L.getTipo().equals(Lavorazione.SOLO_INIZIO) && L.getCodice().equals("V2")){
                getBilancelle(registrazione);
                return;
            }

            if (L.getTipo().equals(Lavorazione.SOLO_FINE)){
                registrazione.setSeconds(1); //fine lavoro
            }
            registrazione.sendDati(getContext(), serverInfo);
        }

    }

    private void switchButton(){
        if (btnFine.getVisibility() == View.VISIBLE) {
            btnInizio.setEnabled(!btnInizio.isEnabled());
            btnFine.setEnabled(!btnFine.isEnabled());
        }
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

}
