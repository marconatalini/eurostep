package com.marconatalini.eurostep;

import androidx.appcompat.app.AppCompatActivity;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.marconatalini.eurostep.tool.Barcoder;

public class WebActivity extends AppCompatActivity {

    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String url = null;

        // check if Intens is a search
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String ordine_lotto = intent.getStringExtra(SearchManager.QUERY);
            Barcoder barcoder = new Barcoder(ordine_lotto);

            if (!barcoder.isValid()) {
                Toast.makeText(this, "Numero non valido: " + ordine_lotto, Toast.LENGTH_SHORT).show();
                finish();
                return;
            } else {
                url = String.format("http://%s/api/dettaglio/%s/%s",MainActivity.WEBSERVER_IP,barcoder.getNumeroOrdine(), barcoder.getLottoOrdine());
            }
        } else {
            url = intent.getStringExtra("URL");
        }

        webView = findViewById(R.id.eurodbWebView);
        WebViewClient MyWebViewClient = new WebViewClient();
        webView.loadUrl(url);
        webView.setWebViewClient(MyWebViewClient);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}
