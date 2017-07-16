/*
 * Copyright 2014 Fraunhofer FOKUS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * AUTHORS: Louay Bassbouss <louay.bassbouss@fokus.fraunhofer.de>
 *          Martin Lasak <martin.lasak@fokus.fraunhofer.de>
 */
package de.fhg.fokus.famium.presentation;

import java.math.BigInteger;
import java.security.SecureRandom;

import org.apache.cordova.LOG;
import org.apache.cordova.CallbackContext;

import android.app.Activity;

/**
 * This class is the Java representation of a <code>PresentationSession</code> defined in the JavaScript Presentation API.
 *
 */
public class PresentationSession{
    private static final String LOG_TAG = "PresentationSession";

	private String id;
	private String url;
	private Activity activity;
	private CallbackContext callbackContext = new NoCallback();
	private State state;
	private SecondScreenPresentation presentation;
    private ConnectionProxy receiverProxy;
    private ConnectionProxy senderProxy;

	/**
	 * 
	 * @param activity the parent activity associated with this session
	 * @param url the URL of the presenting page passed by calling <code>navigator.presentation.requestSession(url)</code>
	 * @param callbackContext The Cordova {@link CallbackContext} associated with the <code>navigator.presentation.requestSession(url)</code> call
	 */
	public PresentationSession(Activity activity, String url) {
		this.id = new BigInteger(130, new SecureRandom()).toString(32);
		this.url = url;
		this.activity = activity;
		this.state = State.terminated;
        this.receiverProxy = new ReceiverProxy(this);
        this.senderProxy = new SenderProxy(this);
	}
	
	/**
	 * @return the parent activity
	 */
	public Activity getActivity() {
		return activity;
	}
	
	/**
	 * @return the {@link CallbackContext}
	 */
	public CallbackContext getCallbackContext() {
		return callbackContext;
	}

	/**
	 * @param callbackContext set the callbackContext.
	 *
	 */
	public void setCallbackContext(CallbackContext callbackContext) {
		this.callbackContext = callbackContext;
	}

	/**
	 * @return the URL of the presenting page
	 */
	public String getUrl() {
		return url;
	}
	
	/**
	 * @return the session ID. It will be created randomly in the constructor 
	 */
	public String getId() {
		return id;
	}

	public State getState() {
		return state;
	}

	public void runOnUiThread(Runnable runnable)
    {
        getActivity().runOnUiThread(runnable);
    }


	/**
	 * @return the {@link SecondScreenPresentation} associated with this session to display the presenting page on it.
	 */
	public SecondScreenPresentation getPresentation() {
		return presentation;
	}
	
	/**
	 * @param presentation the {@link SecondScreenPresentation} associated with this session. State will change to <code>disconnected</code> if value of presentation is <code>null</code>
	 */
	public void assignPresentation(SecondScreenPresentation presentation) {
        LOG.d(LOG_TAG, "assignPresentation()");
        this.presentation = presentation;
        if(presentation != null)
        {
            this.presentation.setSession(this);
        }
        else
        {
            senderProxy.terminate();
        }
    }

    public void postMessageToPresentation(final String msg)
    {
        LOG.d(LOG_TAG, "postMessageToPresentation()");
        if(!isConnected())
            return;
        receiverProxy.postMessage(msg);

    }
    public void postMessageToSender(String msg) {
        LOG.d(LOG_TAG, "postMessageToSender()");
        if(!isConnected())
            return;
        senderProxy.postMessage(msg);
    }

    private void setState(State state)
    {
        LOG.d(LOG_TAG, "setState(" + state.name() + ")");

        if(getState() == state)
            return;
        this.state = state;
    }

    private boolean isConnected() {
        LOG.d(LOG_TAG, "isConnected()");
        return getState() == State.connected;
    }

    public void setConnectionSuccessful() {
        LOG.d(LOG_TAG, "setConnectionSuccessful()");
        setState(State.connected);

        receiverProxy.setConnectionSuccessful();
        senderProxy.setConnectionSuccessful();
        getPresentation().show();
    }

    public void connect() {
        LOG.d(LOG_TAG, "connect()");
        setState(State.connecting);

        receiverProxy.connect();
        senderProxy.connect();
    }

    public void close(String reason, String message) {
        LOG.d(LOG_TAG, "close()");
        setState(State.closed);

        receiverProxy.close(reason, message);
        senderProxy.close(reason, message);
        //presentation.close();
    }

    public void terminate() {
        LOG.d(LOG_TAG, "terminate()");
        setState(State.terminated);

        receiverProxy.terminate();
        senderProxy.terminate();

        if (getPresentation() != null) {
            getPresentation().terminate();
        }
        presentation = null;
    }
}
