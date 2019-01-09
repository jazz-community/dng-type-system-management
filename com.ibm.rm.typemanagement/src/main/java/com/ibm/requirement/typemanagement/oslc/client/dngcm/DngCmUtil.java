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
package com.ibm.requirement.typemanagement.oslc.client.dngcm;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.SimpleSelector;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.wink.client.ClientResponse;
import org.eclipse.lyo.client.exception.ResourceNotFoundException;
import org.eclipse.lyo.client.oslc.OSLCConstants;
import org.eclipse.lyo.client.oslc.jazz.JazzFormAuthClient;
import org.eclipse.lyo.client.oslc.jazz.JazzRootServicesHelper;
import org.eclipse.lyo.oslc4j.core.model.OslcMediaType;
import org.eclipse.lyo.oslc4j.core.model.ServiceProvider;
import org.eclipse.lyo.oslc4j.core.model.ServiceProviderCatalog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.requirement.typemanagement.oslc.client.automation.DngTypeSystemManagementConstants;
import com.ibm.requirement.typemanagement.oslc.client.automation.util.RDFUtils;
import com.ibm.requirement.typemanagement.oslc.client.automation.util.TimeStampUtil;
import com.ibm.requirement.typemanagement.oslc.client.resources.Changeset;
import com.ibm.requirement.typemanagement.oslc.client.resources.Component;
import com.ibm.requirement.typemanagement.oslc.client.resources.Configuration;

import net.oauth.OAuthException;

/**
 * Class to help working with RM components and the CM configurations for them.
 *
 */
public class DngCmUtil {

	public static final Logger logger = LoggerFactory.getLogger(DngCmUtil.class);

	/**
	 * Gets the RM CM Configuration Service Provider from the rootservices document.
	 * E.g. URI looks like https://clm.example.com:9443/rm/oslc_config
	 * 
	 * @param helper the JazzRootservicesHelper
	 * @return the URL of the RM CM Configuration Management service provider URL or
	 *         null;
	 * @throws ResourceNotFoundException
	 */
	public static String getCmServiceProvider(final JazzRootServicesHelper helper) {
		// Get the OSLC Configuration service provider
		final String cmCatalogUrl = RDFUtils.getProperty(helper,
				DngTypeSystemManagementConstants.CONFIGUTATION_MANAGEMENT_NAMESPACE,
				DngTypeSystemManagementConstants.CM_SERVICE_PROVIDER_PROPERTY_NAME);
		return cmCatalogUrl;
	}

	/**
	 * Gets the service provider catalog for the RM CM Configuration service E.g.
	 * URI of the element looks like https://clm.example.com:9443/rm/oslc_config
	 * 
	 * @param client     The JazzFormAuthClient to be used.
	 * @param catalogUrl The RM CM Configuration management service provider URL.
	 * @return
	 * @throws IOException
	 * @throws OAuthException
	 * @throws URISyntaxException
	 */
	public static ServiceProviderCatalog getServiceProviderCatalog(final JazzFormAuthClient client,
			final String catalogUrl) throws IOException, OAuthException, URISyntaxException {
		ServiceProviderCatalog catalog = null;
		ClientResponse response = null;
		try {
			response = client.getResource(catalogUrl, OslcMediaType.RDF_XML);
			logger.debug("Status: " + response.getStatusCode());
			switch (response.getStatusCode()) {
			case 200:
				logger.trace("Success reading '{}'.", catalogUrl);
				catalog = response.getEntity(ServiceProviderCatalog.class);
				break;
			default:
				logger.info("Unexpected return code. Session '{}'.", catalogUrl);
				break;
			}
		} finally {
			if (response != null) {
				response.consumeContent();
			}
		}
		return catalog;
	}

