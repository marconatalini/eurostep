package com.marconatalini.eurostep;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.marconatalini.eurostep.entity.Lavorazione;
import com.marconatalini.eurostep.entity.Registrazione;
import com.marconatalini.eurostep.tool.Barcoder;

import java.util.ArrayList;

import static android.os.SystemClock.elapsedRealtime;


/**
 * A simple {@link Fragment} subclass.
 */
public class Lavorazione_2Fragment extends Fragment {

    private Lavorazione L;
    private long timerON = 0;
    private TextView serverInfo;
    private ListView lista_ordini;
    private String ordine_lotto;
    private Button btnInizio, btnFine;
    private ArrayList<String> arrayList = new ArrayList<String>();
    private ArrayAdapter<String> adapter;

    private IntentIntegrator integrator;

    public Lavorazione_2Fragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_lavorazione_2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnInizio = view.findViewById(R.id.btn_inizio2);
        btnFine = view.findViewById(R.id.btn_fine2);
        serverInfo = view.findViewById(R.id.server_info2);
        lista_ordini = view.findViewById(R.id.listaOrdiniLavorazione2);

        adapter = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, arrayList);
        lista_ordini.setAdapter(adapter);

        integrator = new IntentIntegrator(getActivity());
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Scan a QR code");
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(true);


        if (getArguments() != null) {
            Lavorazione_2FragmentArgs args = Lavorazione_2FragmentArgs.fromBundle(getArguments());
            L = args.getLavorazione();

            View layout = view.getRootView();
            layout.setBackgroundColor(Color.parseColor(L.getColore()));
        }

        btnInizio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                integrator.forSupportFragment(Lavorazione_2Fragment.this).initiateScan();
            }
        });

        btnInizio.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final EditText txtOrdine = new EditText(getActivity());
                new AlertDialog.Builder(getActivity())
                        .setTitle("Inserimento manuale")
                        .setMessage("Ordine_lotto es.847003_A")
                        .setView(txtOrdine)
                        .setPositiveButton("Invia", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                sendDati(txtOrdine.getText().toString());
                            }
                        })

                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        })
                        .show();
                return false;
            }
        });


        btnFine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendDatiFineLavoro();
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result.getContents() != null) {
            sendDati(result.getContents());

        } else {
            // This is important, otherwise the result will not be passed to the fragment
            serverInfo.setText("Scansione codice ANNULLATA");
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onDestroy() {
        if (btnFine.isEnabled()){
            btnFine.performClick();
        }
        super.onDestroy();
    }

    private void sendDati(String barcode) {

        Barcoder barcoder = new Barcoder(barcode);

        Log.d("meo", "sendDati: " + barcode);

        if (!barcoder.isValid()) {
            serverInfo.setText(String.format("Codice ERRATO (%s)! Riprova. Per inserimento manuale tieni premuto il tasto \"Inizio\"", barcode));
        } else {
            ordine_lotto = barcoder.getOrdineLotto();

            if (arrayList.size() == 0) {
                //primo ordine parte il timer
                timerON = elapsedRealtime();
            }

            if (!arrayList.contains(ordine_lotto + " in lavorazione...")) {
                arrayList.add(ordine_lotto + " in lavorazione...");
                adapter.notifyDataSetChanged();

                Registrazione registrazione = new Registrazione(L.getCodice(), ordine_lotto, MainActivity.OPERATORE);

                registrazione.sendDati(getContext(), serverInfo);
            } else {
                serverInfo.setText(String.format("Ordine %s già aggiunto alla lista", ordine_lotto));
            }
            btnFine.setEnabled(true);
        }
    }

    private void sendDatiFineLavoro() {
        long timerOFF = elapsedRealtime();
        long timeDelta = (timerOFF-timerON)/1000 + 1; //secondi lavoro

        for (int i = 0; i < arrayList.size(); ++i) {
            Log.d("meo", "Invio l'ordine :" + arrayList.get(i));
            ordine_lotto = arrayList.get(i).substring(0, 8);

            Registrazione registrazione = new Registrazione(L.getCodice(), ordine_lotto, MainActivity.OPERATORE);

            registrazione.setSeconds(timeDelta); //fine lavoro
            registrazione.setMultiordine(arrayList.size()); //totale ordini chiusi
            registrazione.sendDati(getContext(), serverInfo);
        }

        arrayList.clear();
        adapter.notifyDataSetChanged();
        btnFine.setEnabled(false);
    }
}
