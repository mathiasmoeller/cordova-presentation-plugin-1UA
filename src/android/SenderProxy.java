package de.fhg.fokus.famium.presentation;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;

import android.webkit.WebView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.HashMap;

/**
 * Created by Lo on 19.06.2017.
 */
public class SenderProxy extends ConnectionProxy{
    private static final String LOG_TAG = "SenderProxy";

    public SenderProxy(PresentationSession session) {
        super(session);
    }


    public void setConnectionSuccessful() {
        LOG.d(LOG_TAG, "setConnectionSuccessful()");
        sendStateChangedMessage(State.connected);
    }

    public void connect() {
        LOG.d(LOG_TAG, "connect()");
        sendStateChangedMessage(State.connecting);
    }

    public void close(String reason, String message) {
        LOG.d(LOG_TAG, "close()");
        Map<String,String> values = generateMap("onstatechange", State.closed.name());
        values.put("reason", reason);
        values.put("message", message);

        sendSessionResult(getSession(), values);
    }

    public void terminate() {
        LOG.d(LOG_TAG, "terminate()");
        sendStateChangedMessage(State.terminated);
    }

    private void sendStateChangedMessage(State state) {
        LOG.d(LOG_TAG, "sendStateChangedMessage(" + state.name() + ")");

        sendSessionResult(getSession(), generateMap("onstatechange", state.name()));
    }

    private Map<String, String> generateMap(String eventType, String value) {
        Map<String,String> values = new HashMap<String,String>();
        values.put("eventType", eventType);
        values.put("value", value);
        return values;
    }

    public void postMessage(String msg)
    {
        LOG.d(LOG_TAG, "postMessage(" + msg + ")");
        sendSessionResult(getSession(), generateMap("onmessage", msg));
    }


    /**
     * This is a helper method to send AvailableChange Results to the controlling page. {@code session.onstatechange} will be triggered.
     *
     * @param callbackContext
     * @param available       display availability. <code>true</code> if at least one display is available and <code>false</code> is no display is available
     */
    public static void sendAvailableChangeResult(CallbackContext callbackContext, boolean available) {
        if(callbackContext == null)
            return;
        JSONObject obj = new JSONObject();
        try {
            obj.put("available", available);
            PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
            result.setKeepCallback(true);
            callbackContext.sendPluginResult(result);
            LOG.d(LOG_TAG, obj.toString());
        } catch (JSONException e) {
            LOG.e(LOG_TAG, e.getMessage(), e);
        }
    }

    public static void sendSessionResult(PresentationSession session, Map<String,String> values){
        sendSessionResult(session.getId(), session.getCallbackContext(), values);
    }

    public static void sendSessionResult(String id, CallbackContext callbackContext, Map<String,String> values){

        //if (eventType != null && value != null) {
            //obj.put("eventType", eventType);
            //obj.put("value", value);
        //}

        JSONObject obj = new JSONObject();
        try {
            if(values != null)
            {
                for(Map.Entry<String,String> value : values.entrySet())
                {
                    obj.put(value.getKey(), value.getValue());
                }
            }

            obj.put("id", id);

            PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
            result.setKeepCallback(true);
            callbackContext.sendPluginResult(result);
            LOG.d(LOG_TAG, obj.toString());
        } catch (JSONException e) {
            LOG.e(LOG_TAG, e.getMessage(), e);
        }
    }
}