	/**
	 * Finds the project area configuration management component creation factories.
	 * 
	 * @see https://clm.example.com:9443/rm/cm/component/ldpc?project=_5i4eMKYbEei-7e3SSZvGSg
	 * 
	 * @param client
	 * @param rmCmService
	 * @return
	 * @throws IOException
	 * @throws OAuthException
	 * @throws URISyntaxException
	 * @throws ResourceNotFoundException
	 */
	public static HashMap<String, String> getCmOslcLDPComponentCreationFactories(final JazzFormAuthClient client,
			final ServiceProvider rmCmService)
			throws IOException, OAuthException, URISyntaxException, ResourceNotFoundException {
		HashMap<String, String> cmProviders = new HashMap<String, String>();
		ClientResponse response = null;
		try {
			response = client.getResource(rmCmService.getAbout().toString(), OslcMediaType.RDF_XML);
			// logger.info(RDFUtils.getRawResponse(response));
			logger.debug("Status: " + response.getStatusCode());
			switch (response.getStatusCode()) {
			case 200:
				break;
			default:
				logger.trace("Unexpected return code. Session '{}'.", rmCmService);
				response.consumeContent();
				return null;
			}
			Model rdfModel = ModelFactory.createDefaultModel();
			InputStream input = response.getEntity(InputStream.class);
			rdfModel.read(input, "");
			StmtIterator statements = rdfModel
					.listStatements(
							new SimpleSelector(null,
									rdfModel.getProperty(OSLCConstants.OSLC_V2,
											DngTypeSystemManagementConstants.CM_PROVIDER_CREATION_FACTORY),
									(RDFNode) null));
			while (statements.hasNext()) {

				Statement statement = statements.next();
				String url = statement.getObject().toString();
				String[] values = url.split("project=");
				if (values.length > 1) {
					cmProviders.put(values[1], url);
				} else {
					logger.trace("Failed to isolate the projectArea ID from URL '{}'", url);
				}
			}
		} finally {
			if (response != null) {
				response.consumeContent();
			}
		}
		return cmProviders;
	}

	/**
	 * @param client
	 * @param rmCmService
	 * @param projectAreaID
	 * @return
	 * @throws IOException
	 * @throws OAuthException
	 * @throws URISyntaxException
	 * @throws ResourceNotFoundException
	 */
	public static String getCmOslcLDPComponentCreationFactory(final JazzFormAuthClient client,
			final ServiceProvider rmCmService, final String projectAreaID)
			throws IOException, OAuthException, URISyntaxException, ResourceNotFoundException {
		return getCmOslcLDPComponentCreationFactories(client, rmCmService).get(projectAreaID);
	}

	/**
	 * Get the OSLC Configuration service provider
	 * 
	 * @see https://clm.example.com:9443/rm/oslc_config/components
	 * 
	 *      This service provider allows access to resource states and component
	 *      access for configuration aware projects.
	 * 
	 * 
	 * 
	 * @param client
	 * @param catalogUrl
	 * @return
	 * @throws IOException
	 * @throws OAuthException
	 * @throws URISyntaxException
	 * @throws ResourceNotFoundException
	 */
	public static ServiceProvider getRmCmOslcProvider(final JazzFormAuthClient client, final String catalogUrl)
			throws IOException, OAuthException, URISyntaxException, ResourceNotFoundException {
		final ServiceProviderCatalog catalog = getServiceProviderCatalog(client, catalogUrl);
		if (catalog == null) {
			return null;
		}

		final ServiceProvider[] provider = catalog.getServiceProviders();
		for (ServiceProvider serviceProvider : provider) {
			return serviceProvider;
		}
		return null;
	}

	/**
	 * Gets the RM Configuration Management Query URL to query for configurations in
	 * a project area.
	 * 
	 * @param client                        A JazzFormAuthClient
	 * @param cmCatalogUrl                  The RM CM Configuration management
	 *                                      service provider URL.
	 * @param projectAreaServiceProviderUrl The service provider URL for the project
	 *                                      area.
	 * @return
	 * @throws IOException
	 * @throws OAuthException
	 * @throws URISyntaxException
	 * @throws ResourceNotFoundException
	 */
	public static String getRmCmOslcLDPComponentFactory(final JazzFormAuthClient client, final String cmCatalogUrl,
			final String projectAreaId)
			throws IOException, OAuthException, URISyntaxException, ResourceNotFoundException {
		final ServiceProvider rmCmService = getRmCmOslcProvider(client, cmCatalogUrl);
		final String projectAreaComponentFactory = getCmOslcLDPComponentCreationFactory(client, rmCmService,
				projectAreaId);
		return projectAreaComponentFactory;
	}

