package com.marconatalini.eurostep.tool;

import com.marconatalini.eurostep.entity.Lavorazione;
import com.marconatalini.eurostep.entity.LinkStep;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LavorazioniParser {
    // We don't use namespaces
    private static final String ns = null;

    /*public List <Lavorazione> parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readLavorazioni(parser);
        } finally {
            in.close();
        }
    }*/

    public List <Lavorazione> readLavorazioni(XmlPullParser parser) throws IOException, XmlPullParserException {
        List <Lavorazione> entries = new ArrayList <Lavorazione> ();

        /*int eventType = -1;
        while (eventType != parser.END_DOCUMENT){
            String name1 = parser.getName();
            eventType = parser.next();
        }*/

        parser.next();
        parser.next();

        parser.require(XmlPullParser.START_TAG, ns, "lavorazioni");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("entry")) {
                entries.add(readEntry(parser));
            } else {
                skip(parser);
            }
        }
        return entries;
    }

    private Lavorazione readEntry(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "entry");
        String descrizione = null;
        String codice = null;
        String tipo = null;
        String colore = null;
        String linkedTo = null;
        String needCart = null;
        String cartCode = null;
        String checkComplete = null;
        List<LinkStep> linkSteps = null;

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("descrizione")) {
                descrizione = readInfo(parser, "descrizione");
            } else if (name.equals("codice")) {
                codice = readInfo(parser, "codice");
            } else if (name.equals("tipo")) {
                tipo = readInfo(parser, "tipo");
            } else if (name.equals("colore")) {
                colore = readInfo(parser, "colore");
            } else if (name.equals("linkedTo")) {
                linkedTo = readInfo(parser, "linkedTo");
            } else if (name.equals("needCart")) {
                needCart = readInfo(parser, "needCart");
            } else if (name.equals("cartCode")) {
                cartCode = readInfo(parser, "cartCode");
            } else if (name.equals("checkComplete")) {
                checkComplete = readInfo(parser, "checkComplete");
            } else if (name.equals("linkStep")) {
                linkSteps = readLinkStep(parser);
            } else {
                skip(parser);
            }
        }

        Lavorazione L = new Lavorazione(descrizione, codice, tipo, colore, linkedTo, needCart, cartCode, checkComplete);
        if (linkSteps != null) {
            for (LinkStep step: linkSteps) {
                L.addLinkstep(step);
            }
        }

        return L;
    }

    private String readInfo(XmlPullParser parser, String name) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, name);
        String info = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, name);
        return info;
    }

    private List<LinkStep> readLinkStep(XmlPullParser parser) throws IOException, XmlPullParserException {
        List<LinkStep> linkSteps = new ArrayList<LinkStep>();
//        parser.next();

        /*parser.require(XmlPullParser.START_TAG, ns, "step");
        linkSteps.add(readStep(parser));
        parser.require(XmlPullParser.END_TAG, ns, "step");
        return linkSteps;*/

        parser.require(XmlPullParser.START_TAG, ns, "linkStep");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("step")) {
                linkSteps.add(readStep(parser));
            } else {
                skip(parser);
            }
        }
        return linkSteps;
    }

    private LinkStep readStep(XmlPullParser parser) throws IOException, XmlPullParserException {
        String descrizione = null;
        String codice = null;
        String domanda = null;

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("descrizione")) {
                descrizione = readInfo(parser, "descrizione");
            } else if (name.equals("codice")) {
                codice = readInfo(parser, "codice");
            } else if (name.equals("domanda")) {
                domanda = readInfo(parser, "domanda");
            } else {
                skip(parser);
            }
        }

        return new LinkStep(codice, descrizione, domanda);
    }


    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}
