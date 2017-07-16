package de.fhg.fokus.famium.presentation;
import android.webkit.JavascriptInterface;
import org.apache.cordova.LOG;


public class ReceiverJavaScriptInterface {
    private static final String LOG_TAG = "ReceiverJavaScriptInterface";
    private SecondScreenPresentation presentation;


    public ReceiverJavaScriptInterface(SecondScreenPresentation presentation)
    {
        this.presentation = presentation;
    }

    @JavascriptInterface
    public void setOnPresent() {
        LOG.d(LOG_TAG, "setOnPresent()");

        final PresentationSession session = presentation.getSession();
        if (session == null) {
            return;
        }

        presentation.loadUrl("javascript:NavigatorPresentationJavascriptInterface.onsession({id: '" + presentation.getSession().getId()+"', state: '" + presentation.getSession().getState()+"'})");
        session.setConnectionSuccessful();
    }
    @JavascriptInterface
    public void close(String sessId, String reason, String message) {
        LOG.d(LOG_TAG, "close()");

        PresentationSession session = presentation.getSession();
        if(session != null && session.getId().equals(sessId)){
            session.close(reason, message);
        }
    }
    @JavascriptInterface
    public void postMessage(String sessId, String msg) {
        LOG.d(LOG_TAG, "postMessage()");

        PresentationSession session = presentation.getSession();
        if(session != null && session.getId().equals(sessId)){
            session.postMessageToSender(msg);
        }
    }
    @JavascriptInterface
    public void terminate(String sessId) {
        LOG.d(LOG_TAG, "terminate()");

        PresentationSession session = presentation.getSession();
        if(session != null && session.getId().equals(sessId)){
            session.terminate();
        }
    }
}
