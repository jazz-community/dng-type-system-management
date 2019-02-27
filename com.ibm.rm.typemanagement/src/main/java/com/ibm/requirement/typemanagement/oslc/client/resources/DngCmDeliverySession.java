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
import org.eclipse.lyo.client.oslc.OslcClient;
import org.eclipse.lyo.client.oslc.jazz.JazzFormAuthClient;
import org.eclipse.lyo.oslc4j.core.annotation.OslcDescription;
import org.eclipse.lyo.oslc4j.core.annotation.OslcName;
import org.eclipse.lyo.oslc4j.core.annotation.OslcNamespace;
import org.eclipse.lyo.oslc4j.core.annotation.OslcOccurs;
import org.eclipse.lyo.oslc4j.core.annotation.OslcPropertyDefinition;
import org.eclipse.lyo.oslc4j.core.annotation.OslcRange;
import org.eclipse.lyo.oslc4j.core.annotation.OslcReadOnly;
import org.eclipse.lyo.oslc4j.core.annotation.OslcResourceShape;
import org.eclipse.lyo.oslc4j.core.annotation.OslcTitle;
import org.eclipse.lyo.oslc4j.core.annotation.OslcValueType;
import org.eclipse.lyo.oslc4j.core.model.AbstractResource;
import org.eclipse.lyo.oslc4j.core.model.CreationFactory;
import org.eclipse.lyo.oslc4j.core.model.Occurs;
import org.eclipse.lyo.oslc4j.core.model.OslcConstants;
import org.eclipse.lyo.oslc4j.core.model.OslcMediaType;
import org.eclipse.lyo.oslc4j.core.model.ValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.requirement.typemanagement.oslc.client.automation.DngTypeSystemManagementConstants;
import com.ibm.requirement.typemanagement.oslc.client.automation.util.DngHeaderRequestInterceptor;
import com.ibm.requirement.typemanagement.oslc.client.automation.util.LoggingUtil;
import com.ibm.requirement.typemanagement.oslc.client.automation.util.RDFUtils;
import com.ibm.requirement.typemanagement.oslc.client.automation.util.TimeStampUtil;
import com.ibm.requirement.typemanagement.oslc.client.tracking.IRequestTracker;
import com.ibm.requirement.typemanagement.oslc.client.tracking.RequestTrackerImpl;

import net.oauth.OAuthException;

//@formatter:off
//<?xml version="1.0" encoding="UTF-8"?>
//<rdf:RDF 
//    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" 
//    xmlns:oslc="http://open-services.net/ns/core#" 
//    xmlns:types="http://www.ibm.com/xmlns/rdm/types/" 
//    xmlns:dng_config="http://jazz.net/ns/rm/dng/config#">
//    <dng_config:DeliverySession>
//       <types:source rdf:resource="https://clm.example.com:9443/rm/cm/stream/_lLfcILpEEei0JtWD8O6Peg"/>
//       <types:target rdf:resource="https://clm.example.com:9443/rm/cm/stream/_92SXYLpDEei0JtWD8O6Peg"/>
//       <oslc:serviceProvider rdf:resource="https://clm.example.com:9443/rm/oslc_rm/_acxNwIpvEeikoPPCmErwBQ/services.xml"/>
//    </dng_config:DeliverySession>
//</rdf:RDF> 
//@formatter:on

/**
 * Delivery Session.
 * 
 * API: @see https://jazz.net/wiki/bin/view/Main/DNGConfigManagement
 * 
 * API does not provide a type therefore the marshalling request.getEntity()
 * does not work.
 *
 */

@OslcNamespace(DngTypeSystemManagementConstants.DMG_CONFIGURATION_MANAGEMENT_NAMESPACE)
@OslcResourceShape(title = "Delivery Session Shape", describes = DngTypeSystemManagementConstants.DMG_CONFIGURATION_MANAGEMENT_NAMESPACE
		+ DngTypeSystemManagementConstants.DELIVERY_SESSION_TYPE)
public class DngCmDeliverySession extends AbstractResource {

