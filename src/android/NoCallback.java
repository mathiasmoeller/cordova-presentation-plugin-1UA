package de.fhg.fokus.famium.presentation;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;

/**
 * This class is an implementation of the null-object pattern for the callback context.
 */
public class NoCallback extends CallbackContext {
    public NoCallback() {
        super(null,null);
    }
    public void sendPluginResult(PluginResult result)
    {
        // Do nothing
    }
}
