package de.fhg.fokus.famium.presentation;
//import mock.*;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;

/**
 * Created by Lo on 19.06.2017.
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
