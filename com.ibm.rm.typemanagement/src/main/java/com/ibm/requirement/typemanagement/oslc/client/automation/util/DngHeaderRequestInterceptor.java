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
 *    Ralph Schoon - Initial implementation
 *******************************************************************************/
package com.ibm.requirement.typemanagement.oslc.client.automation.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;
import org.eclipse.lyo.client.oslc.OslcClient;

/**
 * A custom RequestHeaderInterceptor that allows to add and remove headers from
 * a request. It adds the usually required DngApi Headers, in case they are
 * missing.
 *
 */
@SuppressWarnings("deprecation")
public class DngHeaderRequestInterceptor implements HttpRequestInterceptor {

	HashMap<String, String> addHeaders = null;
	HashMap<String, String> removeHeaders = null;

	/**
	 * Constructor
	 * 
	 * @param addHeaders    the headers to be added or null
	 * @param removeHeaders the headers to be removed or null
	 */
	private DngHeaderRequestInterceptor(HashMap<String, String> addHeaders, HashMap<String, String> removeHeaders) {
		this.addHeaders = addHeaders;
		this.removeHeaders = removeHeaders;
	}

	@Override
	public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
		// Remove request headers.
		if (removeHeaders != null) {
			for (Map.Entry<String, String> entry : removeHeaders.entrySet()) {
				// Remove the header.
				request.removeHeaders(entry.getKey());
			}
		}
		// Add request headers.
		if (addHeaders != null) {
			for (Map.Entry<String, String> entry : addHeaders.entrySet()) {
				// Add the header. Remove the header first, in case it exists.
				request.removeHeaders(entry.getKey());
				request.addHeader(entry.getKey(), entry.getValue());
			}
		}
	}

	/**
	 * Install the request Interceptor. This method hides the deprecation of the
	 * HTTP Client.
	 * 
	 * @param client        the OSLC Client
	 * @param addHeaders    the headers to be added or null
	 * @param removeHeaders the headers to be removed or null
	 */
	public static void installRequestInterceptor(OslcClient client, HashMap<String, String> addHeaders,
			HashMap<String, String> removeHeaders) {
		HttpRequestInterceptor requestHeaderFixer = new DngHeaderRequestInterceptor(addHeaders, removeHeaders);
		((DefaultHttpClient) client.getHttpClient()).addRequestInterceptor(requestHeaderFixer);

	}

	/**
	 * Install the request Interceptor. This method hides the deprecation of the
	 * HTTP Client.
	 * 
	 * @param client     the OSLC Client
	 * @param addHeaders the headers to be added or null
	 */
	public static void installRequestInterceptor(OslcClient client, HashMap<String, String> addHeaders) {
		installRequestInterceptor(client, addHeaders, null);
	}

	/**
	 * Removes the request interceptor This should be done after each call, in order
	 * to make sure the interseptor is not active where it should not.
	 * 
	 * @param client
	 */
	public static void removeRequestInterceptor(OslcClient client) {
		((DefaultHttpClient) client.getHttpClient()).removeRequestInterceptorByClass(DngHeaderRequestInterceptor.class);
	}

	/**
	 * Remove the request Interceptor.
	 * 
	 * @param client     the OSLC Client
	 * @param addHeaders the headers to be added or null
	 */
	public static void clearHeaders(OslcClient client) {
		installRequestInterceptor(client, null, null);
	}
}
