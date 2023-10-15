package com.marconatalini.eurostep;

import static android.os.SystemClock.elapsedRealtime;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.marconatalini.eurostep.entity.Lavorazione;
import com.marconatalini.eurostep.entity.Registrazione;
import com.marconatalini.eurostep.tool.Barcoder;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Lavorazione_3Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Lavorazione_3Fragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private Lavorazione L;
    private TextView serverInfo, numeroOrdine, clienteOrdine, editTextTextMultiLine;
    private String ordine_lotto;
    private Button btnInizio, btnFine;

    private IntentIntegrator integrator;

    public Lavorazione_3Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment lavorazione3Fragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Lavorazione_3Fragment newInstance(String param1, String param2) {
        Lavorazione_3Fragment fragment = new Lavorazione_3Fragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_lavorazione_3, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnInizio = view.findViewById(R.id.btn_inizio3);
        btnFine = view.findViewById(R.id.btn_fine);
        serverInfo = view.findViewById(R.id.server_info);
        numeroOrdine = view.findViewById(R.id.numero_ordine);
        clienteOrdine = view.findViewById(R.id.cliente3);
        editTextTextMultiLine = view.findViewById(R.id.editTextTextMultiLine);

        integrator = new IntentIntegrator(getActivity());
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Scan a QR code");
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(true);

        if (getArguments() != null) {
            Lavorazione_3FragmentArgs args = Lavorazione_3FragmentArgs.fromBundle(getArguments());
            L = args.getLavorazione();

            View layout = view.getRootView();
            layout.setBackgroundColor(Color.parseColor(L.getColore()));
        }

        /*TextWatcher numeroWatcher = new TextWatcher() {
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
                    }
                }

            }
        };*/

        TextWatcher noteWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 5) {
                    btnFine.setEnabled(true);
                } else {
                    btnFine.setEnabled(false);
                }
            }
        };
        editTextTextMultiLine.addTextChangedListener(noteWatcher);

        btnInizio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                integrator.forSupportFragment(Lavorazione_3Fragment.this).initiateScan();
            }
        });

        btnInizio.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final EditText txtOrdine = new EditText(getActivity());
//                txtOrdine.addTextChangedListener(numeroWatcher);

                AlertDialog numDialog = new AlertDialog.Builder(getActivity())
                        .setTitle("Inserimento manuale")
                        .setMessage("Ordine_lotto es.847003_A")
                        .setView(txtOrdine)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                setNumeroOrdine(txtOrdine.getText().toString());
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                serverInfo.setText("");
                            }
                        })
                        .create();
                numDialog.show();

                return false;
            }
        });

        btnFine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Registrazione registrazione = new Registrazione(L.getCodice(), ordine_lotto, MainActivity.OPERATORE);
                registrazione.setSeconds(1);
                registrazione.setPezziMancanti(editTextTextMultiLine.getText().toString());
                registrazione.sendDati(getContext(), serverInfo);
                resetView();
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result.getContents() != null) {
            setNumeroOrdine(result.getContents());
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            serverInfo.setText("Scansione codice ANNULLATA");
//            switchButton();
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void resetView() {
        btnFine.setEnabled(false);
//        numeroOrdine.setText("");
//        clienteOrdine.setText("");
        editTextTextMultiLine.setText("");
    }

    private void setNumeroOrdine (String t) {
        Barcoder barcoder = new Barcoder(t);
        if (barcoder.isValid()) {
            ordine_lotto = barcoder.getOrdineLotto();
            numeroOrdine.setText(ordine_lotto);
            getClienteOrdine(barcoder.getNumeroOrdine().toString(), barcoder.getLottoOrdine().toString());
        } else {
            serverInfo.setText("Codice non valido!");
        }
    }

    /*private void sendDati(String barcode){

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

        }
    }*/

    public void getClienteOrdine (String ordine, String lotto){
        String getURL = String.format("http://%s/api/cliente/%s/%s",
                MainActivity.WEBSERVER_IP, ordine, lotto);

        StringRequest sRequest = new StringRequest(Request.Method.GET, getURL,
                response -> {
//                    Toast.makeText(getContext(), response.toString(),Toast.LENGTH_LONG).show();
                    clienteOrdine.setText(response.toString());
                },
                error -> {
                    Toast.makeText(getContext(), error.toString(),Toast.LENGTH_LONG).show();
                    error.printStackTrace();
                });

        MySingleton.getInstance(getContext()).addToRequestque(sRequest);
    }
}