	public static final Logger logger = LoggerFactory.getLogger(DngCmDeliverySession.class);

	private String title;
	private URI deliverySessionState;
	private URI policy;
	private URI source;
	private URI target;
	private URI serviceProvider;

	private DngCmDeliverySession() {
		super();
	}

	private DngCmDeliverySession(URI about) {
		super(about);
	}

	/**
	 * Implements the delivery operation.
	 * 
	 * @param client
	 * @param projectAreaServiceProviderUrl
	 * @param source
	 * @param target
	 * @return
	 * @throws ResourceNotFoundException
	 * @throws IOException
	 * @throws OAuthException
	 * @throws URISyntaxException
	 */
	public static boolean performDelivery(final JazzFormAuthClient client, final String projectAreaServiceProviderUrl,
			final Configuration source, final Configuration target)
			throws ResourceNotFoundException, IOException, OAuthException, URISyntaxException {
		Boolean result = false;
		if (source == null) {
			logger.info("Delivery Source must not be null");
			return result;
		}
		if (target == null) {
			logger.info("Delivery target must not be null");
			return result;
		}
		if (projectAreaServiceProviderUrl == null) {
			logger.info("Project area service provider must not be null");
			return result;
		}

		DngCmDeliverySession deliverySession = createDeliverySession(client, projectAreaServiceProviderUrl, source,
				target);
		if (null == deliverySession) {
			logger.debug("Failed creating a delivery session");
			return result;
		}
		deliverySession = deliverySession.getDeliverySession(client);
		String deliverySessionState = deliverySession.getDeliverySessionState().toString();
		if (deliverySessionState == null) {
			logger.debug("Failed getting delivery session state");
			logger.info("Delivery Session from '" + source.getTitle() + "' to the configuration '" + target.getTitle()
					+ "' failed.");
			deliverySession.deleteDeliverySession(client);
			return result;
		}
		logger.debug("Delivery Session is in state '{}'.", deliverySessionState);
		if (deliverySessionState
				.equals(DngTypeSystemManagementConstants.DMG_CONFIGURATION_MANAGEMENT_NAMESPACE + "delivered")) {
			logger.info("Delivery Session from '" + source.getTitle() + "' to the configuration '" + target.getTitle()
					+ "' succeeded.");
			return result;
		}
		if (deliverySessionState
				.equals(DngTypeSystemManagementConstants.DMG_CONFIGURATION_MANAGEMENT_NAMESPACE + "initialised")
				|| deliverySessionState
						.equals(DngTypeSystemManagementConstants.DMG_CONFIGURATION_MANAGEMENT_NAMESPACE + "scoped")) {
			// Deliver the session
			String tracking = deliverySession.commitDeliverySession(client);
			if (tracking == null) {
				logger.error("Delivery Session from '" + source.getTitle() + "' to the configuration '"
						+ target.getTitle() + "' failed.");
				deliverySession.deleteDeliverySession(client);
				return result;
			}
			result = trackDelivery(client, tracking);
			if (!result) {
				logger.error("Delivery Session from '" + source.getTitle() + "' to the configuration '"
						+ target.getTitle() + "' failed.");
				deliverySession.deleteDeliverySession(client);
				deliverySession.getDeliverySession(client);
			} else {
				logger.trace("Delivery Session from '" + source.getTitle() + "' to the configuration '"
						+ target.getTitle() + "' succeeded.");
			}
		} else {
			logger.debug("Unexpected Delivery Session state '{}'.", deliverySessionState);
		}
		return result;
	}

