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
import org.eclipse.lyo.client.oslc.OSLCConstants;
import org.eclipse.lyo.client.oslc.jazz.JazzFormAuthClient;
import org.eclipse.lyo.oslc4j.core.annotation.OslcDescription;
import org.eclipse.lyo.oslc4j.core.annotation.OslcNamespace;
import org.eclipse.lyo.oslc4j.core.annotation.OslcOccurs;
import org.eclipse.lyo.oslc4j.core.annotation.OslcPropertyDefinition;
import org.eclipse.lyo.oslc4j.core.annotation.OslcResourceShape;
import org.eclipse.lyo.oslc4j.core.annotation.OslcTitle;
import org.eclipse.lyo.oslc4j.core.annotation.OslcValueType;
import org.eclipse.lyo.oslc4j.core.model.AbstractResource;
import org.eclipse.lyo.oslc4j.core.model.Occurs;
import org.eclipse.lyo.oslc4j.core.model.OslcConstants;
import org.eclipse.lyo.oslc4j.core.model.OslcMediaType;
import org.eclipse.lyo.oslc4j.core.model.ValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.requirement.typemanagement.oslc.client.automation.DngTypeSystemManagementConstants;
import com.ibm.requirement.typemanagement.oslc.client.automation.util.TimeStampUtil;
import com.ibm.requirement.typemanagement.oslc.client.dngcm.DngHeaderRequestInterceptor;
import com.ibm.requirement.typemanagement.oslc.client.tracking.IRequestTracker;
import com.ibm.requirement.typemanagement.oslc.client.tracking.RequestTrackerImpl;

import net.oauth.OAuthException;

@OslcNamespace("http://open-services.net/ns/config#")
@OslcResourceShape(title = "Configuration", describes = "http://open-services.net/ns/config#" + "Configuration")
public class Changeset extends AbstractResource {

	private String title;
	private String description;
	private URI component;
	private URI stream;
	private URI changeSetFactory;
	private URI type;

	@OslcDescription("The component this changeset refers to.")
	@OslcPropertyDefinition(DngTypeSystemManagementConstants.CONFIGUTATION_MANAGEMENT_NAMESPACE + "component")
	@OslcOccurs(Occurs.ExactlyOne)
	@OslcTitle("Component")
	public URI getComponent() {
		return component;
	}

	@OslcDescription("The stream this changeset refers to.")
	@OslcPropertyDefinition(DngTypeSystemManagementConstants.CONFIGUTATION_MANAGEMENT_NAMESPACE + "stream")
	@OslcOccurs(Occurs.ExactlyOne)
	@OslcTitle("Stream")
	public URI getStream() {
		return stream;
	}

	@OslcDescription("A name for the changeSet.")
	@OslcPropertyDefinition(OslcConstants.DCTERMS_NAMESPACE + DngTypeSystemManagementConstants.DC_PROPERTY_NAME_TITLE)
	@OslcOccurs(Occurs.ZeroOrOne)
	@OslcTitle("Title")
	@OslcValueType(ValueType.XMLLiteral)
	public String getTitle() {
		return title;
	}

	@OslcDescription("A description for the changeSet.")
	@OslcPropertyDefinition(OslcConstants.DCTERMS_NAMESPACE + "description")
	@OslcOccurs(Occurs.ZeroOrOne)
	@OslcTitle("Description")
	public String getDescription() {
		return description;
	}

	@OslcDescription("Some additional type.")
	@OslcPropertyDefinition(OslcConstants.RDF_NAMESPACE + "type")
	@OslcTitle("Type")
	public URI getType() throws URISyntaxException {
		return type;
	}

