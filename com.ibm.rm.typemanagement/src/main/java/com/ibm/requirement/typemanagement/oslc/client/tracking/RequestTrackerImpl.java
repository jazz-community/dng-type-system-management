/*******************************************************************************
 * Copyright (c) 2012 - 2013, 2018 IBM Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 *
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *
 *    Matthias Buettgen - Initial implementation
 *    Ralph Schoon		- Small changes 
 *******************************************************************************/
package com.ibm.requirement.typemanagement.oslc.client.tracking;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

import org.eclipse.lyo.client.oslc.OSLCConstants;
import org.eclipse.lyo.client.oslc.OslcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements a tracker used to track long running operations.
 *
 */
public class RequestTrackerImpl implements IRequestTracker {

	public static final Logger logger = LoggerFactory.getLogger(RequestTrackerImpl.class);

	private static HashMap<String, RequestTrackerImpl> instances = new HashMap<String, RequestTrackerImpl>();

	private OslcClient client;
	private Map<String, String> headers;
	private String trackerUri;
	private JsonObject tracker;
	private JsonObject jsonResultObject;

	public synchronized URI getVerdict() throws URISyntaxException {
		JsonObject verdict = tracker.getJsonArray(IRequestTracker.TRAKCER_VERDICT_URI).getJsonObject(0);
		return new URI(verdict.getString("value"));
	}

	public synchronized URI getState() throws URISyntaxException {

		tracker = getTrackerResult();
		JsonObject state = tracker.getJsonArray(IRequestTracker.TRACKER_STATE_URI).getJsonObject(0);

		return new URI(state.getString("value"));
	}

	public synchronized String getMessage() {
		String message = "Tracker: '" + trackerUri + "'";
		try {
			message += "state: " + getState().toString();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		try {
			message += " verdict: " + getState().toString();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		JsonArray references = tracker.getJsonArray(IRequestTracker.TRACKER_REFERENCE_URI);
		message += "[";
		for (JsonValue reference : references) {
			message += reference.toString();
		}
		message += "]";
		JsonObject title = tracker.getJsonArray(OSLCConstants.DC_TITLE_PROP).getJsonObject(0);
		message += "{ " + title.getString("value") + " }";
		return message;
	}

	public static synchronized IRequestTracker getInstance(String uri, OslcClient client, Map<String, String> headers) {
		if (instances.get(uri) == null) {
			instances.put(uri, new RequestTrackerImpl(uri, client, headers));
		}
		return instances.get(uri);
	}

	@Override
	public URI getReferences() throws URISyntaxException {
		JsonArray references = tracker.getJsonArray(IRequestTracker.TRACKER_REFERENCE_URI);
		for (int i = 0; i < references.size(); i++) {
			JsonObject reference = references.getJsonObject(i);
			String type = reference.getString("type");
			if (type != null && "uri".equals(type)) {
				String uri = reference.getString("value");
				return new URI(uri);
			}
		}
		return null;
	}

	private RequestTrackerImpl(String uri, OslcClient client, Map<String, String> headers) {
		// first of all init the tracker
		this.client = client;
		this.headers = headers;
		this.trackerUri = uri;
	}

	private synchronized JsonObject getTrackerResult() {
		jsonResultObject = JsonUtils.getTrackerState(this.trackerUri, this.client, this.headers);
		return jsonResultObject.getJsonObject(this.trackerUri);
	}

}