	/**
	 * Gets the RM Configuration Management Query URL to query for configurations in
	 * a project area.
	 * 
	 * @param client                        A JazzFormAuthClient
	 * @param cmCatalogUrl                  The RM CM Configuration management
	 *                                      service provider URL.
	 * @param projectAreaServiceProviderUrl The service provider URL for the project
	 *                                      area.
	 * @return
	 * @throws IOException
	 * @throws OAuthException
	 * @throws URISyntaxException
	 * @throws ResourceNotFoundException
	 */
	public static Collection<String> getRmCmOslcLDPComponentFactories(final JazzFormAuthClient client,
			final String cmCatalogUrl)
			throws IOException, OAuthException, URISyntaxException, ResourceNotFoundException {
		final ServiceProvider rmCmService = getRmCmOslcProvider(client, cmCatalogUrl);
		final HashMap<String, String> projectAreaComponentFactories = getCmOslcLDPComponentCreationFactories(client,
				rmCmService);
		return projectAreaComponentFactories.values();
	}

	/**
	 * Gets the RM Configuration Management Query URL to query for configurations in
	 * a project area.
	 * 
	 * @param client                        A JazzFormAuthClient
	 * @param cmCatalogUrl                  The RM CM Configuration management
	 *                                      service provider URL.
	 * @param projectAreaServiceProviderUrl The service provider URL for the project
	 *                                      area.
	 * @return
	 * @throws IOException
	 * @throws OAuthException
	 * @throws URISyntaxException
	 * @throws ResourceNotFoundException
	 */
	public static HashMap<String, String> getRmCmOslcLDPComponentFactoryMap(final JazzFormAuthClient client,
			final String cmCatalogUrl)
			throws IOException, OAuthException, URISyntaxException, ResourceNotFoundException {
		final ServiceProvider rmCmService = getRmCmOslcProvider(client, cmCatalogUrl);
		final HashMap<String, String> projectAreaComponentFactories = getCmOslcLDPComponentCreationFactories(client,
				rmCmService);
		return projectAreaComponentFactories;
	}

	/**
	 * 
	 * Query the CM service provider of the project area for components TODO:
	 * explore and implement the real query mechanism e.g. to only query streams if
	 * possible
	 * 
	 * @see https://clm.example.com:9443/rm/cm/component/ldpc?project=_5i4eMKYbEei-7e3SSZvGSg
	 * 
	 * 
	 * @param client
	 * @param componentQuery
	 * @return A collection, never null
	 * @throws IOException
	 * @throws OAuthException
	 * @throws URISyntaxException
	 */
	public static Collection<Component> getComponents(final JazzFormAuthClient client, final String componentQuery)
			throws IOException, OAuthException, URISyntaxException {
		Collection<Component> foundComponents = new ArrayList<Component>();
		ClientResponse response = null;
		try {
			response = client.getResource(componentQuery, OslcMediaType.RDF_XML);
			logger.debug("Status: " + response.getStatusCode());
			switch (response.getStatusCode()) {
			case 200:
				logger.trace("Success reading '{}'.", componentQuery);
				break;
			default:
				logger.trace("Unexpected return code. Session '{}'.", componentQuery);
				response.consumeContent();
				return foundComponents;
			}
			Model model = ModelFactory.createDefaultModel();
			InputStream input = response.getEntity(InputStream.class);
			model.read(input, null);

			StmtIterator statements = model.listStatements(new SimpleSelector(null,
					model.getProperty(DngTypeSystemManagementConstants.CM_LINKED_DATA_PLATFORM_CONTAINER_NAMESPACE,
							DngTypeSystemManagementConstants.PROPERTY_NAME_LDP_CONTAINS_PROPERTY_NAME),
					(RDFNode) null));
			while (statements.hasNext()) {
				Statement stmt = statements.next();
				StmtIterator components = model.listStatements(new SimpleSelector(stmt.getObject().asResource(),
						model.getProperty(OSLCConstants.DC, DngTypeSystemManagementConstants.DC_PROPERTY_NAME_TITLE),
						(RDFNode) null));
				while (components.hasNext()) {
					Statement component = components.next();
					Component foundComp = getComponent(client, component.getSubject().toString());
					foundComponents.add(foundComp);
				}
			}
		} finally {
			if (response != null) {
				response.consumeContent();
			}
		}
		return foundComponents;
	}

