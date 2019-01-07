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

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;

import org.apache.wink.client.ClientResponse;
import org.eclipse.lyo.client.oslc.OslcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.oauth.OAuthException;

public class JsonUtils {

	private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);

	public static String[] toStringArray(JsonArray array) {
		if (array == null)
			return null;

		String[] arr = new String[array.size()];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = array.getString(i);
		}
		return arr;
	}

	public static synchronized String getTrackerState(String uri, String requestUri, OslcClient client,
			Map<String, String> requestHeaders) throws IOException, OAuthException, URISyntaxException {

		ClientResponse response = null;
		try {
			response = client.getResource(uri, requestHeaders);
			InputStream input = response.getEntity(InputStream.class);

			// Create JsonReader object
			JsonReaderFactory factory = Json.createReaderFactory(null);
			JsonReader jsonReader = factory.createReader(input);

			JsonObject jObj = jsonReader.readObject();
			logger.info(jObj.toString());

			// first let's fetch the verdict of
			JsonObject x = jObj.getJsonObject(requestUri);
			if (x == null) {
				response.consumeContent();
				jsonReader.close();
				logger.info("Tracker in progress.");
				response.consumeContent();
				return "IN PROGRESS";
			}
			JsonArray y = x.getJsonArray("http://open-services.net/ns/core#message");

			String value = y.getJsonObject(0).getString("value");

			response.consumeContent();
			jsonReader.close();

			logger.debug("Tracker finished with status : '{}'", value);

			return value;
		} finally {
			if (response != null) {
				response.consumeContent();
			}
		}
	}

	public static synchronized JsonObject getTrackerState(String uri, OslcClient client,
			Map<String, String> requestHeaders) {

		JsonObject json = null;
		ClientResponse response = null;
		try {
			response = client.getResource(uri, requestHeaders);
			// logger.info(" RawResponse '{}'", RDFUtils.getRawResponse(response));
			InputStream input = response.getEntity(InputStream.class);

			// Create JsonReader object
			JsonReaderFactory factory = Json.createReaderFactory(null);
			JsonReader jsonReader = factory.createReader(input);

			json = jsonReader.readObject();
			jsonReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (response != null) {
				response.consumeContent();
			}
		}
		return json;
	}

}