	/**
	 * Create a delivery session using the factory. HTTP POST on the factory
	 * 
	 * @param client
	 * @param projectAreaServiceProviderUrl
	 * @param source
	 * @param target
	 * @return
	 * @throws ResourceNotFoundException
	 * @throws IOException
	 * @throws OAuthException
	 * @throws URISyntaxException
	 */
	private static DngCmDeliverySession createDeliverySession(final JazzFormAuthClient client,
			final String projectAreaServiceProviderUrl, final Configuration source, final Configuration target)
			throws ResourceNotFoundException, IOException, OAuthException, URISyntaxException {

		if (source == null) {
			logger.info("Delivery Source must not be null");
			return null;
		}
		if (target == null) {
			logger.info("Delivery target must not be null");
			return null;
		}
		DngCmDeliverySession deliverySession = new DngCmDeliverySession();
		deliverySession.setTitle("Type System Delivery Session " + TimeStampUtil.getTimestamp());
		deliverySession.setServiceProvider(target.getServiceProvider());
		deliverySession.setSource(source.getAbout());
		deliverySession.setTarget(target.getAbout());

		logger.debug("CreateDeliverySession");
		CreationFactory deliverySessionFactory = getDeliverySessionFactory(client, projectAreaServiceProviderUrl);
		logger.debug("CreateDeliverySession using factory '{}'", deliverySessionFactory.getCreation().toString());

		HashMap<String, String> header = new HashMap<String, String>();
		header.put(DngTypeSystemManagementConstants.DNG_CM_CONFIGURATION_CONTEXT_HEADER, source.getAbout().toString());

		DngHeaderRequestInterceptor.installRequestInterceptor(client, header);

		ClientResponse response = null;
		String sessionUrl = null;
		try {
			response = client.createResource(deliverySessionFactory.getCreation().toString(), deliverySession,
					OslcMediaType.APPLICATION_RDF_XML, OSLCConstants.CT_RDF);
			DngHeaderRequestInterceptor.removeRequestInterceptor(client);

			logger.debug("Status: " + response.getStatusCode());

			/**
			 * 
			 * Behavior of POST
			 * 
			 * A POST request must contain -
			 * 
			 * a valid Source Configuration resource uri: this must be of a Baseline, a
			 * Stream or a ChangeSet. a valid Target Configuration resource uri: this must
			 * be of a Stream.
			 * 
			 * A POST request may contain -
			 * 
			 * a title.
			 * 
			 * The server may respond with an error code, or, 201 CREATED.
			 * 
			 * 201 CREATED: The request was completed. The Location response header contains
			 * the uri of the newly-created delivery session resource.
			 * 
			 * 409: The source or target configuration was not found in the repository.
			 * 
			 * 400: The request was not completed. This can happen when the request is
			 * invalid for a reason other than above. The body of the response may include a
			 * description of any failures.
			 * 
			 * 
			 * 
			 */
			switch (response.getStatusCode()) {
			case 201:
				sessionUrl = response.getHeaders().getFirst(HttpHeaders.LOCATION);
				logger.debug("The delivery session url for delivery session is '{}'.", sessionUrl);
				if (sessionUrl != null) {
					deliverySession.setAbout(new URI(sessionUrl));
				}
				return deliverySession;
			case 409:
				logger.debug("The source or target configuration was not found in the repository.");
				break;
			case 400:
				logger.debug("The request was not completed.");
				break;
			default:
				logger.debug("Unexpected return code.");
				break;
			}
		} finally {
			if (response != null) {
				response.consumeContent();
			}
		}
		return null;
	}

	/**
	 * Get the delivery session factory from the service provider
	 * 
	 * @param client
	 * @param serviceProviderUrl
	 * @return
	 * @throws IOException
	 * @throws OAuthException
	 * @throws URISyntaxException
	 * @throws ResourceNotFoundException
	 */
	private static CreationFactory getDeliverySessionFactory(final JazzFormAuthClient client,
			final String serviceProviderUrl)
			throws IOException, OAuthException, URISyntaxException, ResourceNotFoundException {
		CreationFactory deliverySessionFactory = client.lookupCreationFactoryResource(serviceProviderUrl,
				OSLCConstants.OSLC_RM_V2, DngTypeSystemManagementConstants.DMG_CONFIGURATION_MANAGEMENT_NAMESPACE
						+ DngTypeSystemManagementConstants.DELIVERY_SESSION_TYPE,
				null);
		return deliverySessionFactory;
	}

