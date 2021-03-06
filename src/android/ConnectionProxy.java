package de.fhg.fokus.famium.presentation;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.LOG;

/**
 * This class acts as an abstraction layer for Sender and ReceiverProxy
 */
public abstract class ConnectionProxy {
    private PresentationSession session;

    public ConnectionProxy(PresentationSession session) {
        this.session = session;
    }

    public PresentationSession getSession()
    {
        return session;
    }

    public abstract void setConnectionSuccessful();

    public abstract void connect();

    public abstract void close(String reason, String message);

    public abstract void terminate();

    public abstract void postMessage(String msg);
}
