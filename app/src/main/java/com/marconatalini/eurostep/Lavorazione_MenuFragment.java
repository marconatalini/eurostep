package com.marconatalini.eurostep;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.flexbox.FlexboxLayout;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.marconatalini.eurostep.entity.Lavorazione;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Objects;

public class Lavorazione_MenuFragment extends Fragment {

    private FlexboxLayout flexboxLayout;
    private SharedPreferences sharedPref;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_lavorazione_menu, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        flexboxLayout = (FlexboxLayout) view.findViewById(R.id.menu_flexbox_layout);

        XmlResourceParser parser = getResources().getXml(R.xml.items_lavorazioni);
        // Process the XML data
        try{
            processXMLData(parser);
        }catch(IOException | XmlPullParserException e){
            e.printStackTrace();
        }
    }

    // Custom method to process XML data
    private void processXMLData(XmlResourceParser parser)throws IOException,XmlPullParserException{
        int eventType = -1;

        String lastUsedLavCode = sharedPref.getString(getString(R.string.last_select_lavorazione_code), "");

        // Loop through the XML data
        while(eventType!=parser.END_DOCUMENT){
            if(eventType == XmlResourceParser.START_TAG){
                String lavorazioneValue = parser.getName();
                if (lavorazioneValue.equals("lavorazione")){
                    String descrizione = parser.getAttributeValue(null,"descrizione");
                    String codice = parser.getAttributeValue(null,"codice");
                    String tipo = parser.getAttributeValue(null,"tipo");
                    String colore = parser.getAttributeValue(null,"colore");
                    Lavorazione Lav = new Lavorazione(descrizione, codice, tipo, colore );
                    addButton(Lav, lastUsedLavCode);
                }
            }
            /*
                The method next() advances the parser to the next event. The int value returned from
                next determines the current parser state and is identical to the value returned
                from following calls to getEventType ().

                The following event types are seen by next()

                    START_TAG
                        An XML start tag was read.
                    TEXT
                        Text content was read; the text content can be retrieved using the getText()
                        method. (when in validating mode next() will not report ignorable
                        whitespace, use nextToken() instead)
                    END_TAG
                        An end tag was read
                    END_DOCUMENT
                        No more events are available
            */
            eventType = parser.next();
        }
    }

    // Custom method to format XML data to display
    private void addButton(Lavorazione Lav, String lastUsedLavCode){
        SharedPreferences.Editor editor = sharedPref.edit();

        Button btn = new Button(this.getContext());
        btn.setText(Lav.getDescrizione());

        FlexboxLayout.LayoutParams lp = new FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,200
        );
        lp.setFlexGrow(2);
        lp.setOrder(2);
        lp.setMargins(7,15,7,0);

        if (Lav.getCodice().equals(lastUsedLavCode) && !Lav.getCodice().equals("P1")){
            lp.setOrder(1); // metto per prima l'ultima usata, tranne se pulizia
        }

        btn.setLayoutParams(lp);
        btn.setBackgroundColor(Color.parseColor(Lav.getColore()));

        switch (Lav.getTipo()) {
            case Lavorazione.TEMPORIZZATA_SENZA_NUMERO: //inizio lavoro senza numero
            case Lavorazione.SOLO_INIZIO: //lavoro non temporizzato > INZIO
            case Lavorazione.SOLO_FINE: //lavoro non temporizzato > FINE
            case Lavorazione.TEMPORIZZATA: //lavoro con inizio e fine
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setTitleMainActivity(Lav.getDescrizione());
                        editor.putString(getString(R.string.last_select_lavorazione_code), Lav.getCodice());
                        editor.apply();
                        Lavorazione_MenuFragmentDirections.ActionMenuFragmentToLavorazione1Fragment action =
                                Lavorazione_MenuFragmentDirections.actionMenuFragmentToLavorazione1Fragment(Lav);

                        NavHostFragment.findNavController(Lavorazione_MenuFragment.this)
                                .navigate(action);
                    }
                });
                break;
            case Lavorazione.TEMPORIZZATA_MULTIPLA: //lavoro con inizio multiplo e fine
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setTitleMainActivity(Lav.getDescrizione());
                        editor.putString(getString(R.string.last_select_lavorazione_code), Lav.getCodice());
                        editor.apply();
                        Lavorazione_MenuFragmentDirections.ActionMenuFragmentToLavorazione2Fragment action =
                                Lavorazione_MenuFragmentDirections.actionMenuFragmentToLavorazione2Fragment(Lav);

                        NavHostFragment.findNavController(Lavorazione_MenuFragment.this)
                                .navigate(action);
                    }
                });
                break;
        }
        flexboxLayout.addView(btn);
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitleMainActivity("Scelta lavorazione");
    }

    private void setTitleMainActivity(String title){
        MainLavorazioniActivity mainLavorazioniActivity = (MainLavorazioniActivity) getActivity();
        mainLavorazioniActivity.setTitle(title);
    }
}
