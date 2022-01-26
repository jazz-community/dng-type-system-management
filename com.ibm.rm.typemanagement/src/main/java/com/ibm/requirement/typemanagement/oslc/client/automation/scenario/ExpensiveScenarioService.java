/*******************************************************************************
 * Copyright (c) 2012 - 2019 IBM Corporation.
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
package com.ibm.requirement.typemanagement.oslc.client.automation.scenario;

import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.wink.client.ClientResponse;
import org.eclipse.lyo.client.oslc.OSLCConstants;
import org.eclipse.lyo.client.oslc.jazz.JazzFormAuthClient;
import org.eclipse.lyo.oslc4j.core.model.OslcMediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.requirement.typemanagement.oslc.client.automation.DngTypeSystemManagementConstants;
import com.ibm.requirement.typemanagement.oslc.client.automation.util.HeaderRequestInterceptor;

public class ExpensiveScenarioService implements IExpensiveScenarioService {

	public static final Logger logger = LoggerFactory.getLogger(ExpensiveScenarioService.class);

	private static final String EXPENSIVE_SCENARIO_START_PATH = "/service/com.ibm.team.repository.service.serviceability.IScenarioRestService/scenarios/startscenario";
	private static final String EXPENSIVE_SCENARIO_STOP_PATH = "/service/com.ibm.team.repository.service.serviceability.IScenarioRestService/scenarios/stopscenario";
	private static final String ACCEPT_HEADER = "Accept";
	private static final String CONTENT_TYPE = "Content-Type";
	private static final String ENCODING_UTF_8 = "UTF-8";
	private static final String SCENARIO_NAME = "scenarioName";
	private static final String SCENARIO_INSTANCE_ID = "scenarioInstanceId";

	private URI fPublicURI = null;
	private String fScenarioName = null;
	private JazzFormAuthClient fClient = null;

	/**
	 * Start and stop expensive scenario counter are performed persisting the
	 * scenario counter in a file or pass it as string. See option
	 * persistStartAsFile.
	 * 
	 * @param teamRepository
	 *            Team repository
	 * @param publicURI
	 *            Public URI of the target CLM server
	 * @param scenarioName
	 *            the name of the scenario
	 * @throws URISyntaxException
	 */
	public ExpensiveScenarioService(final JazzFormAuthClient client, final String publicURI, final String scenarioName)
			throws URISyntaxException, NullPointerException {
		if (client == null)
			throw new NullPointerException("Client URI can not be null");
		this.fClient = client;
		if (publicURI == null)
			throw new NullPointerException("Public URI can not be null");
		this.fPublicURI = new URI(publicURI.replaceAll("/$", ""));
		if (scenarioName == null)
			throw new NullPointerException("Scenario name can not be null");
		this.fScenarioName = scenarioName;
	}

	/**
	 * Construct the service URI from the public URI.
	 * 
	 * @param path
	 * @return
	 */
	private URI getServiceURI(String path) {
		return URI.create(fPublicURI.toString() + path);
	}

	@Override
	public String start() throws Exception {
		ClientResponse response = null;
		try {
			// Compose the request body
			JsonObject scenarioRequest = Json.createObjectBuilder().add(SCENARIO_NAME, fScenarioName).build();

			HashMap<String, String> addHeader = new HashMap<String, String>();

			addHeader.put(CONTENT_TYPE, OSLCConstants.CT_JSON);
			addHeader.put(ACCEPT_HEADER, OSLCConstants.CT_JSON);
			HashMap<String, String> removeHeader = new HashMap<String, String>();
			removeHeader.put(OSLCConstants.OSLC_CORE_VERSION, null);
			HeaderRequestInterceptor.installRequestInterceptor(this.fClient, addHeader, removeHeader);
			// Get the connection
			String startUri = getServiceURI(EXPENSIVE_SCENARIO_START_PATH).toString();
			logger.debug("StartScenario");
			logger.trace("Json '{}'", scenarioRequest.toString());

			response = this.fClient.createResource(startUri, scenarioRequest.toString(),
					OslcMediaType.APPLICATION_JSON);
			HeaderRequestInterceptor.removeRequestInterceptor(this.fClient);
			logger.debug("Status: " + response.getStatusCode());
			/**
			 * 
			 * Behavior of POST
			 * 
			 * 200 created. Json response body contains the URI of the change
			 * set:
			 * 
			 * {"scenarioName":"MyCustomExpensiveScenario",
			 * "scenarioInstanceId":"_Jbe94DaQEempFf7xSdsBAQ",
			 * "scenarioHeaderKey":"x-com-ibm-team-scenario",
			 * "scenarioHeaderValue":"_Jbe94DaQEempFf7xSdsBAQ%3Bname%3DMyCustomExpensiveScenario"}
			 *
			 */
			int statusCode = response.getStatusCode();
			if (statusCode == HttpStatus.SC_OK) {
				InputStream input = response.getEntity(InputStream.class);
				// Create JsonReader object
				String responseString = IOUtils.toString(input, ENCODING_UTF_8);
				JsonReaderFactory factory = Json.createReaderFactory(null);
				JsonReader jsonReader = factory.createReader(new StringReader(responseString));
				JsonObject json = jsonReader.readObject();

				String scenarioName = null;
				if (json.containsKey(SCENARIO_NAME)) {
					scenarioName = (String) json.getJsonString(SCENARIO_NAME).toString();
				}
				if (scenarioName.equals("\"" + fScenarioName + "\"")) {
					return responseString;
				}
				throw new Exception("Unexpected Response Body '" + responseString + "'");
			} else {
				logger.debug("Unexpected return code.");
			}
		} finally {
			if (response != null) {
				response.consumeContent();
			}
		}
		return null;
	}

	@Override
	public void stop(final String startRequestResponse) throws Exception {
		ClientResponse response = null;
		try {

			String startRequest = startRequestResponse;
			String scenarioInstanceID;
			if (null == startRequest) {
				throw new Exception("Missing Scenario Start Request");
			}
			try {
				// Parse as JSON to get the scenario ID and the scenario name
				// from the file
				JsonReaderFactory factory = Json.createReaderFactory(null);
				JsonReader jsonReader = factory.createReader(new StringReader(startRequest));
				JsonObject json = jsonReader.readObject();

				String scenarioName = null;
				if (json.containsKey(SCENARIO_NAME)) {
					scenarioName = (String) json.getJsonString(SCENARIO_NAME).toString();
				}
				scenarioInstanceID = null;
				if (json.containsKey(SCENARIO_INSTANCE_ID)) {
					scenarioInstanceID = (String) json.getJsonString(SCENARIO_INSTANCE_ID).toString();
				}
				String compareName = "\"" + fScenarioName + "\"";
				if (!compareName.equals(scenarioName)) {
					throw new Exception("Incorrect Scenario Name Exception");
				}
			} catch (Exception e) {
				throw new Exception("Error Parsing Scenario Start Request '" + startRequest + "'");
			}

			HashMap<String, String> addHeader = new HashMap<String, String>();

			addHeader.put(CONTENT_TYPE, OSLCConstants.CT_JSON);
			addHeader.put(ACCEPT_HEADER, OSLCConstants.CT_JSON);
			HashMap<String, String> removeHeader = new HashMap<String, String>();
			removeHeader.put(OSLCConstants.OSLC_CORE_VERSION, null);
			HeaderRequestInterceptor.installRequestInterceptor(this.fClient, addHeader, removeHeader);
			// Get the connection
			String startUri = getServiceURI(EXPENSIVE_SCENARIO_STOP_PATH).toString();

			logger.debug("StartScenario");
			logger.trace("Json '{}'", startRequest);

			response = this.fClient.createResource(startUri, startRequest, OslcMediaType.APPLICATION_JSON);
			HeaderRequestInterceptor.removeRequestInterceptor(this.fClient);
			logger.debug("Status: " + response.getStatusCode());
			// logger.info("Response: " + RDFUtils.getRawResponse(response));
			/**
			 * 
			 * Behavior of POST
			 * 
			 * 200 created. Json response body contains the URI of the change
			 * set:
			 * 
			 * {"scenarioName":"MyCustomExpensiveScenario",
			 * "scenarioInstanceId":"_Jbe94DaQEempFf7xSdsBAQ",
			 * "scenarioHeaderKey":"x-com-ibm-team-scenario",
			 * "scenarioHeaderValue":"_Jbe94DaQEempFf7xSdsBAQ%3Bname%3DMyCustomExpensiveScenario"}
			 *
			 */
			int status = response.getStatusCode();
			if (status == HttpStatus.SC_OK) {
				InputStream input = response.getEntity(InputStream.class);
				// Create JsonReader object
				String responseString = IOUtils.toString(input, ENCODING_UTF_8);
				if (responseString != null) {
					// System.out.println(responseString);
					if (responseString.equals(scenarioInstanceID)) {
						return;
					}
					throw new Exception("Response Body Scenario Mismatch Exception");
				}
				throw new Exception("Missing Response Body Exception");
			}
			throw new Exception("Unexpected Response Code '" + status + "'");
		} finally {
			if (response != null) {
				response.consumeContent();
			}
		}
	}

	@Override
	public Object getScenarioName() {
		return fScenarioName;
	}

	/**
	 * Stop a Resource Intensive Scenario instance
	 * 
	 * @see https://jazz.net/wiki/bin/view/Deployment/CreateCustomScenarios
	 * 
	 *      Wrap all errors
	 * 
	 * @param scenarioInstance
	 */
	public static void stopScenario(final IExpensiveScenarioService scenarioService, final String scenarioInstance) {
		try {
			scenarioService.stop(scenarioInstance);
		} catch (Exception e) {
			logger.trace("Resource Intensive Scenario Notifier Service: Scenario can not be stopped!");
		}
	}

	/**
	 * Start a Resource Intensive Scenario instance
	 * 
	 * @see https://jazz.net/wiki/bin/view/Deployment/CreateCustomScenarios
	 * 
	 *      Wrap all errors
	 * 
	 * @param client
	 * @param webContextUrl
	 * @param commandName
	 * @return
	 */
	public static String startScenario(final IExpensiveScenarioService scenarioService) {
		String scenarioInstance = null;
		try {
			scenarioInstance = scenarioService.start();
		} catch (Exception e) {
			logger.trace("Resource Intensive Scenario Notifier Service: Scenario can not be started!");
		}
		return scenarioInstance;
	}

	/**
	 * Create a Resource Intensive Scenario instance
	 * 
	 * @see https://jazz.net/wiki/bin/view/Deployment/CreateCustomScenarios
	 * 
	 *      Wrap all errors
	 * 
	 * @param client
	 * @param webContextUrl
	 * @param toolName
	 * @param commandName
	 * @param versionInfo
	 * @return
	 */
	public static IExpensiveScenarioService createScenarioService(final JazzFormAuthClient client,
			final String webContextUrl, final String toolName, final String versionInfo, final String commandName) {
		IExpensiveScenarioService scenarioService = null;
		try {
			scenarioService = new ExpensiveScenarioService(client, webContextUrl,
					toolName + "_" + versionInfo + "_" + commandName);
		} catch (Exception e) {
			logger.trace("Resource Intensive Scenario Notifier Service: Scenario can not be started!");
		}
		return scenarioService;
	}

	/**
	 * Create a Resource Intensive Scenario instance
	 * 
	 * @see https://jazz.net/wiki/bin/view/Deployment/CreateCustomScenarios
	 * 
	 *      Wrap all errors
	 * 
	 * @param client
	 * @param webContextUrl
	 * @param commandName
	 * @return
	 */
	public static IExpensiveScenarioService createScenarioService(final JazzFormAuthClient client,
			final String webContextUrl, final String commandName) {
		return createScenarioService(client, webContextUrl, "TSM", DngTypeSystemManagementConstants.VERSIONINFO,
				commandName);
	}

}