	/**
	 * Read/Get the delivery session e.g. to get the state. HTTP GET
	 * 
	 * @param client
	 * @param deliverySession
	 * @return
	 * @throws IOException
	 * @throws OAuthException
	 * @throws URISyntaxException
	 */
	public DngCmDeliverySession getDeliverySession(final JazzFormAuthClient client)
			throws IOException, OAuthException, URISyntaxException {

		String tracker = null;
		String sessionURL = this.getAbout().toString();
		HashMap<String, String> header = new HashMap<String, String>();
		header.put(DngTypeSystemManagementConstants.DNG_CM_CONFIGURATION_CONTEXT_HEADER, this.getSource().toString());
		header.put(OSLCConstants.OSLC_CORE_VERSION, DngTypeSystemManagementConstants.OSLC_VERSION_2_HEADER_VALUE);
		header.put(DngTypeSystemManagementConstants.ACCEPT_HEADER, OslcMediaType.APPLICATION_RDF_XML);
		logger.debug("Get Status session '{}'.", sessionURL);
		ClientResponse response = null;
		try {
			response = client.getResource(sessionURL, header);
			logger.debug("Status: " + response.getStatusCode());

			/**
			 * 6.1 Behavior of GET
			 * 
			 * The server will respond with an error code, or, 200 OK.
			 * 
			 * 200 OK: The request was completed. The representation in the body will
			 * indicate the state of the session.
			 * 
			 * 202: The delivery session is being processed. The Location header of the
			 * response will contain a uri which will report the progress of this
			 * processing: this uri can be polled every few seconds.
			 * 
			 * 404: The delivery session was not found in the repository.
			 * 
			 * 410: The delivery session has been deleted from the repository.
			 * 
			 * 400: The request was not completed. This can happen when the request fails in
			 * such a way that the session cannot be recovered for reasons other than above.
			 * The body of the response may include a description of any failures.
			 * 
			 */
			switch (response.getStatusCode()) {
			case 200:
				logger.debug("The delivery '{}' was completed. Check the state.", sessionURL);
				tracker = response.getHeaders().getFirst(HttpHeaders.LOCATION);
				logger.debug("The delivery session state is tacked by state. Tracker url is '{}'.", tracker);
				String sessionState = RDFUtils.getStatementForProperty(response,
						DngTypeSystemManagementConstants.DMG_CONFIGURATION_MANAGEMENT_NAMESPACE,
						DngTypeSystemManagementConstants.DELIVERY_SESSION_STATE);
				if (sessionState != null) {
					this.setDeliverySessionState(new URI(sessionState));
				}
				break;
			case 202:
				logger.debug("The delivery session '{}' is being processed. Check the tracker.", sessionURL);
				tracker = response.getHeaders().getFirst(HttpHeaders.LOCATION);
				logger.debug("The delivery session state is tacked by tracker url '{}'.", tracker);
				break;
			case 404:
				logger.debug("The delivery session '{}' was not found in the repository.", sessionURL);
				break;
			case 410:
				logger.debug("The delivery session '{}' has been deleted from the repository. ", sessionURL);
				break;
			case 400:
				logger.debug("The delivery '{}' session is not available.", sessionURL);
				break;
			default:
				logger.debug("Unexpected return code. Session '{}'.", sessionURL);
				break;
			}
		} finally {
			if (response != null) {
				response.consumeContent();
			}
		}
		if (tracker == null) {
			return null;
		}
		return this;
	}