	public void setType(URI uri) {
		this.type = uri;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setComponent(URI component) {
		this.component = component;
	}

	public void setStream(URI stream) {
		this.stream = stream;
	}

	/**
	 * @return The URI for the creation factory
	 */
	public URI getChangeSetFactory() {
		return changeSetFactory;
	}

	/**
	 * Set the URI for the creation factory
	 * 
	 * @param changeSetFactory
	 */
	public void setChangeSetFactory(URI changeSetFactory) {
		this.changeSetFactory = changeSetFactory;
	}

	/**
	 * Initialize this object with the required information.
	 * 
	 * @param title
	 * @param description
	 * @param target
	 * @throws URISyntaxException
	 */
	public void initialize(String title, String description, Configuration target) throws URISyntaxException {
		initialize(title, description, target.getComponent(), target.getAbout(), target.getChangesets());
	}

	/**
	 * Initialize this object with the required information.
	 * 
	 * @param title            A title
	 * @param description      A Description
	 * @param component        The component for this stream
	 * @param targetStream     The target Stream for this change set
	 * @param changesetFactory The change set creation factory
	 * @throws URISyntaxException
	 */
	public void initialize(String title, String description, URI component, URI targetStream, URI changesetFactory)
			throws URISyntaxException {
		if (title == null) {
			title = "No Title " + TimeStampUtil.getTimestamp();
		}
		setTitle(title);
		setDescription(description);
		setComponent(component);
		setStream(targetStream);
		setChangeSetFactory(changesetFactory);
		setAbout(null);
		setType(new URI("http://open-services.net/ns/config#Configuration"));
	}

	/**
	 * @param about
	 */
	public Changeset(URI about) {
		super(about);
	}

	/**
	 * 
	 */
	public Changeset() {
		super(null);
	}

	/**
	 * Try to create this change set.
	 * 
	 * @param client
	 * @param target
	 * @return
	 * @throws IOException
	 * @throws OAuthException
	 * @throws URISyntaxException
	 */
	public Changeset(final JazzFormAuthClient client, final Configuration target)
			throws IOException, OAuthException, URISyntaxException {
		super(null);
		create(client, "Import-Type-System-CS-" + TimeStampUtil.getTimestamp(), "Created for automation.", target);
	}

	/**
	 * @param client
	 * @param title
	 * @param description
	 * @param target
	 * @throws IOException
	 * @throws OAuthException
	 * @throws URISyntaxException
	 */
	public Changeset(final JazzFormAuthClient client, final String title, final String description,
			final Configuration target) throws IOException, OAuthException, URISyntaxException {
		super(null);
		create(client, title, description, target);
	}

	public static final Logger logger = LoggerFactory.getLogger(Changeset.class);

	/**
	 * @param client
	 * @param title
	 * @param description
	 * @param target
	 * @return The URI of the newly created change set or null if the change set was
	 *         not created
	 * @throws IOException
	 * @throws OAuthException
	 * @throws URISyntaxException
	 */
	private URI create(final JazzFormAuthClient client, final String title, final String description,
			final Configuration target) throws IOException, OAuthException, URISyntaxException {
		this.initialize(title, description, target);
		URI myURI = this.create(client);
		return myURI;
	}

	/**
	 * @param client
	 * @return The URI of the newly created change set or null if the change set was
	 *         not created
	 * @throws IOException
	 * @throws OAuthException
	 * @throws URISyntaxException
	 */
	private URI create(final JazzFormAuthClient client) throws IOException, OAuthException, URISyntaxException {

		/**
		 * Three steps Create the change set Get the tracker track the result get the
		 * change set URI
		 */

		URI result = null;
		if (getComponent() == null) {
			logger.info("Component must not be null");
			return result;
		}
		if (getStream() == null) {
			logger.info("Stream must not be null");
			return result;
		}
		if (getChangeSetFactory() == null) {
			logger.info("Changeset Factory must not be null");
			return result;
		}

		String trackerURI = createChangeSet(client);
		logger.trace("Tracker: '{}' ", trackerURI);
		URI changeSetURI = trackCreation(client, trackerURI);
		if (null != changeSetURI) {
			this.setAbout(changeSetURI);
			return changeSetURI;
		}
		this.setAbout(null);
		return result;
	}

	/**
	 * Tries to create the change set represented by this object
	 * 
	 * @param client
	 * @return The tracker URI for the creation process or null, if it failed
	 * @throws URISyntaxException
	 * @throws OAuthException
	 * @throws IOException
	 */
	private String createChangeSet(final JazzFormAuthClient client)
			throws URISyntaxException, IOException, OAuthException {
		HashMap<String, String> header = new HashMap<String, String>();
		header.put(OSLCConstants.OSLC_CORE_VERSION, DngTypeSystemManagementConstants.OSLC_VERSION_2_HEADER_VALUE);
		header.put(DngTypeSystemManagementConstants.ACCEPT_HEADER, OSLCConstants.CT_RDF);

		logger.debug("Create Changeset");
		String changeSetCreationFactory = this.getChangeSetFactory().toString();
		logger.debug("Create Changeset using factory '{}'", changeSetCreationFactory);

		// The API does not tolerate a missing about
		// the OSLC client does not tolerate a can not use new URI("")
		setAbout(new URI("http://open-services.net/ns/config#Changeset"));
		ClientResponse response = null;
		String trackerURI = null;
		try {
			response = client.createResource(changeSetCreationFactory, (Changeset) this,
					OslcMediaType.APPLICATION_RDF_XML, OSLCConstants.CT_RDF);
			DngHeaderRequestInterceptor.removeRequestInterceptor(client);
			setAbout(null);

			logger.debug("Status: " + response.getStatusCode());

			/**
			 * 
			 * Behavior of POST
			 * 
			 * A POST request must contain -
			 * 
			 * 
			 * 201 ACCEPTED: The request was completed. The Location response header
			 * contains the URI of the newly-created change set.
			 * 
			 */
			switch (response.getStatusCode()) {
			case 202:
				trackerURI = response.getHeaders().getFirst(HttpHeaders.LOCATION);
				if (trackerURI != null) {
					logger.debug("Change set creation tracker URL is '{}'.", trackerURI);
				}
				return trackerURI;
			default:
				logger.debug("Unexpected return code '{}'.", response.getStatusCode());
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
	 * Tracks the creation and returns the URI
	 * 
	 * @param client
	 * @param trackerURI
	 * @return the URI of the changeSet that was created or null, if the creation
	 *         failed
	 * @throws URISyntaxException
	 */
	private URI trackCreation(final JazzFormAuthClient client, final String trackerURI) throws URISyntaxException {
		boolean result = true;
		Map<String, String> targetJsonHeader = new HashMap<String, String>();
		targetJsonHeader.put(DngTypeSystemManagementConstants.ACCEPT_HEADER,
				DngTypeSystemManagementConstants.CONTENT_TYPE_APPLICATION_JSON);
		targetJsonHeader.put(DngTypeSystemManagementConstants.CONTENT_TYPE_HEADER,
				DngTypeSystemManagementConstants.CONTENT_TYPE_APPLICATION_JSON);

		IRequestTracker tracker = RequestTrackerImpl.getInstance(trackerURI, client, targetJsonHeader);
		URI trackerComplete = new URI(IRequestTracker.TRACKER_STATE_COMPLETE);
		URI trackerVerdictError = new URI(IRequestTracker.TRACKER_VERDICT_ERROR);
		URI trackerVerdictFailed = new URI(IRequestTracker.TRACKER_VERDICT_FAILED);
		URI trackerVerdictPassed = new URI(IRequestTracker.TRACKER_VERDICT_PASSED);

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
		if (!trackerVerdict.equals(trackerVerdictPassed)) {
			result = false;
		}
		if (result == true) {
			logger.trace("The operation has completed successfully!");
			URI changeSetURI = tracker.getReferences();
			return changeSetURI;
		} else {
			logger.error("The operation has failed!");
			logger.error("The tracker reports an error '{}'", tracker.getMessage());
		}
		return null;
	}
}
