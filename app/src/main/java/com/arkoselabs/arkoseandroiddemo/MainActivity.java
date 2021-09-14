package com.arkoselabs.arkoseandroiddemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.net.Uri;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;


public class MainActivity extends AppCompatActivity {

    public static final String JS_ARKOSE_INTERFACE_NAME = "ARKOSE";

    // UI references
    private WebView webView = null;
    private FrameLayout webViewFrame;
    public Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        // The web view and it's container will initially be invisible
        this.webViewFrame = findViewById(R.id.webViewFrame);
        webViewFrame.setVisibility(View.INVISIBLE);

        // Setup the web view
        this.webView = findViewById(R.id.webView);
        webView.setWebChromeClient(new WebChromeClient());
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        // Add a JavaScript interface to the web view that will be used to process messages
        // from the JavaScript callbacks in the Arkose API
        webView.addJavascriptInterface(new ArkoseJavaScriptInterface(), JS_ARKOSE_INTERFACE_NAME);

        // Stop the web View from being able to scroll it's content. This allows the web view to
        // be tighter against the iFrame without odd scrolling occurring
        webView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return (event.getAction() == MotionEvent.ACTION_MOVE);
            }
        });

        loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(v);
                // Grab text that has been entered into the password field. This is just for demo
                // purposes, so when a password of bot is used the user agent is changed causing a
                // challenge to show
                EditText password = findViewById(R.id.password);
                if (password.getText().toString().equals("bot")) {
                    webView.getSettings().setUserAgentString("bad_ua_to_trigger_an_enforcement_challenge");
                }

                // Load the local HTML file that hosts the Arkose Labs API.
                String url = Uri.parse("file:///android_asset/ArkoseLabsAPI.html").toString();
                webView.loadUrl(url);
            }
        });
    }

    // Hide the virtual keyboard when called
    private void hideKeyboard(View v) {
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(v.getApplicationWindowToken(),0);
    }

    // Javascript interface used to processed messages send from the HTML file hosting the Arkose
    // Labs API
    final class ArkoseJavaScriptInterface {

        @JavascriptInterface
        public void onCompleted(String response) {
            Log.i("onCompleted: ", response);
            // TODO: Pass response.token to your own server where the Arkose verify API can be called using your private key
        }

        @JavascriptInterface
        public void onReady(String response) {
            Log.i("onReady: ", response);
        }

        @JavascriptInterface
        public void onReset(String response) {
            Log.i("onReset: ", response);
        }

        @JavascriptInterface
        public void onHide(String response) {
            Log.i("onHide: ", response);
        }

        @JavascriptInterface
        public void onSuppress(String response) {
            Log.i("onSuppress: ", response);
        }

        @JavascriptInterface
        public void onShown(String response) {
            Log.i("onShown: ", response);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    webViewFrame.setVisibility(View.VISIBLE);
                }
            });
        }

        @JavascriptInterface
        public void setInvisible() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    webViewFrame.setVisibility(View.INVISIBLE);
                }
            });
        }
    }
}

