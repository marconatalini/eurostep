package com.marconatalini.eurostep.tool;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.marconatalini.eurostep.MainActivity;
import com.marconatalini.eurostep.MySingleton;

import org.ligi.tracedroid.TraceDroid;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class TraceDroidHttpSender {

    public static boolean sendStackTraces(final Context context) {
        if (TraceDroid.getStackTraceFiles().length > 0) {
            new AlertDialog.Builder(context).setTitle("Segnalazione ERRORE")

//                    .setMessage("A Problem with this App was detected - would you send an Crash-Report to help fixing this Problem?")
                    .setMessage("E' stato rilevato un errore che ha fatto chiudere l'applicazione. Vuoi spedire un report per aiutarmi a risolvere il problema?")
                    .setPositiveButton("Invia", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            String stacktrace = null;
                            try {
                                stacktrace = URLEncoder.encode((String) TraceDroid.getStackTraceText(10), "UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                                stacktrace = "";
                            }
                            String getURL = String.format("http://%s/api/errorlog?stacktrace=%s", MainActivity.WEBSERVER_IP, stacktrace);
                            StringRequest sRequest = new StringRequest(Request.Method.GET, getURL,
                                    response -> {
                                        Toast.makeText(context, response.toString(), Toast.LENGTH_SHORT).show();
                            },
                                    error -> {
                                        Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show();
                            });

                            MySingleton.getInstance(context).addToRequestque(sRequest);
                            TraceDroid.deleteStacktraceFiles();
                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    TraceDroid.deleteStacktraceFiles();
                }
            }).setNeutralButton("Dopo", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            })

            .show();

            return true;
        }
        return false;
    }
}
