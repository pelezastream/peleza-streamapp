package com.pelezastream.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.EditText;
import android.app.AlertDialog;

import java.util.Locale;

public class MainActivity extends Activity {
    private static final int FILE_CHOOSER_REQUEST = 1001;

    private WebView webView;
    private ProgressBar progressBar;
    private FrameLayout fullscreenContainer;
    private View customView;
    private WebChromeClient.CustomViewCallback customViewCallback;
    private ValueCallback<Uri[]> filePathCallback;
    private String lastMainFrameUrl = BuildConfig.HOME_URL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.web_view);
        progressBar = findViewById(R.id.progress);
        fullscreenContainer = findViewById(R.id.fullscreen_container);

        configureWebView();
        configureBackNavigation();
        configureBottomNavigation();

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        } else {
            loadIntentOrHome(getIntent());
        }
    }

    private final int[] navTabs = {
            R.id.nav_home, R.id.nav_news, R.id.nav_jobs, R.id.nav_education, R.id.nav_more
    };
    private final int[] navIcons = {
            R.id.nav_icon_home, R.id.nav_icon_news, R.id.nav_icon_jobs,
            R.id.nav_icon_education, R.id.nav_icon_more
    };
    private final int[] navLabels = {
            R.id.nav_label_home, R.id.nav_label_news, R.id.nav_label_jobs,
            R.id.nav_label_education, R.id.nav_label_more
    };

    private void configureBottomNavigation() {
        findViewById(R.id.nav_home).setOnClickListener(v -> openSection("", 0));
        findViewById(R.id.nav_news).setOnClickListener(v -> openSection("category/news/", 1));
        findViewById(R.id.nav_jobs).setOnClickListener(v -> openSection("jobs/", 2));
        findViewById(R.id.nav_education).setOnClickListener(v -> openSection("category/education/", 3));
        findViewById(R.id.nav_more).setOnClickListener(v -> showMoreMenu());
        selectTab(0);
    }

    private void openSection(String path, int activeTab) {
        selectTab(activeTab);
        webView.loadUrl(BuildConfig.HOME_URL + path);
    }

    private void selectTab(int selectedTab) {
        int highlighted = getColor(R.color.peleza_red_bright);
        int muted = getColor(R.color.peleza_muted);
        for (int i = 0; i < navTabs.length; i++) {
            LinearLayout tab = findViewById(navTabs[i]);
            ImageView icon = findViewById(navIcons[i]);
            TextView title = findViewById(navLabels[i]);
            boolean selected = i == selectedTab;
            if (selected) {
                tab.setBackgroundResource(R.drawable.nav_tab_selected);
            } else {
                tab.setBackgroundColor(Color.TRANSPARENT);
            }
            icon.setColorFilter(selected ? highlighted : muted);
            title.setTextColor(selected ? highlighted : muted);
            title.setTypeface(null, selected ? Typeface.BOLD : Typeface.NORMAL);
            tab.setSelected(selected);
        }
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void showMoreMenu() {
        Dialog sheet = new Dialog(this);
        LinearLayout menu = new LinearLayout(this);
        menu.setOrientation(LinearLayout.VERTICAL);
        menu.setPadding(dp(20), dp(17), dp(20), dp(22));
        menu.setBackgroundResource(R.drawable.nav_sheet_background);

        TextView heading = new TextView(this);
        heading.setText("Explore Peleza Updates");
        heading.setTextSize(18);
        heading.setTypeface(null, Typeface.BOLD);
        heading.setTextColor(getColor(R.color.peleza_black));
        heading.setPadding(dp(10), 0, dp(10), dp(12));
        menu.addView(heading, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        addMoreOption(menu, sheet, "Opportunities", R.drawable.ic_nav_opportunities,
                () -> openSection("category/opportunities/", 4));
        addMoreOption(menu, sheet, "Entertainment", R.drawable.ic_nav_entertainment,
                () -> openSection("category/entertainment/", 4));
        addMoreOption(menu, sheet, "Community", R.drawable.ic_nav_community,
                () -> openSection("category/community/", 4));
        addMoreOption(menu, sheet, "Search", R.drawable.ic_nav_search, this::showSearchDialog);
        addMoreOption(menu, sheet, "Refresh", R.drawable.ic_nav_refresh, () -> webView.reload());

        sheet.setContentView(menu);
        sheet.show();
        Window window = sheet.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setGravity(Gravity.BOTTOM);
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void addMoreOption(LinearLayout menu, Dialog sheet, String title, int drawable, Runnable action) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(14), 0, dp(14), 0);
        row.setClickable(true);
        row.setFocusable(true);
        row.setContentDescription(title);
        TypedValue background = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.selectableItemBackground, background, true)) {
            row.setBackgroundResource(background.resourceId);
        }

        ImageView symbol = new ImageView(this);
        symbol.setImageResource(drawable);
        symbol.setColorFilter(getColor(R.color.peleza_red));
        symbol.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        row.addView(symbol, new LinearLayout.LayoutParams(dp(24), dp(24)));

        TextView label = new TextView(this);
        label.setText(title);
        label.setTextSize(15);
        label.setTextColor(getColor(R.color.peleza_black));
        label.setPadding(dp(20), 0, 0, 0);
        label.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        row.addView(label, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        row.setOnClickListener(v -> {
            sheet.dismiss();
            action.run();
        });
        menu.addView(row, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(54)));
    }

    private void showSearchDialog() {
        EditText field = new EditText(this);
        field.setSingleLine(true);
        field.setHint("Search PELEZA UPDATES");
        new AlertDialog.Builder(this).setTitle("Search Articles").setView(field)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Search", (dialog, which) -> {
                    String term = field.getText().toString().trim();
                    if (!term.isEmpty()) {
                        selectTab(4);
                        webView.loadUrl(BuildConfig.HOME_URL + "?s=" + Uri.encode(term));
                    }
                }).show();
    }

    private void updateSelectedTabForUrl(String url) {
        if (url == null) return;
        Uri uri = Uri.parse(url);
        if (!isInternalHttpUrl(uri)) return;
        String path = uri.getPath() == null ? "/" : uri.getPath().toLowerCase(Locale.ROOT);
        if (path.equals("/")) {
            selectTab(uri.getQueryParameter("s") == null ? 0 : 4);
        } else if (path.startsWith("/category/news") || path.startsWith("/news/")) {
            selectTab(1);
        } else if (path.startsWith("/jobs/") || path.startsWith("/category/jobs")) {
            selectTab(2);
        } else if (path.startsWith("/category/education")) {
            selectTab(3);
        } else if (path.startsWith("/category/opportunities")
                || path.startsWith("/category/entertainment")
                || path.startsWith("/category/community")) {
            selectTab(4);
        }
    }

    private void configureWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccess(true);
        settings.setAllowFileAccessFromFileURLs(false);
        settings.setAllowUniversalAccessFromFileURLs(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        settings.setUserAgentString(settings.getUserAgentString() + " PelezaUpdatesAndroid/" + BuildConfig.VERSION_NAME);

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webView, true);


        webView.setWebViewClient(new PelezaWebViewClient());
        webView.setWebChromeClient(new PelezaChromeClient());
        webView.setDownloadListener(new PelezaDownloadListener());
    }

    private void configureBackNavigation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                    android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,
                    this::handleBack
            );
        }
    }

    private void loadIntentOrHome(Intent intent) {
        Uri data = intent != null ? intent.getData() : null;
        if (data != null && isInternalHttpUrl(data)) {
            lastMainFrameUrl = data.toString();
            webView.loadUrl(data.toString());
        } else {
            webView.loadUrl(BuildConfig.HOME_URL);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        loadIntentOrHome(intent);
    }

    private boolean handleUri(Uri uri) {
        if (uri == null) return false;
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);

        if ("http".equals(scheme) || "https".equals(scheme)) {
            if (isInternalHttpUrl(uri)) return false;
            return openExternal(uri);
        }

        if ("about".equals(scheme) || "javascript".equals(scheme) || "blob".equals(scheme) || "data".equals(scheme)) {
            return false;
        }

        if ("intent".equals(scheme)) {
            try {
                Intent parsed = Intent.parseUri(uri.toString(), Intent.URI_INTENT_SCHEME);
                startActivity(parsed);
                return true;
            } catch (Exception ignored) {
                return true;
            }
        }

        return openExternal(uri);
    }

    private boolean isInternalHttpUrl(Uri uri) {
        String host = uri.getHost();
        if (host == null) return false;
        host = host.toLowerCase(Locale.ROOT);
        return BuildConfig.PRIMARY_HOST.equals(host) || BuildConfig.APEX_HOST.equals(host);
    }

    private boolean openExternal(Uri uri) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        } catch (ActivityNotFoundException error) {
            Toast.makeText(this, "No app can open this link.", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    private void showFallback() {
        if (webView.getUrl() != null && webView.getUrl().startsWith("file:///android_asset/offline.html")) return;
        webView.loadUrl("file:///android_asset/offline.html");
    }

    private void handleBack() {
        if (customView != null) {
            hideCustomView();
        } else if (webView.canGoBack()) {
            webView.goBack();
        } else {
            finish();
        }
    }

    @Override
    @SuppressLint("GestureBackNavigation")
    @SuppressWarnings("deprecation")
    public void onBackPressed() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            handleBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        webView.saveState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        webView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.setWebChromeClient(null);
            webView.setWebViewClient(null);
            webView.destroy();
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_REQUEST && filePathCallback != null) {
            Uri[] results = null;
            if (resultCode == RESULT_OK && data != null) {
                String dataString = data.getDataString();
                if (dataString != null) {
                    results = new Uri[]{Uri.parse(dataString)};
                } else if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    results = new Uri[count];
                    for (int i = 0; i < count; i++) {
                        results[i] = data.getClipData().getItemAt(i).getUri();
                    }
                }
            }
            filePathCallback.onReceiveValue(results);
            filePathCallback = null;
        }
    }

    private void hideSystemUiForVideo() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    }

    private void restoreSystemUi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.show(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }
    }

    private void hideCustomView() {
        if (customView == null) return;
        fullscreenContainer.removeView(customView);
        fullscreenContainer.setVisibility(View.GONE);
        webView.setVisibility(View.VISIBLE);
        restoreSystemUi();
        customView = null;
        if (customViewCallback != null) {
            customViewCallback.onCustomViewHidden();
            customViewCallback = null;
        }
    }

    private final class PelezaWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return handleUri(request.getUrl());
        }

        @Override
        @SuppressWarnings("deprecation")
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return handleUri(Uri.parse(url));
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            if (url != null && (url.startsWith("https://") || url.startsWith("http://"))) {
                lastMainFrameUrl = url;
            }
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progressBar.setVisibility(View.GONE);
            updateSelectedTabForUrl(url);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            if (request.isForMainFrame()) {
                showFallback();
            }
        }

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            super.onReceivedHttpError(view, request, errorResponse);
            if (!request.isForMainFrame()) return;
            int status = errorResponse.getStatusCode();
            if (status == 408 || status == 429 || status >= 500) {
                showFallback();
            }
        }
    }

    private final class PelezaChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            progressBar.setProgress(newProgress);
            progressBar.setVisibility(newProgress >= 100 ? View.GONE : View.VISIBLE);
        }

        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> callback, FileChooserParams params) {
            if (filePathCallback != null) filePathCallback.onReceiveValue(null);
            filePathCallback = callback;
            try {
                Intent chooser = params.createIntent();
                chooser.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, params.getMode() == FileChooserParams.MODE_OPEN_MULTIPLE);
                startActivityForResult(chooser, FILE_CHOOSER_REQUEST);
                return true;
            } catch (ActivityNotFoundException error) {
                filePathCallback = null;
                Toast.makeText(MainActivity.this, "No file picker is available.", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            if (customView != null) {
                callback.onCustomViewHidden();
                return;
            }
            customView = view;
            customViewCallback = callback;
            webView.setVisibility(View.GONE);
            fullscreenContainer.setVisibility(View.VISIBLE);
            fullscreenContainer.addView(view, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
            hideSystemUiForVideo();
        }

        @Override
        public void onHideCustomView() {
            hideCustomView();
        }
    }

    private final class PelezaDownloadListener implements DownloadListener {
        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
            try {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setTitle(android.webkit.URLUtil.guessFileName(url, contentDisposition, mimetype));
                request.setMimeType(mimetype);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.addRequestHeader("User-Agent", userAgent);
                String cookies = CookieManager.getInstance().getCookie(url);
                if (cookies != null) request.addRequestHeader("Cookie", cookies);
                DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                manager.enqueue(request);
                Toast.makeText(MainActivity.this, "Download started.", Toast.LENGTH_SHORT).show();
            } catch (Exception error) {
                openExternal(Uri.parse(url));
            }
        }
    }
}
