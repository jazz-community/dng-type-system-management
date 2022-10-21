package com.ibm.requirement.typemanagement.oslc.client.dngcm;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.eclipse.lyo.client.oslc.jazz.JazzFormAuthClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.requirement.typemanagement.oslc.client.resources.Configuration;

/**
 * Internal API implementation to find dependent configurations Usage of this
 * API voids support.
 * 
 * @deprecated
 * 
 *
 */
public class DependentConfigurationsApi {

	public static final Logger logger = LoggerFactory.getLogger(DependentConfigurationsApi.class);
	public static final String queryConfigurations = "/localVersionExplorer/configurations?includeArchived=false&pageStart=0&pageEnd=10&configurationId=";

	/**
	 * @param client
	 * @param configuration
	 * @return
	 */
	public static CallStatus hasNoDependentConfigurations(final JazzFormAuthClient client,
			final Configuration configuration, final int testmode) {
		if (configuration == null) {
			String message = "Configuration must not be null";
			logger.info(message);
			CallStatus status = new CallStatus(message);
			return status;
		}
		return hasNoDependentConfiguration(client, configuration.getAbout().toString(), testmode);
	}

	/**
	 * @param client
	 * @param configuration
	 * @return
	 */
	public static CallStatus hasNoDependentConfiguration(final JazzFormAuthClient client, final String configuration,
			final int testmode) {
		CallStatus resultStatus = new CallStatus();
		InputStream input = null;
		// boolean result = false;
		if (configuration == null) {
			String message = "Configuration must not be null";
			logger.info(message);
			CallStatus status = new CallStatus(message);
			return status;
		}
		// logger.info("Testing '{}'", configuration);
		// GET
		// rm/localVersionExplorer/configurations?includeArchived=false&pageStart=0&pageEnd=10&configurationId=https%3A%2F%2Fdev.elm.net%2Frm%2Fcm%2Fstream%2F_NrZk9gz0Ee2kputSs8qI0g
		// Accept=application/json
		// Sample of JSON response:
		// {"https://dev.elm.net/rm/cm/stream/_NrZk9gz0Ee2kputSs8qI0g":{"data":["https://dev.elm.net/rm/cm/baseline/_iDNLkAz0Ee2kputSs8qI0g"],"totalNumberOfResults":1}}
		String url = client.getUrl();
		String param = null;
		try {
			param = URLEncoder.encode(configuration, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			String message = "Exception unsupported encoding: " + e.getMessage();
			logger.error(message);
			resultStatus.setMessage(message);
			return resultStatus;
		}
		final String finalUrl = url.concat(queryConfigurations).concat(param);

		logger.debug("Dependent configurations API");
		HttpResponse response = null;
		try {
			if (testmode == 2) {
				throw new Exception("TestException");
			}
			HttpClient rawClient = client.getHttpClient();
			HttpUriRequest archive = new HttpGet(finalUrl);
			archive.addHeader("Accept", "application/json");
			response = rawClient.execute(archive);
			final HttpEntity entity = response.getEntity();
			if (entity != null) {
				input = entity.getContent();
			}
			int statusCode = response.getStatusLine().getStatusCode();
			logger.debug("Status: " + Integer.toString(statusCode));
			if (testmode == 1) {
				statusCode = 400;
			}

			/**
			 * 
			 * Behavior of GET
			 * 
			 * 200 archived.
			 *
			 * 
			 */
			switch (statusCode) {
			case 200:
				resultStatus.setCallSuccess(true);
				String aResult = response.toString();
				logger.debug("Result '{}'.", aResult);
				if (input == null) {
					resultStatus.setMessage("No data returned");
					resultStatus.setCallResult(false);
				}

				JsonObject json;
				// Create JsonReader object
				JsonReaderFactory factory = Json.createReaderFactory(null);
				JsonReader jsonReader = factory.createReader(input);
				json = jsonReader.readObject();
				input.close();
				// String resultString = IOUtils.toString(entity.getContent(), "UTF-8");
				// logger.info("Result '{}'",json.toString());
				JsonObject x = json.getJsonObject(configuration);
				// Integer noOfResults = x.getInt("data");
				Integer noOfResults = x.getInt("totalNumberOfResults");
				resultStatus.setNoResults(noOfResults);
				if (noOfResults == 0) {
					resultStatus.setCallResult(true);
					return resultStatus;
				}
				break;
			default:
				String message = "Unexpected return code '" + statusCode + "'.";
				if (input != null) {
					input.close();
				}
				logger.debug(message);
				resultStatus.setMessage(message);
				break;
			}
		} catch (Exception e) {
			String message = "Exception " + e.getMessage();
			// logger.error(message);
			// e.printStackTrace();
			resultStatus.setMessage(message);
			return resultStatus;
		} finally {
			if (response != null) {
				response = null;
			}

		}
		return resultStatus;
	}

}