	/**
	 * Get a component by its URI
	 * 
	 * @param client
	 * @param componentURI
	 * @return
	 * @throws IOException
	 * @throws OAuthException
	 * @throws URISyntaxException
	 */
	public static Component getComponent(final JazzFormAuthClient client, final String componentURI)
			throws IOException, OAuthException, URISyntaxException {
		Component foundComp = null;
		ClientResponse response = null;
		try {
			response = client.getResource(componentURI, OslcMediaType.RDF_XML);
			logger.debug("Status: " + response.getStatusCode());
			// logger.info("Component:\n {}", RDFUtils.getRDFRepresentation(response));
			switch (response.getStatusCode()) {
			case 200:
				foundComp = response.getEntity(Component.class);
				break;
			default:
				logger.trace("Unexpected return code. Session '{}'.", componentURI);
				break;
			}
		} finally {
			if (response != null) {
				response.consumeContent();
			}
		}
		return foundComp;
	}

	/**
	 * Get all the configurations for a component
	 * 
	 * @param client
	 * @param component
	 * @return
	 * @throws IOException
	 * @throws OAuthException
	 * @throws URISyntaxException
	 */
	public static Collection<Configuration> getComponentConfigurations(final JazzFormAuthClient client,
			final Component component) throws IOException, OAuthException, URISyntaxException {
		Collection<Configuration> foundConfigurations = new ArrayList<Configuration>();
		final String configurations = component.getConfigurations().toString();
		ClientResponse response = null;
		try {
			response = client.getResource(configurations, OslcMediaType.RDF_XML);
			logger.debug("Status: " + response.getStatusCode());
			switch (response.getStatusCode()) {
			case 200:
				break;
			default:
				logger.trace("Unexpected return code. Session '{}'.", configurations);
				response.consumeContent();
				return foundConfigurations;
			}
			Model rdfModel = ModelFactory.createDefaultModel();
			InputStream input = response.getEntity(InputStream.class);
			rdfModel.read(input, "");
			StmtIterator statements = rdfModel.listStatements(new SimpleSelector(null,
					rdfModel.getProperty(OSLCConstants.RDFS, DngTypeSystemManagementConstants.RDF_PROPERTY_MEMBER_NAME),
					(RDFNode) null));
			while (statements.hasNext()) {
				Statement stmt = statements.next();
				String config = stmt.getObject().toString();
				logger.trace("Configuration: '{}'", config);
				Configuration foundConfiguration = getConfiguration(client, config);
				if (foundConfiguration != null) {
					foundConfiguration.setComponentName(component.getTitle());
					foundConfigurations.add(foundConfiguration);
				}
			}
		} finally {
			if (response != null) {
				response.consumeContent();
			}
		}
		return foundConfigurations;
	}

