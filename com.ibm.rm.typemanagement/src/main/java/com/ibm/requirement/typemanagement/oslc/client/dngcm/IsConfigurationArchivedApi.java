package com.ibm.requirement.typemanagement.oslc.client.dngcm;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
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

import net.oauth.OAuthException;

/**
 * Internal API implementation to find dependent configurations Usage of this
 * API voids support.
 * 
 * @deprecated
 * 
 *
 */
public class IsConfigurationArchivedApi {

	public static final Logger logger = LoggerFactory.getLogger(IsConfigurationArchivedApi.class);
	public static final String queryConfigurationIsArchived = "/localVersioning/configurations?configurationUri=";

	/**
	 * @param client
	 * @param configuration
	 * @return
	 * @throws IOException
	 * @throws OAuthException
	 * @throws URISyntaxException
	 */
	public static CallStatus isArchived(final JazzFormAuthClient client, final Configuration configuration,
			final int testmode) {
		if (configuration == null) {
			String message = "Error: parameter 'configuration' must not be null";
			logger.info(message);
			CallStatus status = new CallStatus(message);
			return status;
		}
		return isArchived(client, configuration.getAbout().toString(), testmode);
	}

	/**
	 * @param client
	 * @param configuration
	 * @return
	 */
	public static CallStatus isArchived(final JazzFormAuthClient client, final String configuration,
			final int testmode) {
		CallStatus resultStatus = new CallStatus();
		InputStream input = null;
		if (configuration == null) {
			String message = "Error: parameter 'configuration' must not be null";
			logger.info(message);
			resultStatus.setMessage(message);
			return resultStatus;
		}

		// logger.info("Testing '{}'", configuration);
		// GET
		// https://dev.elm.net/rm/localVersioning/configurations?configurationUri=https://dev.elm.net/rm/cm/baseline/_i-j-kE71Ee2Wt61sMZ9ehg&accept=application/json
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
		final String finalUrl = url.concat(queryConfigurationIsArchived).concat(param);

		logger.debug("Is configuration archived API");
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
				JsonObject json;
				// Create JsonReader object
				JsonReaderFactory factory = Json.createReaderFactory(null);
				if (input == null) {
					resultStatus.setMessage("No data returned");
					resultStatus.setCallResult(false);
				}
				JsonReader jsonReader = factory.createReader(input);
				json = jsonReader.readObject();
				input.close();
				// logger.info("Result '{}'",json.toString());
				boolean isArchived = json.getBoolean("archived");
				if (isArchived) {
					resultStatus.setMessage("Configuration is archived");
					resultStatus.setCallResult(isArchived);
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
