package de.fhg.fokus.famium.presentation;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.LOG;
import android.webkit.WebView;


import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;

/**
 * Created by Lo on 19.06.2017.
 */
public class ReceiverProxy  extends ConnectionProxy{
    private static final String LOG_TAG = "ReceiverProxy";

    public ReceiverProxy(PresentationSession session) {
        super(session);
    }

    public void setConnectionSuccessful() {
        LOG.d(LOG_TAG, "setConnectionSuccessful()");
        callReceiverStateChanged(State.connected);
    }

    public void connect() {
        LOG.d(LOG_TAG, "connect()");
        callReceiverStateChanged(State.connecting);
    }

    public void close(String reason, String message) {
        LOG.d(LOG_TAG, "close()");
        callReceiverStateChanged(State.closed, reason, message);
    }

    public void terminate() {
        LOG.d(LOG_TAG, "terminate()");
        callReceiverStateChanged(State.terminated);
    }

    private void callReceiverStateChanged(final State state) {
        callReceiverStateChanged(state,"","");
    }
    private void callReceiverStateChanged(final State state, String reason, String message) {
        LOG.d(LOG_TAG, "callReceiverStateChanged()");

        final String baseUrl = "javascript:NavigatorPresentationJavascriptInterface";
        final SecondScreenPresentation presentation = getSession().getPresentation();

        if(presentation == null)
            return;

        presentation.loadUrl(baseUrl + ".onstatechange('" + getSession().getId() + "','" + state.name() + "','" + reason + "','" + message + "')");
    }
    public void postMessage(final String msg)
    {
        String escapedMsg = msg;

        try{
            escapedMsg = URLEncoder.encode(msg, "UTF-8");
        }
        catch (UnsupportedEncodingException ex)
        {
        }
        getSession().getPresentation().loadUrl("javascript:NavigatorPresentationJavascriptInterface.onmessage('"+getSession().getId()+"',\"" +escapedMsg+"\")");
    }
}