	/**
	 * Update a delivery session resource HTTP PUT
	 * 
	 * @param client
	 * @param deliverySession
	 * @return
	 * @throws IOException
	 * @throws OAuthException
	 * @throws URISyntaxException
	 */
	public String updateDeliverySession(final JazzFormAuthClient client)
			throws IOException, OAuthException, URISyntaxException {

		String tracker = null;
		String sessionURI = this.getAbout().toString();

		// LoggingUtil.setLoggingLevel(LoggingUtil.INFO);
		logger.debug("Update session: " + sessionURI);

		ClientResponse response = null;
		try {
			response = client.updateResource(sessionURI, this, OslcMediaType.APPLICATION_RDF_XML);

			logger.debug("Status: " + response.getStatusCode());

			/**
			 * 6.2 Behavior of PUT
			 * 
			 * A PUT which attempts to make an invalid state transition will respond with
			 * 409 Conflict.
			 * 
			 * Changing the delivery state of a session to dng_config:delivered will cause
			 * the server to begin processing the delivery of changes from the source to the
			 * target. This may take some time.
			 *
			 * If that delivery session is already in the process of being delivered, the
			 * response to the PUT will be 409 Conflict.
			 * 
			 * The server may respond with an error code, or, 200 OK, or it may respond with
			 * a 202 Accepted, indicating that the delivery operation may take some time and
			 * is being processed in the background.
			 *
			 * In case of 202 response, the server will continue processing the delivery
			 * asynchronously. Clients can poll the returned resource uri until such times
			 * as the server responds with a message indicating that the processing is
			 * complete.
			 * 
			 * 200 OK: The delivery was completed. The representation in the body will
			 * indicate the state of the session. In case of a successful delivery, the
			 * state will be dng_config:delivered; in case of an unsuccessful delivery, it
			 * will be in some other state.
			 * 
			 * 202: The delivery session is being processed. The Location header of the
			 * response will contain a uri which will report the progress of this
			 * processing: this uri can be polled every few seconds.
			 * 
			 * 404: The delivery session was not found in the repository.
			 * 
			 * 409: The delivery session is already in the process of being delivered.
			 * 
			 * 410: The delivery session has been deleted from the repository.
			 * 
			 * 400: The delivery session is not available. This can happen when the delivery
			 * fails in such a way that the session cannot be updated. The body of the
			 * response may include a description of any delivery failures.
			 * 
			 */
			switch (response.getStatusCode()) {
			case 200:
				logger.info("The delivery '{}' was completed. Check the state.", sessionURI);
				tracker = response.getHeaders().getFirst(HttpHeaders.LOCATION);
				logger.info("The delivery session state is tacked by state. Tracker url is '{}'.", tracker);
				String sessionState = RDFUtils.getStatementForProperty(response,
						DngTypeSystemManagementConstants.DMG_CONFIGURATION_MANAGEMENT_NAMESPACE,
						DngTypeSystemManagementConstants.DELIVERY_SESSION_STATE);
				if (sessionState != null) {
					this.setDeliverySessionState(new URI(sessionState));
				}
				break;
			case 202:
				logger.debug("The delivery session '{}' is being processed. Check the tracker.", sessionURI);
				tracker = response.getHeaders().getFirst(HttpHeaders.LOCATION);
				logger.debug("The delivery session state is tacked by tracker url '{}'.", tracker);
				break;
			case 404:
				logger.debug("The delivery session '{}' was not found in the repository.", sessionURI);
				break;
			case 409:
				logger.debug("The delivery session '{}' is already in the process of being delivered.", sessionURI);
				break;
			case 410:
				logger.debug("The delivery session '{}' has been deleted from the repository. ", sessionURI);
				break;
			case 400:
				logger.debug("The delivery '{}' session is not available.", sessionURI);
				break;
			default:
				logger.debug("Unexpected return code. Session '{}'.", sessionURI);
				break;
			}
		} finally {
			if (response != null) {
				response.consumeContent();
			}
		}
		return tracker;
	}

	/**
	 * @param client
	 * @param deliverySession
	 * @return
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws OAuthException
	 */
	public String commitDeliverySession(final JazzFormAuthClient client)
			throws URISyntaxException, IOException, OAuthException {

		this.setDeliverySessionState(new URI("http://jazz.net/ns/rm/dng/config#delivered"));
		logger.debug("Setting Delivery Session state to '{}'.", this.getDeliverySessionState().toString());
		String tracking = this.updateDeliverySession(client);
		return tracking;
	}

