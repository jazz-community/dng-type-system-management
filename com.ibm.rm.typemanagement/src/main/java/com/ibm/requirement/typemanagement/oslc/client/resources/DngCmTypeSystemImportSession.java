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
package com.ibm.requirement.typemanagement.oslc.client.resources;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpHeaders;
import org.apache.wink.client.ClientResponse;
import org.eclipse.lyo.client.exception.ResourceNotFoundException;
import org.eclipse.lyo.client.oslc.OSLCConstants;
import org.eclipse.lyo.client.oslc.jazz.JazzFormAuthClient;
import org.eclipse.lyo.oslc4j.core.annotation.OslcDescription;
import org.eclipse.lyo.oslc4j.core.annotation.OslcName;
import org.eclipse.lyo.oslc4j.core.annotation.OslcNamespace;
import org.eclipse.lyo.oslc4j.core.annotation.OslcPropertyDefinition;
import org.eclipse.lyo.oslc4j.core.annotation.OslcRange;
import org.eclipse.lyo.oslc4j.core.annotation.OslcReadOnly;
import org.eclipse.lyo.oslc4j.core.annotation.OslcResourceShape;
import org.eclipse.lyo.oslc4j.core.annotation.OslcTitle;
import org.eclipse.lyo.oslc4j.core.model.AbstractResource;
import org.eclipse.lyo.oslc4j.core.model.OslcConstants;
import org.eclipse.lyo.oslc4j.core.model.OslcMediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.requirement.typemanagement.oslc.client.automation.DngTypeSystemManagementConstants;
import com.ibm.requirement.typemanagement.oslc.client.dngcm.DngHeaderRequestInterceptor;
import com.ibm.requirement.typemanagement.oslc.client.tracking.IRequestTracker;
import com.ibm.requirement.typemanagement.oslc.client.tracking.RequestTrackerImpl;

import net.oauth.OAuthException;

/**
 * Represents a DNG Type Import session. Implements the import.
 * 
 * API: @see https://jazz.net/wiki/bin/view/Main/DNGTypeImport
 *
 */

@OslcNamespace(DngTypeSystemManagementConstants.DNG_TYPES_NAMESPACE)
@OslcResourceShape(title = "Type Import Session Shape", describes = DngTypeSystemManagementConstants.DNG_TYPES_NAMESPACE
		+ DngTypeSystemManagementConstants.TYPE_IMPORT_SESSION_TYPE)
public class DngCmTypeSystemImportSession extends AbstractResource {

	public static final Logger logger = LoggerFactory.getLogger(DngCmTypeSystemImportSession.class);

	private URI source;
	private URI target;

	private URI serviceProvider;

	public DngCmTypeSystemImportSession() {
		super();
	}

	public DngCmTypeSystemImportSession(URI about) {
		super(about);
	}

