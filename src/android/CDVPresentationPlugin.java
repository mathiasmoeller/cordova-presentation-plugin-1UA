package de.fhg.fokus.famium.presentation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.hardware.display.DisplayManager;
import android.view.Display;

/**
 * Entry Class for Presentation API Cordova Plugin.
 * This Plugin implements the W3C Presentation API as described in the final report
 * {@link http://www.w3.org/2014/secondscreen/presentation-api/20140721/} of the Second Screen Presentation API Community Group.
 */
public class CDVPresentationPlugin extends CordovaPlugin implements DisplayManager.DisplayListener {
    private static final String LOG_TAG = "CDVPresentationPlugin";

    private CallbackContext availableChangeCallbackContext = new NoCallback();
    private Map<String, PresentationSession> sessions;
    private Map<Integer, SecondScreenPresentation> presentations;
    private DisplayManager displayManager;
    private Activity activity;
    private String defaultDisplay;
    private final Lock mutex = new ReentrantLock(true);


    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        LOG.d(LOG_TAG, "----------INIT----------");
        activity = cordova.getActivity();
        initDisplayManager();
        super.initialize(cordova, webView);
    }

    @Override
    public void onDestroy() {
        getDisplayManager().unregisterDisplayListener(this);
        getPresentations().clear();
        displayManager = null;
        super.onDestroy();
    }

    /**
     * Executes the request and returns PluginResult.
     *
     * @param action          The action to execute.
     * @param args            JSONArray of arguments for the plugin.
     * @param callbackContext The callback context used when calling back into JavaScript.
     * @return True when the action was valid, false otherwise.
     */
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("addWatchAvailableChange")) {
            LOG.d(LOG_TAG, "addWatchAvailableChange");
            return addWatchAvailableChange(args, callbackContext);
        } else if (action.equals("clearWatchAvailableChange")) {
            LOG.d(LOG_TAG, "clearWatchAvailableChange");
            return clearWatchAvailableChange(args, callbackContext);
        } else if (action.equals("getAvailability")) {
            LOG.d(LOG_TAG, "getAvailability");
            return getAvailability(args, callbackContext);
        } else if (action.equals("requestSession")) {
            LOG.d(LOG_TAG, "requestSession");
            return requestSession(args, callbackContext);
        } else if (action.equals("startSession")) {
            LOG.d(LOG_TAG, "startSession");
            return startSession(args, callbackContext);
        } else if (action.equals("reconnectSession")) {
            LOG.d(LOG_TAG, "reconnectSession");
            return reconnectSession(args, callbackContext);
        } else if (action.equals("presentationSessionPostMessage")) {
            LOG.d(LOG_TAG, "presentationSessionPostMessage");
            return presentationSessionPostMessage(args, callbackContext);
        } else if (action.equals("presentationSessionClose")) {
            LOG.d(LOG_TAG, "presentationSessionClose");
            return presentationSessionClose(args, callbackContext);
        } else if (action.equals("presentationSessionTerminate")) {
            LOG.d(LOG_TAG, "presentationSessionTerminate");
            return presentationSessionTerminate(args, callbackContext);
        }
        return false;
    }

    // --------------------------------------------------------------------------
    // LOCAL METHODS
    // --------------------------------------------------------------------------

    /**
     * This method will be called when navigator.presentation.onavailablechange is set to a valid JavaScript function in the controlling page
     *
     * @param args            is an empty {@link JSONArray}
     * @param callbackContext the Cordova {@link CallbackContext} associated with this call
     * @return always true
     * @throws JSONException
     */
    private boolean addWatchAvailableChange(JSONArray args, CallbackContext callbackContext) throws JSONException {
        mutex.lock();

        availableChangeCallbackContext = callbackContext;
        SenderProxy.sendAvailableChangeResult(callbackContext, getPresentations().size() > 0);

        mutex.unlock();
        return true;
    }


    /**
     * Deletes the change listener for availability events
     * @param args ignored
     * @param callbackContext always success
     * @return always true
     * @throws JSONException
     */
    private boolean clearWatchAvailableChange(JSONArray args, CallbackContext callbackContext) throws JSONException {
        mutex.lock();

        availableChangeCallbackContext = new NoCallback();
        callbackContext.success();

        mutex.unlock();
        return true;
    }

    /**
     * This method will be called when {@code navigator.presentation.requestSession(url)} is called in the controlling page.
     * An initial session will be send back to the presenting page.
     *
     * @param args            a {@link JSONArray} with one argument args[0]. args[0] contains the URL of the presenting page to open on the second screen
     * @param callbackContext the Cordova {@link CallbackContext} associated with this call
     * @return
     * @throws JSONException
     */
    private boolean requestSession(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String url = args.getString(0);
        PresentationSession session = new PresentationSession(activity, url);
        getSessions().put(session.getId(), session);
        SenderProxy.sendSessionResult(session.getId(), callbackContext, null);
        return true;
    }


    /**
     * A Display selection dialog will be shown to the user to pick a display.
     * Then the session starts connecting to the receiver.
     * @param args Contains the session id
     * @param callbackContext Includes the session callback that will be used for all session events
     * @return always true
     * @throws JSONException
     */
    private boolean startSession(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String id = args.getString(0);
        PresentationSession session = getSessions().get(id);

        if(session == null) {
            callbackContext.error("Session " + id + " has not been requested yet.");
            return false;
        }
        showDisplaySelectionDialog(session);
        session.setCallbackContext(callbackContext);
        session.connect();
        SenderProxy.sendSessionResult(session, null);
        return true;
    }

    private boolean reconnectSession(JSONArray args, CallbackContext callbackContext) throws JSONException {

        String id = args.getString(0);
        PresentationSession session = getSessions().get(id);
        if(session == null)
        {
            callbackContext.error("Session " + id + " not found.");
            return false;
        }

        SecondScreenPresentation presentation = session.getPresentation();
        if (presentation == null) {
            callbackContext.error("no presentation selected for reconnect");
            return false;
        }
        session.connect();
        SenderProxy.sendSessionResult(session, null);
        return true;
    }

    private boolean getAvailability(JSONArray args, CallbackContext callbackContext) throws JSONException {
        LOG.d(LOG_TAG, "getAvailability(): " + getPresentations().size());

        mutex.lock();
        SenderProxy.sendAvailableChangeResult(callbackContext, getPresentations().size() > 0);
        mutex.unlock();

        return true;
    }

    /**
     * This method will be called when {@code session.postMessage(msg)} is called in the controlling page. {@code session} is the return value of {@code navigator.presentation.requestSession(url)}.
     *
     * @param args            a {@link JSONArray} with two arguments args[0] and args[1]. args[0] is the id of the session associated with this call and args[1] is the message to send to the presenting page.
     * @param callbackContext the Cordova {@link CallbackContext} associated with this call
     * @return
     * @throws JSONException
     */
    private boolean presentationSessionPostMessage(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String id = args.get(0).toString();
        PresentationSession session = getSessions().get(id);
        if (session != null) {
            String msg = args.getString(1);
            session.postMessageToPresentation(msg);
        }
        return true;
    }

    /**
     * This method will be called when {@code session.close()} is called in the controlling page. Session state will be changed to 'disconnected' and both controlling page and receiver page will be notified by triggering {@code session.onstatechange} if set.
     *
     * @param args            a {@link JSONArray} with one argument args[0]. args[0] is the id of the session associated with this call.
     * @param callbackContext the Cordova {@link CallbackContext} associated with this call
     * @return
     * @throws JSONException
     */
    private boolean presentationSessionClose(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String id = args.get(0).toString();
        String reason = args.get(1).toString();
        String message = args.get(2).toString();

        PresentationSession session = getSessions().get(id);
        if (session != null) {
            session.close(reason, message);
            callbackContext.success();
        } else {
            callbackContext.error("session not found");
        }
        return true;
    }

    private boolean presentationSessionTerminate(JSONArray args, final CallbackContext callbackContext) throws JSONException {
        final String id = args.get(0).toString();
        final PresentationSession session = getSessions().remove(id);
        if (session == null) {
            callbackContext.error("session not found");
            return false;
        }
        activity.runOnUiThread(new Runnable()
        {
            public void run()
            {
                LOG.d(LOG_TAG, "presentationSessionTerminate(id: " + id + ")");
                recreatePresentationObject(session);
                session.terminate();
                getSessions().remove(id);
                callbackContext.success();
            }
        });

        return true;
    }

    /**
     * This method ultimately destroys the Presentation Object associated with the terminated session
     * and creates a new Presentation Object for the now reusable display.
     * @param session The terminated session
     */
    private void recreatePresentationObject(PresentationSession session) {
        LOG.d(LOG_TAG, "recreatePresentationObject(" + session.getId()  + ")");

        SecondScreenPresentation oldPresentation = session.getPresentation();
        if (oldPresentation == null) {
            return;
        }
        oldPresentation.dismiss();

        Display display = oldPresentation.getDisplay();
        if (display == null) {
            return;
        }

        SecondScreenPresentation newPresentation = new SecondScreenPresentation(activity, display);
        getPresentations().put(display.getDisplayId(), newPresentation);
    }

    private DisplayManager getDisplayManager() {
        if (displayManager == null) {
            initDisplayManager();
        }
        return displayManager;
    }

    private void initDisplayManager() {
        displayManager = (DisplayManager) activity.getSystemService(Activity.DISPLAY_SERVICE);
        displayManager.registerDisplayListener(this, null);
        for (Display display : displayManager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION)) {
            addDisplay(display);
        }
    }

    private Map<String, PresentationSession> getSessions() {
        if (sessions == null) {
            sessions = new HashMap<String, PresentationSession>();
        }
        return sessions;
    }

    private Map<Integer, SecondScreenPresentation> getPresentations() {
        if (presentations == null) {
            presentations = new HashMap<Integer, SecondScreenPresentation>();
        }
        return presentations;
    }

    private void showDisplaySelectionDialog(final PresentationSession session) {

        Collection<SecondScreenPresentation> collection = getPresentations().values();
        int size = collection.size();
        int counter = 0;
        final SecondScreenPresentation presentations[] = new SecondScreenPresentation[size];
        String items[] = new String[size];
        for (SecondScreenPresentation presentation : collection) {
            presentations[counter] = presentation;
            items[counter++] = presentation.getDisplay().getName();
        }
        AlertDialog.Builder builder = createAlertDialogBuilder(session, presentations, items);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private AlertDialog.Builder createAlertDialogBuilder(final PresentationSession session, final SecondScreenPresentation[] presentations, final String[] items) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Select Presentation Display").setItems(
                items,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        SecondScreenPresentation presentation = presentations[which];
                        session.assignPresentation(presentation);
                        getSessions().put(session.getId(), session);
                    }
                })
                .setCancelable(false)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
            }
        });
        return builder;
    }

    @Override
    public void onDisplayAdded(int displayId) {
        Display display = getDisplayManager().getDisplay(displayId);
        addDisplay(display);
    }

    @Override
    public void onDisplayChanged(int displayId) {
        // nothing todo for now
    }

    @Override
    public void onDisplayRemoved(int displayId) {
        removeDisplay(displayId);
    }

    private void addDisplay(final Display display) {
        LOG.d(LOG_TAG, "addDisplay(): " + display.getName());
        if (!isPresentationDisplay(display)) {
            LOG.d(LOG_TAG, "Display " + display.getName() + " cannot show presentations.");
            return;
        }
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                boolean hadPresentations = getPresentations().size() > 0;

                LOG.d(LOG_TAG, "creating a new SecondScreenPresentation inside Plugin::addDisplay");
                SecondScreenPresentation presentation = new SecondScreenPresentation(activity, display);
                getPresentations().put(display.getDisplayId(), presentation);
                LOG.d(LOG_TAG, "addDisplay(). Finished adding: " + display.getName());

                LOG.d(LOG_TAG, "hadPresentations=" + (hadPresentations ? "true" : "false"));
                LOG.d(LOG_TAG, "Now has " + getPresentations().size() + " Presentations.");
                if (!hadPresentations && getPresentations().size() > 0) {
                    LOG.d(LOG_TAG, "Sending availability.");
                    SenderProxy.sendAvailableChangeResult(availableChangeCallbackContext, true);
                }
            }
        });
    }

    private boolean isPresentationDisplay(Display display) {
        return (display.getFlags() & Display.FLAG_PRESENTATION) != 0;
    }

    private void removeDisplay(int displayId) {
        int oldSize = getPresentations().size();
        final SecondScreenPresentation presentation = getPresentations().remove(displayId);
        if (presentation != null) {
            PresentationSession session = presentation.getSession();
            if (session != null) {
                session.assignPresentation(null);
                getSessions().remove(session.getId());
            }
        }
        int newSize = getPresentations().size();
        if (oldSize > 0 && newSize == 0) {
            SenderProxy.sendAvailableChangeResult(availableChangeCallbackContext, false);
        }
    }
}