	/**
	 * Get a configuration from RM based on its URI.
	 * 
	 * @param client
	 * @param config
	 * @return
	 * @throws IOException
	 * @throws OAuthException
	 * @throws URISyntaxException
	 */
	public static Configuration getConfiguration(final JazzFormAuthClient client, final String config)
			throws IOException, OAuthException, URISyntaxException {

		if (null == config) {
			return null;
		}
		Configuration foundConfiguration = null;
		ClientResponse response = null;
		try {
			response = client.getResource(config, OslcMediaType.RDF_XML);
			logger.debug("Status: " + response.getStatusCode());
			switch (response.getStatusCode()) {
			case 200:
				logger.trace("Success reading '{}'.", config);
				foundConfiguration = response.getEntity(Configuration.class);
				break;
			case 404:
				logger.trace("Configuration Archived '{}'.", config);
				break;
			default:
				logger.trace("Unexpected return code. Session '{}'.", config);
				break;
			}
		} finally {
			if (response != null) {
				response.consumeContent();
			}
		}
		return foundConfiguration;
	}

	/**
	 * Get the configurations (Streams/baselines) for a given set of components
	 * 
	 * @param client
	 * @param components
	 * @return
	 * @throws IOException
	 * @throws OAuthException
	 * @throws URISyntaxException
	 */
	public static Collection<Configuration> getConfigurationsForComponents(final JazzFormAuthClient client,
			final Collection<Component> components) throws IOException, OAuthException, URISyntaxException {
		Collection<Configuration> configurations = new ArrayList<Configuration>();
		for (Iterator<Component> iterator = components.iterator(); iterator.hasNext();) {
			Component component = (Component) iterator.next();
			configurations.addAll(getComponentConfigurations(client, component));
		}
		return configurations;
	}

	/**
	 * Get all the components owned by a project area
	 * 
	 * @param client
	 * @param cmCatalogUrl
	 * @param projectAreaServiceProviderUrl
	 * @return
	 * @throws IOException
	 * @throws OAuthException
	 * @throws URISyntaxException
	 * @throws ResourceNotFoundException
	 */
	public static Collection<Component> getComponentsForProjectArea(final JazzFormAuthClient client,
			final String cmCatalogUrl, final String projectAreaId)
			throws IOException, OAuthException, URISyntaxException, ResourceNotFoundException {
		String projectAreaComponentFactory = getRmCmOslcLDPComponentFactory(client, cmCatalogUrl, projectAreaId);
		Collection<Component> components = getComponents(client, projectAreaComponentFactory);
		return components;
	}

	/**
	 * Get all the components owned by all project areas
	 * 
	 * @param client
	 * @param cmCatalogUrl
	 * @param projectAreaServiceProviderUrl
	 * @return
	 * @throws IOException
	 * @throws OAuthException
	 * @throws URISyntaxException
	 * @throws ResourceNotFoundException
	 */
	public static Collection<Component> getComponentsForAllProjectAreas(final JazzFormAuthClient client,
			final String cmCatalogUrl)
			throws IOException, OAuthException, URISyntaxException, ResourceNotFoundException {

		Collection<Component> foundComponents = new ArrayList<Component>();

		Collection<String> projectAreaComponentFactories = getRmCmOslcLDPComponentFactories(client, cmCatalogUrl);
		for (String factory : projectAreaComponentFactories) {
			foundComponents.addAll(getComponents(client, factory));
		}
		return foundComponents;
	}

	/**
	 * Create a change set for a target configuration URI (e.g. a Stream)
	 * 
	 * @param client
	 * @param target
	 * @return the change set as a configuration
	 * @throws IOException
	 * @throws OAuthException
	 * @throws URISyntaxException
	 */
	public static Configuration createChangeSetConfiguration(final JazzFormAuthClient client,
			final Configuration target) throws IOException, OAuthException, URISyntaxException {
		if (target == null) {
			logger.info("Target must not be null");
			return null;
		}
		Changeset changeSet = createChangeSet(client, target);
		if (changeSet.getAbout() == null) {
			logger.info("Failed to create change set");
			return null;
		}
		return DngCmUtil.getConfiguration(client, changeSet.getAbout().toString());
	}

