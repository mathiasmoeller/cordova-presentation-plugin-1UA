package de.fhg.fokus.famium.presentation;
//import mock.*;


import android.app.Activity;
import android.app.Presentation;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Display;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.apache.cordova.LOG;

/**
 * This class is responsible to display the WebView of the presenting page on the connected Presentation Display.
 */
public class SecondScreenPresentation extends Presentation {
    private static final String LOG_TAG = "SecondScreenPresentation";

    private static String DEFAULT_DISPLAY_URL = "about:blank";
    private WebView webView;
    private PresentationSession session;
    private Activity outerContext;
    private String displayUrl;

    /**
     * @param outerContext the parent activity
     * @param display      the {@link Display} associated to this presentation
     * @param displayUrl   the URL of the display html page to present on the display as default page
     */
    public SecondScreenPresentation(Activity outerContext, Display display, String displayUrl) {
        super(outerContext, display);
        this.outerContext = outerContext;
        this.displayUrl = displayUrl == null ? DEFAULT_DISPLAY_URL : displayUrl + "#" + display.getName();
    }

    /**
     * set webview as content view of the presentation
     *
     * @see android.app.Dialog#onCreate(android.os.Bundle)
     */
    protected void onCreate(Bundle savedInstanceState) {
        LOG.d(LOG_TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(getWebView());
    }


    /**
     * destroy webview on stop
     *
     * @see android.app.Presentation#onStop()
     */
    protected void onStop() {
        LOG.d(LOG_TAG, "onStop()");
        super.onStop();
        destroyWebView();
    }

    /**
     * initialize the {@link WebView}: Add JavaScript interface <code>NavigatorPresentationJavascriptInterface</code>, inject the receiver JavaScript code from {@link NavigatorPresentationJS} in the webview after page load is finished and fire <code>deviceready</code> event.
     *
     * @return the webview of the presenting page.
     */
    private WebView getWebView() {
        if (webView == null) {
            initWebView();
        }
        return webView;
    }

    private void initWebView() {
        webView = new WebView(this.getContext());
        webView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setDatabaseEnabled(true);
        webView.getSettings().setUserAgentString("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.117 Safari/537.36");
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                LOG.d(LOG_TAG, "Webview::onPageFinished()");

                view.loadUrl(NavigatorPresentationJS.RECEIVER);
                view.loadUrl("javascript:document.dispatchEvent(new Event('deviceready'));");

                super.onPageFinished(view, url);
            }
        });
        webView.addJavascriptInterface(new ReceiverJavaScriptInterface(this), "NavigatorPresentationJavascriptInterface");
    }


    /**
     * @return the {@link PresentationSession} associated with this presentation or <code>null</code>
     */
    public PresentationSession getSession() {
        return session;
    }

    /**
     * @param session the {@link PresentationSession} to set. if <code>null</code> the default display html page will be displayed instead of the presenting page.
     */
    public void setSession(PresentationSession session) {
        LOG.d(LOG_TAG, "setSession()");

        this.session = session;
        if (session == null) {
            return;
        }

        loadUrl(session.getUrl());
    }


    /**
     * @param url the url of the page to load
     */
    public void loadUrl(final String url) {
        LOG.d(LOG_TAG, "loadUrl(): " + url);
        if (getDisplay() == null) {
            LOG.d(LOG_TAG, "getDisplay is null!");
            return;
        }

        outerContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getWebView().loadUrl(url);
            }
        });
    }

    public void show() {
        LOG.d(LOG_TAG, "show()");
        outerContext.runOnUiThread(new Runnable()
        {
            public void run()
            {
                SecondScreenPresentation.super.show();
            }
        });
    }

    public void cancel() {
        LOG.d(LOG_TAG, "cancel()");
        super.cancel();
        //destroyWebView();
    }

    /**
     * @return the URL of the display html page
     */
    public String getDisplayUrl() {
        return displayUrl;
    }

    public void connect() {
        LOG.d(LOG_TAG, "connect()");
        loadUrl(getDisplayUrl());
        show();
    }

    public void close() {
        LOG.d(LOG_TAG,"close()");
        loadUrl(getDisplayUrl());
    }

    public void terminate() {
        LOG.d(LOG_TAG, "terminate()");
        setSession(null);
        dismiss();
    }

    private void destroyWebView() {
        outerContext.runOnUiThread(new Runnable() {
            public void run() {
                if(SecondScreenPresentation.this.webView != null)
                {
                    getWebView().destroy();
                    SecondScreenPresentation.this.webView = null;
                }
            }
        });
    }
}