	/**
	 * @param client
	 * @param trackerUrl
	 * @throws URISyntaxException
	 */
	private static boolean trackDelivery(final OslcClient client, final String trackerUrl) throws URISyntaxException {
		boolean result = true;
		Map<String, String> targetJsonHeader = new HashMap<String, String>();
		targetJsonHeader.put(DngTypeSystemManagementConstants.ACCEPT_HEADER,
				DngTypeSystemManagementConstants.CONTENT_TYPE_APPLICATION_JSON);
		targetJsonHeader.put(DngTypeSystemManagementConstants.CONTENT_TYPE_HEADER,
				DngTypeSystemManagementConstants.CONTENT_TYPE_APPLICATION_JSON);

		IRequestTracker tracker = RequestTrackerImpl.getInstance(trackerUrl, client, targetJsonHeader);
		URI trackerComplete = new URI(IRequestTracker.TRACKER_STATE_COMPLETE);
		URI trackerVerdictError = new URI(IRequestTracker.TRACKER_VERDICT_ERROR);
		URI trackerVerdictFailed = new URI(IRequestTracker.TRACKER_VERDICT_FAILED);

		URI trackerState = tracker.getState();

		while (!trackerState.equals(trackerComplete)) {
			logger.trace("Tracker state : '{}'", trackerState.toString());
			trackerState = tracker.getState();
		}

		URI trackerVerdict = tracker.getVerdict();
		logger.trace("Tracker verdict : '{}'", trackerVerdict.toString());
		if (trackerVerdict.equals(trackerVerdictError)) {
			result = false;
		}
		if (trackerVerdict.equals(trackerVerdictFailed)) {
			result = false;
		}
		if (result == true) {
			logger.trace("The configuration has been updated successfully!");
		} else {
			logger.error("The configuration update has failed or there were no differences to deliver!");
			logger.error("The tracker reports an error '{}'", tracker.getMessage());
		}
		return result;
	}