	/**
	 * Perform the type import. Main entry point into this functionality.
	 * 
	 * @param client
	 * @param source
	 * @param target
	 * @return
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws OAuthException
	 * @throws ResourceNotFoundException
	 */
	public static boolean performTypeImport(final JazzFormAuthClient client, final Configuration source,
			final Configuration target)
			throws URISyntaxException, IOException, OAuthException, ResourceNotFoundException {
		boolean result = false;

		logger.trace("TypeImport");
		if (target == null) {
			logger.info("Target must not be null");
			return result;
		}
		if (source == null) {
			logger.info("Source must not be null");
			return result;
		}
		
		DngCmTypeSystemImportSession typeImportSession = new DngCmTypeSystemImportSession();
		typeImportSession.setSource(source.getAbout());
		typeImportSession.setTarget(target.getAbout());
		typeImportSession.setServiceProvider(target.getServiceProvider());

		String typeImportCreationFactory = client.lookupCreationFactory(target.getServiceProvider().toString(),
				OSLCConstants.OSLC_RM_V2, DngTypeSystemManagementConstants.DNG_TYPES_NAMESPACE
						+ DngTypeSystemManagementConstants.TYPE_IMPORT_SESSION_TYPE);

		HashMap<String, String> header = new HashMap<String, String>();
		header.put(DngTypeSystemManagementConstants.DNG_CM_CONFIGURATION_CONTEXT_HEADER, target.getAbout().toString());

		HashMap<String, String> removeHeader = new HashMap<String, String>();
		removeHeader.put(OSLCConstants.OSLC_CORE_VERSION, "");

		DngHeaderRequestInterceptor.installRequestInterceptor(client, header, removeHeader);

		ClientResponse response = null;
		try {

			response = client.createResource(typeImportCreationFactory, typeImportSession,
					OslcMediaType.APPLICATION_RDF_XML, OSLCConstants.CT_RDF);
			logger.debug("Status: " + response.getStatusCode());
			DngHeaderRequestInterceptor.removeRequestInterceptor(client);

			if (response.getStatusCode() == 202) {

				// Get the location for the tracker
				String trackerUrl = response.getHeaders().getFirst(HttpHeaders.LOCATION);
				logger.trace("This is the tracker url for type import session is '{}'.", trackerUrl);
				Map<String, String> targetJsonHeader = new HashMap<String, String>();
				targetJsonHeader.put(DngTypeSystemManagementConstants.ACCEPT_HEADER, OSLCConstants.CT_JSON);
				targetJsonHeader.put(DngTypeSystemManagementConstants.CONTENT_TYPE_HEADER, OSLCConstants.CT_JSON);

				IRequestTracker tracker = RequestTrackerImpl.getInstance(trackerUrl, client, targetJsonHeader);
				URI trackerComplete = new URI(IRequestTracker.TRACKER_STATE_COMPLETE);
				URI trackerVerdictError = new URI(IRequestTracker.TRACKER_VERDICT_ERROR);

				URI trackerState = tracker.getState();

				while (!trackerState.equals(trackerComplete)) {
					logger.trace("Tracker state : '{}'", trackerState.toString());
					trackerState = tracker.getState();
				}

				URI trackerVerdict = tracker.getVerdict();
				logger.trace("Tracker verdict : '{}'", trackerVerdict.toString());
				if (trackerVerdict.equals(trackerVerdictError)) {
					logger.error("The tracker " + trackerUrl + " reports an error '{}'", tracker.getMessage());
					logger.error("Tracker verdict : '{}'", trackerVerdict.toString());
					result = false;
				} else {
					logger.trace("Type System imported successfully into the configuration '{}'!", target.getTitle());
					result = true;
				}
			} else {
				logger.error("Unexpected return code " + response.getStatusCode() + ". Factory '{}'.",
						typeImportCreationFactory);
				result = false;
			}
		} catch (Exception e) {
			logger.error("Exception '{}'.", e.getMessage());
			e.printStackTrace();
			result = false;
		} finally {
			if (response != null) {
				response.consumeContent();
			}

		}
		return result;
	}

	@OslcDescription("URI to the source configuration")
	@OslcName("source")
	@OslcPropertyDefinition(DngTypeSystemManagementConstants.DNG_TYPES_NAMESPACE + "source")
	@OslcReadOnly(false)
	@OslcTitle("Source Configuration")
	public URI getSource() {
		return source;
	}

	@OslcDescription("URI to the target configuration")
	@OslcName("target")
	@OslcPropertyDefinition(DngTypeSystemManagementConstants.DNG_TYPES_NAMESPACE + "target")
	@OslcReadOnly(false)
	@OslcTitle("Target Configuration")
	public URI getTarget() {
		return target;
	}

	@OslcDescription("The scope of a resource is a URI for the resource's OSLC Service Provider.")
	@OslcPropertyDefinition(OslcConstants.OSLC_CORE_NAMESPACE + "serviceProvider")
	@OslcRange(OslcConstants.TYPE_SERVICE_PROVIDER)
	@OslcTitle("Service Provider")
	public URI getServiceProvider() {
		return serviceProvider;
	}

	public void setTarget(final URI target) {
		this.target = target;
	}

	public void setSource(final URI source) {
		this.source = source;
	}

	public void setServiceProvider(final URI serviceProvider) {
		this.serviceProvider = serviceProvider;
	}

}