	/**
	 * Create a change set for a target configuration URI (e.g. a Stream)
	 * 
	 * @param client
	 * @param target
	 * @return
	 * @throws IOException
	 * @throws OAuthException
	 * @throws URISyntaxException
	 */
	public static Changeset createChangeSet(final JazzFormAuthClient client, final Configuration target)
			throws IOException, OAuthException, URISyntaxException {
		return new Changeset(client, target);
	}

	/**
	 * Do not use.
	 * 
	 * This is an internal API that is not officially supported. Use of this API is
	 * unsupported and the API can change any time without notice.
	 * 
	 * Get the URL to discard change sets
	 * 
	 * @param client
	 * @return
	 */
	public static String getChangeSetDiscardFactory(JazzFormAuthClient client) {
		return client.getUrl() + "/localVersioning/configurations/changesets/discard";
	}

	/**
	 * Do not use.
	 * 
	 * This is an internal API that is not officially supported. Use of this API is
	 * unsupported and the API can change any time without notice.
	 * 
	 * Discard a change set.
	 * 
	 * @param client
	 * @param changeSet
	 * @return
	 * @throws IOException
	 * @throws OAuthException
	 * @throws URISyntaxException
	 */
	public static boolean discardChangeSet(final JazzFormAuthClient client, final Configuration changeSet)
			throws IOException, OAuthException, URISyntaxException {
		if (changeSet == null) {
			logger.info("Changeset must not be null");
			return false;
		}
		return discardChangeSet(client, changeSet.getAbout().toString(), changeSet.getOverrides().toString());
	}

	/**
	 * Do not use.
	 * 
	 * This is an internal API that is not officially supported. Use of this API is
	 * unsupported and the API can change any time without notice.
	 * 
	 * Discard a change set.
	 * 
	 * @param client
	 * @param changeset
	 * @param context
	 * @return
	 */
	public static boolean discardChangeSet(final JazzFormAuthClient client, final String changeset,
			final String context) {

		boolean result = false;
		if (changeset == null) {
			logger.info("Change set must not be null");
			return false;
		}
		if (context == null) {
			logger.info("Context set must not be null");
			return false;
		}

		final String changesetDiscardFactotryURL = getChangeSetDiscardFactory(client);

		// Create JSON object for discard factory
		JsonObject changeSet = Json.createObjectBuilder().add("configurationId", changeset).build();

		HashMap<String, String> addHeader = new HashMap<String, String>();

		addHeader.put("Content-Type", "text/plain");
		addHeader.put("Accept", "*/*");
		addHeader.put(DngTypeSystemManagementConstants.DNG_CM_CONFIGURATION_CONTEXT_HEADER, context);

		logger.debug("DiscardChangeSet");
		logger.trace("CreateChangeSet using factory '{}'", changesetDiscardFactotryURL);
		logger.trace("Json '{}'", changeSet.toString());

		DngHeaderRequestInterceptor.installRequestInterceptor(client, addHeader);
		ClientResponse response = null;
		try {
			response = client.createResource(changesetDiscardFactotryURL, changeSet.toString(),
					OslcMediaType.APPLICATION_JSON);
			DngHeaderRequestInterceptor.removeRequestInterceptor(client);
			logger.debug("Status: " + response.getStatusCode());
			// logger.info("Response: " + RDFUtils.getRawResponse(response));
			/**
			 * 
			 * Behavior of POST
			 * 
			 * 200 discarded. Response body contains OK
			 *
			 * 
			 */
			switch (response.getStatusCode()) {
			case 200:
				if ("OK".endsWith(RDFUtils.getRawResponse(response))) {
					result = true;
				}
				logger.debug("Result '{}'.", result);
				break;
			default:
				logger.debug("Unexpected return code.");
				break;
			}
		} catch (Exception e) {
			logger.error("Exception '{}'.", e.getMessage());
			e.printStackTrace();
		} finally {
			if (response != null) {
				response.consumeContent();
			}
		}
		return result;
	}

}