	/**
	 * Update a delivery session resource HTTP delete
	 * 
	 * @param response
	 * @param deliverySession
	 * @return
	 * @throws URISyntaxException
	 * @throws OAuthException
	 * @throws IOException
	 */
	public boolean deleteDeliverySession(final JazzFormAuthClient client)
			throws URISyntaxException, IOException, OAuthException {
		Boolean result = false;

		String sessionURL = this.getAbout().toString();
		HashMap<String, String> header = new HashMap<String, String>();
		// header.put("Configuration-Context", source.getAbout().toString());
		header.put(OSLCConstants.OSLC_CORE_VERSION, DngTypeSystemManagementConstants.OSLC_VERSION_2_HEADER_VALUE);
		header.put(DngTypeSystemManagementConstants.ACCEPT_HEADER, OslcMediaType.APPLICATION_RDF_XML);
//		header.put("DoorsRP-Request-Type", "public 2.0");

		LoggingUtil.setLoggingLevel(LoggingUtil.INFO);
		logger.debug("Delete session: " + sessionURL);

		ClientResponse response = null;
		try {
			response = client.deleteResource(sessionURL);
			logger.debug("Status: " + response.getStatusCode());

			/**
			 * 6.3 Behavior of DELETE
			 * 
			 * The server will respond with one of the following codes:
			 * 
			 * 200 OK: The request was completed.
			 * 
			 * 202: The delivery session is already in the process of being delivered. The
			 * Location header of the response will contain a uri which will report the
			 * progress of this processing: this uri can be polled every few seconds.
			 * 
			 * 404: The delivery session was not found in the repository.
			 * 
			 * 409: The delivery session is already in the process of being delivered.
			 * 
			 * 410: The delivery session has been deleted from the repository.
			 * 
			 * 400: The request was not completed. This can happen when the request fails in
			 * such a way that the session cannot be deleted. The body of the response may
			 * include a description of any failures.
			 */
			switch (response.getStatusCode()) {
			case 200:
				logger.debug("Deleted.... '{}'.", sessionURL);
				result = true;
				break;
			case 202:
				logger.debug(
						"The delivery session is already in the process of being delivered. Session '{}' not deleted.",
						sessionURL);
				result = false;
				break;
			case 404:
				logger.debug("The delivery session was not found in the repository. Session '{}' not deleted.",
						sessionURL);
				result = false;
				break;
			case 409:
				logger.debug(
						"The delivery session is already in the process of being delivered. Session '{}' not deleted.",
						sessionURL);
				result = false;
				break;
			case 410:
				logger.debug("The delivery session '{}' has been deleted from the repository. ", sessionURL);
				result = false;
				break;
			case 400:
				logger.debug("The request was not completed. Session '{}' not deleted.", sessionURL);
				result = false;
				break;
			default:
				logger.debug("Unexpected return code. Session '{}'.", sessionURL);
				result = false;
				break;
			}
		} finally {
			if (response != null) {
				response.consumeContent();
			}
		}
		return result;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setDeliverySessionState(String deliverySessionState) throws URISyntaxException {
		this.deliverySessionState = new URI(deliverySessionState);
	}

	public void setDeliverySessionState(URI deliverySessionState) {
		this.deliverySessionState = deliverySessionState;
	}

	public void setPolicy(URI policy) {
		this.policy = policy;
	}

	public void setSource(URI source) {
		this.source = source;
	}

	public void setTarget(URI target) {
		this.target = target;
	}

	public void setServiceProvider(URI serviceProvider) {
		this.serviceProvider = serviceProvider;
	}

	@OslcDescription("Title (reference: Dublin Core) of the resource represented as plain text.")
	@OslcOccurs(Occurs.ZeroOrOne)
	@OslcReadOnly(false)
	@OslcPropertyDefinition(OslcConstants.DCTERMS_NAMESPACE + "title")
	@OslcTitle("Title")
	@OslcValueType(ValueType.XMLLiteral)
	public String getTitle() {
		return title;
	}

	@OslcDescription("State of the delivery session.")
	@OslcOccurs(Occurs.ExactlyOne)
	@OslcReadOnly(false)
	@OslcName(DngTypeSystemManagementConstants.DELIVERY_SESSION_STATE)
	@OslcPropertyDefinition(DngTypeSystemManagementConstants.DMG_CONFIGURATION_MANAGEMENT_NAMESPACE
			+ DngTypeSystemManagementConstants.DELIVERY_SESSION_STATE)
	@OslcTitle("Delivery Session State")
	public URI getDeliverySessionState() {
		return deliverySessionState;
	}

	@OslcDescription("Policy rules governing the delivery of a Session.")
	@OslcOccurs(Occurs.ZeroOrOne)
	@OslcReadOnly(false)
	@OslcName("policy")
	@OslcPropertyDefinition(DngTypeSystemManagementConstants.DMG_CONFIGURATION_MANAGEMENT_NAMESPACE + "policy")
	@OslcTitle("Policy")
	public URI getPolicy() {
		return policy;
	}

	@OslcDescription("URI to the source configuration")
	@OslcOccurs(Occurs.ExactlyOne)
	@OslcName("source")
	@OslcPropertyDefinition(DngTypeSystemManagementConstants.DMG_CONFIGURATION_MANAGEMENT_NAMESPACE + "source")
	@OslcTitle("Source Configuration")
	public URI getSource() {
		return source;
	}

	@OslcDescription("URI to the target configuration")
	@OslcName("target")
	@OslcPropertyDefinition(DngTypeSystemManagementConstants.DMG_CONFIGURATION_MANAGEMENT_NAMESPACE + "target")
	@OslcTitle("Target Configuration")
	public URI getTarget() {
		return target;
	}

	@OslcDescription("A link to the resource's OSLC Service Provider.")
	@OslcPropertyDefinition(OslcConstants.OSLC_CORE_NAMESPACE + "serviceProvider")
	@OslcRange(OslcConstants.TYPE_SERVICE_PROVIDER)
	@OslcReadOnly(true)
	@OslcTitle("Service Provider")
	public URI getServiceProvider() {
		return serviceProvider;
	}

}
