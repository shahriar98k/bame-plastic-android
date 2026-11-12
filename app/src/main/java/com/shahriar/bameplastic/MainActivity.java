package com.shahriar.bameplastic;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {
    // The live game. Whatever you push to the web repo shows up here automatically.
    static final String GAME_URL = "https://shahriar98k.github.io/bame-plastic-web/";
    // Offline copy that ships inside the APK (used when the live game cannot be reached).
    static final String BUNDLED_URL = "file:///android_asset/game/index.html";
    // The app itself (rarely changes). A newer APK here triggers the "update" popup.
    static final String UPDATE_URL = "https://github.com/shahriar98k/bame-plastic-android/releases/download/latest/update.json";

    private WebView web;
    private boolean usingBundled = false;

    /** Saves the player name / device id inside the app so they survive restarts and the online/offline switch. */
    public class Store {
        private final SharedPreferences sp;
        Store(Context c) { sp = c.getSharedPreferences("bp", MODE_PRIVATE); }
        @JavascriptInterface public String get(String k) { return sp.getString(k, null); }
        @JavascriptInterface public void set(String k, String v) { sp.edit().putString(k, v).apply(); }
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (Build.VERSION.SDK_INT >= 28) {
            getWindow().getAttributes().layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        web = new WebView(this);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setAllowFileAccess(true);
        web.addJavascriptInterface(new Store(this), "AndroidStore");
        web.setWebChromeClient(new WebChromeClient());
        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
                String u = r.getUrl().toString();
                if (u.startsWith(GAME_URL) || u.startsWith("file:///android_asset/")) return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW, r.getUrl())); } catch (Exception e) { }
                return true;
            }
            @Override
            public void onReceivedError(WebView v, WebResourceRequest r, WebResourceError e) {
                if (r.isForMainFrame()) fallback(v);
            }
            @Override
            public void onReceivedHttpError(WebView v, WebResourceRequest r, WebResourceResponse resp) {
                if (r.isForMainFrame() && resp.getStatusCode() >= 400) fallback(v);
            }
        });
        setContentView(web);
        web.clearCache(true); // always fetch the newest game files from the site
        web.loadUrl(GAME_URL);
        checkForAppUpdate();
    }

    private void fallback(WebView v) {
        if (usingBundled) return;
        usingBundled = true;
        v.loadUrl(BUNDLED_URL);
    }

    private void checkForAppUpdate() {
        new Thread(() -> {
            try {
                HttpURLConnection c = (HttpURLConnection) new URL(UPDATE_URL).openConnection();
                c.setConnectTimeout(8000);
                c.setReadTimeout(8000);
                c.setInstanceFollowRedirects(true);
                if (c.getResponseCode() != 200) return;
                StringBuilder sb = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream(), "UTF-8"))) {
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line);
                }
                JSONObject j = new JSONObject(sb.toString());
                int remote = j.getInt("versionCode");
                @SuppressWarnings("deprecation")
                int local = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
                final String apk = j.getString("apk");
                final String notes = j.optString("notes", "");
                if (remote > local) runOnUiThread(() -> showUpdate(apk, notes));
            } catch (Exception ignored) { }
        }).start();
    }

    private void showUpdate(String apkUrl, String notes) {
        if (isFinishing()) return;
        new AlertDialog.Builder(this)
            .setTitle("নতুন আপডেট এসেছে")
            .setMessage(notes.isEmpty() ? "অ্যাপের নতুন ভার্সন পাওয়া গেছে। আপডেট করতে চান?" : notes)
            .setPositiveButton("আপডেট", (d, w) -> {
                try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(apkUrl))); } catch (Exception e) { }
            })
            .setNegativeButton("পরে", null)
            .show();
    }

    @Override
    public void onWindowFocusChanged(boolean f) {
        super.onWindowFocusChanged(f);
        if (f) {
            getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override protected void onPause() { super.onPause(); web.onPause(); }
    @Override protected void onResume() { super.onResume(); web.onResume(); }
    @Override protected void onDestroy() { web.destroy(); super.onDestroy(); }
}
